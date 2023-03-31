package xin.manong.darwin.spider;

import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
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
        Process process = null;
        InputStream inputStream = (InputStream) context.get(Constants.DARWIN_INPUT_STREAM);
        String tempFile = String.format("%s/%s.mp4", config.tempDirectory, record.key);
        Long fetchTime = 0L, putTime = 0L;
        try {
            Long startFetchTime = System.currentTimeMillis();
            record.fetchTime = System.currentTimeMillis();
            if (inputStream == null) {
                buildHttpClient();
                HttpRequest httpRequest = new HttpRequest.Builder().requestURL(record.url).
                        method(RequestMethod.GET).build();
                Response httpResponse = httpClient.execute(httpRequest);
                if (httpResponse == null || !httpResponse.isSuccessful()) {
                    if (httpResponse != null) httpResponse.close();
                    record.status = Constants.URL_STATUS_FAIL;
                    context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取M3U8元信息失败");
                    logger.error("fetch M3U8 meta failed for url[{}]", record.url);
                    return;
                }
                if (isLiveStream(httpResponse, context)) {
                    if (!context.contains(Constants.DARWIN_DEBUG_MESSAGE)) {
                        context.put(Constants.DARWIN_DEBUG_MESSAGE, "不支持直播流抓取");
                    }
                    record.status = Constants.URL_STATUS_FAIL;
                    logger.error("unsupported live stream fetching for url[{}]", record.url);
                    return;
                }
                File directory = new File(config.tempDirectory);
                if (!directory.exists()) directory.mkdirs();
                List<String> commands = buildCommands(record.url, tempFile);
                ProcessBuilder processBuilder = new ProcessBuilder();
                process = processBuilder.inheritIO().command(commands).start();
                int code = process.waitFor();
                fetchTime = System.currentTimeMillis() - startFetchTime;
                if (code != 0) {
                    record.status = Constants.URL_STATUS_FAIL;
                    context.put(Constants.DARWIN_DEBUG_MESSAGE, "执行ffmpeg抓取失败");
                    logger.error("execute ffmpeg command[{}] failed for code[{}]",
                            String.join(" ", commands.toArray(new String[0])), code);
                    return;
                }
                context.put(Constants.RESOURCE_SUFFIX, "mp4");
                inputStream = new FileInputStream(tempFile);
            }
            Long startPutTime = System.currentTimeMillis();
            if (inputStream == null || !writeContent(record, inputStream, context)) {
                putTime = System.currentTimeMillis() - startPutTime;
                record.status = Constants.URL_STATUS_FAIL;
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取内容写入OSS失败");
                logger.error("write fetch content failed for url[{}]", record.url);
            }
            record.status = Constants.URL_STATUS_SUCCESS;
            putTime = System.currentTimeMillis() - startPutTime;
        } finally {
            context.put(Constants.DARWIN_FETCH_TIME, fetchTime);
            context.put(Constants.DARWIN_PUT_TIME, putTime);
            if (process != null) process.destroy();
            if (inputStream != null) inputStream.close();
            File file = new File(tempFile);
            if (file.exists()) file.delete();
        }
    }

    /**
     * 判断是否为直播流
     *
     * @param httpResponse HTTP响应
     * @param context 上下文
     * @return 直播流或判断失败返回true，否则返回false
     */
    private boolean isLiveStream(Response httpResponse, Context context) {
        try {
            String body = httpResponse.body().string();
            return body.indexOf("#EXT-X-ENDLIST") == -1;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "判断直播状态失败");
            return true;
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
    private List<String> buildCommands(String m3u8URL, String tempFilePath) {
        List<String> commands = new ArrayList<>();
        commands.add(ffmpeg);
        commands.add("-user_agent");
        commands.add(config.userAgent);
        commands.add("-i");
        commands.add(m3u8URL);
        commands.add(tempFilePath);
        return commands;
    }
}
