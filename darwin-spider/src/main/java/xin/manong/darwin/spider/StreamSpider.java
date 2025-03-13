package xin.manong.darwin.spider;

import jakarta.annotation.Resource;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.weapon.base.common.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 流媒体爬虫
 * 1. M3U8视频流
 *
 * @author frankcl
 * @date 2023-03-24 16:23:28
 */
@Component
public class StreamSpider extends Spider {

    private static final Logger logger = LoggerFactory.getLogger(StreamSpider.class);

    private static final String CATEGORY = "stream";
    private static final String LIVE_STREAM_FINISH_TAG = "#EXT-X-ENDLIST";

    private final String ffmpeg;
    @Resource
    protected SpiderConfig config;
    @Resource
    protected ProxyService proxyService;

    public StreamSpider() {
        super(CATEGORY);
        ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
        if (StringUtils.isEmpty(ffmpeg)) throw new UnsupportedOperationException("ffmpeg is not found");
    }

    @Override
    protected void handle(URLRecord record, Context context) throws Exception {
        InputStream inputStream = getPrevInputStream(record, context);
        String tempFile = String.format("%s/%s.mp4", config.tempDirectory, record.key);
        try {
            if (inputStream == null) {
                inputStream = getM3U8InputStream(record, tempFile, context);
                record.mimeType = Spider.MIME_TYPE_VIDEO;
                record.subMimeType = Spider.SUB_MIME_TYPE_MP4;
                record.httpCode = Spider.HTTP_CODE_OK;
                record.status = Constants.URL_STATUS_SUCCESS;
            }
            write(record, inputStream, context);
        } finally {
            if (inputStream != null) inputStream.close();
            if (!new File(tempFile).delete()) logger.warn("delete temp file failed: {}", tempFile);
        }
    }

    /**
     * 抓取M3U8视频流
     *
     * @param record URL数据
     * @param tempFile 临时文件
     * @param context 上下文
     * @return 输入流
     * @throws Exception 异常
     */
    private InputStream getM3U8InputStream(URLRecord record, String tempFile, Context context) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            String meta = fetchM3U8Meta(record);
            if (!meta.contains(LIVE_STREAM_FINISH_TAG)) {
                record.httpCode = HTTP_CODE_NOT_ACCEPTABLE;
                logger.error("unsupported live stream fetching for URL[{}]", record.url);
                throw new IllegalStateException("不支持直播流抓取");
            }
            fetchM3U8Stream(record, tempFile);
            return new FileInputStream(tempFile);
        } finally {
            context.put(Constants.DARWIN_FETCH_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 下载M3U8直播流资源
     *
     * @param record URL记录
     * @param tempFile 保存临时文件
     */
    private void fetchM3U8Stream(URLRecord record, String tempFile) throws Exception {
        Process process = null;
        try {
            createTempDirectory();
            List<String> commands = buildFFMPEGCommands(record, tempFile);
            ProcessBuilder processBuilder = new ProcessBuilder();
            process = processBuilder.inheritIO().command(commands).start();
            int code = process.waitFor();
            String cmd = String.join(" ", commands.toArray(new String[0]));
            if (code != 0) {
                logger.error("execute ffmpeg command[{}] failed for code[{}]", cmd, code);
                throw new IllegalStateException(String.format("ffmpeg抓取直播流失败，返回码[%d]", code));
            }
            logger.error("execute ffmpeg command[{}] success", cmd);
        } catch (Exception e) {
            logger.error("exception occurred when fetching M3U8 stream for URL[{}]", record.url);
            throw new IOException("ffmpeg抓取直播流异常");
        } finally {
            if (process != null) process.destroy();
        }
    }

    /**
     * 抓取M3U8元数据
     *
     * @param record URL记录
     * @return 成功返回M3U8元数据，否则返回null
     */
    private String fetchM3U8Meta(URLRecord record) throws Exception {
        try (Response httpResponse = httpRequest(record)) {
            if (httpResponse == null || !httpResponse.isSuccessful()) {
                if (httpResponse != null) record.httpCode = httpResponse.code();
                logger.error("fetch M3U8 meta failed for URL[{}]", record.url);
                throw new IOException("抓取M3U8元信息失败");
            }
            assert httpResponse.body() != null;
            return httpResponse.body().string();
        }
    }

    /**
     * 构建FFMPEG命令
     *
     * @param record URL记录
     * @param tempFilePath 本地临时文件地址
     * @return 命令
     */
    private List<String> buildFFMPEGCommands(URLRecord record, String tempFilePath) {
        List<String> commands = new ArrayList<>();
        commands.add(ffmpeg);
        commands.add("-user_agent");
        commands.add(config.userAgent);
        addProxyOptions(commands, record);
        commands.add("-i");
        commands.add(record.url);
        commands.add(tempFilePath);
        return commands;
    }

    /**
     * 添加代理选项
     *
     * @param commands FFMPEG命令
     * @param record URL记录
     */
    private void addProxyOptions(List<String> commands, URLRecord record) {
        if (!record.useProxy()) return;
        Proxy proxy = proxyService.randomGet(record.fetchMethod);
        if (proxy == null) return;
        commands.add("-http_proxy");
        commands.add(proxy.toString());
    }

    /**
     * 创建本地临时目录
     */
    private void createTempDirectory() {
        File directory = new File(config.tempDirectory);
        if (directory.exists()) return;
        if (directory.mkdirs()) logger.info("create temp directory[{}] success", config.tempDirectory);
    }
}
