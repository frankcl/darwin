package xin.manong.darwin.spider.input;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.spider.core.SpiderConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * M3U8视频流输入源
 *
 * @author frankcl
 * @date 2025-04-11 17:59:46
 */
public class M3U8Input extends Input {

    private static final Logger logger = LoggerFactory.getLogger(M3U8Input.class);

    private final String ffmpeg;
    private final URLRecord record;
    private final Proxy proxy;
    private final SpiderConfig config;
    private final File tempFile;

    public M3U8Input(URLRecord record, Proxy proxy, SpiderConfig config) {
        this.record = record;
        this.proxy = proxy;
        this.config = config;
        this.tempFile = new File(String.format("%s/%s.mp4", config.tempDirectory, record.key));
        ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
        if (StringUtils.isEmpty(ffmpeg)) throw new UnsupportedOperationException("ffmpeg is not found");
    }

    @Override
    public void open() throws IOException {
        close();
        Process process = null;
        try {
            createTempDirectory();
            List<String> commands = buildCommands(record, tempFile.getAbsolutePath());
            ProcessBuilder processBuilder = new ProcessBuilder();
            process = processBuilder.inheritIO().command(commands).start();
            int code = process.waitFor();
            if (code == 0) {
                inputStream = new FileInputStream(tempFile);
                record.contentLength = tempFile.length();
                return;
            }
            throw new IllegalStateException(String.format("Fetch M3U8 stream failed for %s, code is %d",
                    record.url, code));
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (process != null) process.destroy();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (tempFile != null && tempFile.exists()) {
            if (!tempFile.delete()) logger.warn("Delete temp file: {} failed", tempFile.getAbsolutePath());
        }
    }

    /**
     * 构建FFMPEG命令
     *
     * @param record URL记录
     * @param tempFile 临时文件地址
     * @return 命令
     */
    private List<String> buildCommands(URLRecord record, String tempFile) {
        List<String> commands = new ArrayList<>();
        commands.add(ffmpeg);
        commands.add("-v");
        commands.add("quiet");
        commands.add("-user_agent");
        commands.add(config.userAgent);
        if (record.isUseProxy() && proxy != null) {
            commands.add("-http_proxy");
            commands.add(proxy.toString());
        }
        commands.add("-i");
        commands.add(record.url);
        commands.add("-c:v");
        commands.add("libopenh264");
        commands.add("-c:a");
        commands.add("aac");
        commands.add(tempFile);
        return commands;
    }

    /**
     * 创建临时目录
     */
    private void createTempDirectory() {
        File directory = new File(config.tempDirectory);
        if (directory.exists()) return;
        if (directory.mkdirs()) logger.info("Create temp directory:{} success", config.tempDirectory);
    }
}
