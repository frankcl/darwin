package xin.manong.darwin.spider;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.base.common.Context;

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

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36";

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
        Long executeFetchTime = 0L, executePutTime = 0L;
        try {
            Long startFetchTime = System.currentTimeMillis();
            if (inputStream == null) {
                File directory = new File(config.tempDirectory);
                if (!directory.exists()) directory.mkdirs();
                List<String> commands = buildCommands(record.url, tempFile);
                ProcessBuilder processBuilder = new ProcessBuilder();
                process = processBuilder.inheritIO().command(commands).start();
                int code = process.waitFor();
                executeFetchTime = System.currentTimeMillis() - startFetchTime;
                if (code != 0) {
                    record.status = Constants.URL_STATUS_FAIL;
                    context.put(Constants.DARWIN_DEBUG_MESSAGE, "执行ffmpeg抓取失败");
                    logger.error("execute ffmpeg command[{}] failed for code[{}]",
                            String.join(" ", commands.toArray(new String[0])), code);
                    return;
                }
                inputStream = new FileInputStream(tempFile);
            }
            Long startPutTime = System.currentTimeMillis();
            if (inputStream == null || !writeContent(record, inputStream)) {
                executePutTime = System.currentTimeMillis() - startPutTime;
                record.status = Constants.URL_STATUS_FAIL;
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取内容写入OSS失败");
                logger.error("write fetch content failed for url[{}]", record.url);
            }
            executePutTime = System.currentTimeMillis() - startPutTime;
        } finally {
            context.put(Constants.DARWIN_FETCH_TIME, executeFetchTime);
            context.put(Constants.DARWIN_PUT_TIME, executePutTime);
            if (process != null) process.destroy();
            if (inputStream != null) inputStream.close();
            File file = new File(tempFile);
            if (file.exists()) file.delete();
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
        commands.add(USER_AGENT);
        commands.add("-i");
        commands.add(m3u8URL);
        commands.add(tempFilePath);
        return commands;
    }
}
