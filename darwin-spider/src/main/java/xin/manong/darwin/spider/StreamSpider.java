package xin.manong.darwin.spider;

import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bytedeco.javacpp.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestMethod;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
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

    private static final String LIVE_STREAM_FINISH_TAG = "#EXT-X-ENDLIST";

    private String ffmpeg;
    @Resource
    protected SpiderConfig config;

    public StreamSpider() {
        super("stream");
        ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
        if (StringUtils.isEmpty(ffmpeg)) throw new RuntimeException("ffmpeg command is not found");
    }

    @Override
    protected void handle(URLRecord record, Context context) throws Exception {
        SpiderResource resource = getPreviousResource(record, context);
        String tempFile = String.format("%s/%s.mp4", config.tempDirectory, record.key);
        try {
            if (resource == null || resource.inputStream == null) {
                record.fetchTime = System.currentTimeMillis();
                String m3u8Body = fetchM3U8Meta(record, context);
                if (m3u8Body.indexOf(LIVE_STREAM_FINISH_TAG) == -1) {
                    record.status = Constants.URL_STATUS_FAIL;
                    context.put(Constants.DARWIN_DEBUG_MESSAGE, "不支持直播流抓取");
                    logger.error("unsupported live stream fetching for url[{}]", record.url);
                    return;
                }
                createTempDirectory();
                if (!fetchM3U8Resource(record, context, tempFile)) return;
                resource = new SpiderResource();
                resource.inputStream = new FileInputStream(tempFile);
                resource.mimeType = MIME_TYPE_VIDEO;
                resource.subMimeType = SUB_MIME_TYPE_MP4;
                record.mimeType = MIME_TYPE_VIDEO;
                record.subMimeType = SUB_MIME_TYPE_MP4;
            }
            Long startTime = System.currentTimeMillis();
            try {
                if (!writeContent(record, resource.inputStream, context)) return;
                record.status = Constants.URL_STATUS_SUCCESS;
            } finally {
                context.put(Constants.DARWIN_WRITE_TIME, System.currentTimeMillis() - startTime);
            }
        } finally {
            if (resource != null) resource.close();
            new File(tempFile).delete();
        }
    }

    /**
     * 下载M3U8直播流资源
     *
     * @param record URL记录
     * @param context 上下文
     * @param tempFile 保存临时文件
     * @return 成功返回true，否则返回false
     */
    private boolean fetchM3U8Resource(URLRecord record, Context context, String tempFile) {
        Process process = null;
        Long startTime = System.currentTimeMillis();
        try {
            List<String> commands = buildFFMPEGCommands(record.url, tempFile);
            ProcessBuilder processBuilder = new ProcessBuilder();
            process = processBuilder.inheritIO().command(commands).start();
            int returnCode = process.waitFor();
            if (returnCode == 0) return true;
            record.status = Constants.URL_STATUS_FAIL;
            context.put(Constants.DARWIN_DEBUG_MESSAGE, String.format("ffmpeg抓取直播流失败，返回码[%d]", returnCode));
            logger.error("execute ffmpeg command[{}] failed for code[{}]",
                    String.join(" ", commands.toArray(new String[0])), returnCode);
            return false;
        } catch (Exception e) {
            record.status = Constants.URL_STATUS_FAIL;
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "ffmpeg抓取直播流异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            if (process != null) process.destroy();
            context.put(Constants.DARWIN_FETCH_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 抓取M3U8元数据
     *
     * @param record URL记录
     * @param context 上下文
     * @return 成功返回M3U8元数据，否则返回null
     */
    private String fetchM3U8Meta(URLRecord record, Context context) {
        buildHttpClient();
        HttpRequest httpRequest = new HttpRequest.Builder().requestURL(record.url).
                method(RequestMethod.GET).build();
        Response httpResponse = httpClient.execute(httpRequest);
        try {
            if (httpResponse == null || !httpResponse.isSuccessful()) {
                if (httpResponse != null) httpResponse.close();
                record.status = Constants.URL_STATUS_FAIL;
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取M3U8元信息失败");
                logger.error("fetch M3U8 meta failed for url[{}]", record.url);
                return null;
            }
            return httpResponse.body().string();
        } catch (Exception e) {
            record.status = Constants.URL_STATUS_FAIL;
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取M3U8元信息异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (httpResponse != null) httpResponse.close();
        }
    }

    /**
     * 构建FFMPEG命令
     *
     * @param m3u8URL m3u8地址
     * @param tempFilePath 本地临时文件地址
     * @return 命令
     */
    private List<String> buildFFMPEGCommands(String m3u8URL, String tempFilePath) {
        List<String> commands = new ArrayList<>();
        commands.add(ffmpeg);
        commands.add("-user_agent");
        commands.add(config.userAgent);
        commands.add("-i");
        commands.add(m3u8URL);
        commands.add(tempFilePath);
        return commands;
    }

    /**
     * 创建本地临时目录
     */
    private void createTempDirectory() {
        File directory = new File(config.tempDirectory);
        if (directory.exists()) return;
        directory.mkdirs();
        logger.info("create temp directory[{}] success", config.tempDirectory);
    }
}
