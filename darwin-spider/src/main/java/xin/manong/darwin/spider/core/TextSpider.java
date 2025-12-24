package xin.manong.darwin.spider.core;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.MediaType;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.spider.input.ByteArrayInput;
import xin.manong.darwin.spider.input.HTTPInput;
import xin.manong.darwin.spider.input.Input;
import xin.manong.darwin.spider.input.OSSInput;
import xin.manong.darwin.spider.output.ByteArrayOutput;
import xin.manong.weapon.base.common.Context;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文本数据爬虫
 *
 * @author frankcl
 * @date 2025-04-27 20:00:39
 */
@Component
public class TextSpider extends Spider {

    private static final Logger logger = LoggerFactory.getLogger(TextSpider.class);

    private static final String M3U8_MARK_START = "#EXTM3U";
    private static final String M3U8_MARK_END = "#EXT-X-ENDLIST";

    @Resource
    private HttpClientFactory httpClientFactory;
    @Resource
    private TextParser textParser;

    @Override
    public MediaType handle(URLRecord record, Input input, Context context) throws IOException {
        assert input != null;
        record.contentType = Constants.CONTENT_TYPE_PAGE;
        if (input instanceof HTTPInput) {
            record.text = fetch(record, (HTTPInput) input, context);
            if (checkM3U8(record)) return MediaType.STREAM_M3U8;
        } else {
            record.text = read(record, (OSSInput) input, context);
        }
        try (Input byteArrayInput = new ByteArrayInput(record.text.getBytes(StandardCharsets.UTF_8))){
            writer.write(record, byteArrayInput, context);
            textParser.parse(record, context);
        }
        return MediaType.UNKNOWN;
    }

    @Override
    public List<MediaType> supportedMediaTypes() {
        return List.of(MediaType.TEXT_HTML, MediaType.TEXT_PLAIN, MediaType.APPLICATION_XHTML,
                MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.TEXT_CSS, MediaType.TEXT_CSV,
                MediaType.TEXT_JAVASCRIPT, MediaType.APPLICATION_JAVASCRIPT, MediaType.APPLICATION_X_JAVASCRIPT,
                MediaType.APPLICATION_XML);
    }

    /**
     * 抓取URL
     *
     * @param record URL数据
     * @throws IOException I/O异常
     */
    public void fetch(URLRecord record) throws IOException {
        HTTPInput input = new HTTPInput(record, httpClientFactory.getHttpClient(record), spiderConfig);
        input.open();
        if (record.mediaType == null || (!record.mediaType.isText() &&
                !supportedMediaTypes().contains(record.mediaType))) {
            throw new IOException("不支持的媒体类型：" + record.mediaType);
        }
        record.text = fetch(record, input, null);
    }

    /**
     * 抓取URL
     *
     * @param fetchURL 抓取URL
     * @return URL数据
     * @throws Exception 异常
     */
    public URLRecord fetch(String fetchURL) throws Exception {
        URLRecord record = new URLRecord(fetchURL);
        fetch(record);
        return record;
    }

    /**
     * 检测是否为M3U8资源
     *
     * @param record 数据
     * @return 满足条件返回true，否则返回false
     */
    private boolean checkM3U8(URLRecord record) {
        if (!record.mediaType.equals(MediaType.TEXT_PLAIN) || record.text == null) return false;
        String[] textLines = record.text.split("\n");
        return textLines[0].equals(M3U8_MARK_START) && textLines[textLines.length - 1].equals(M3U8_MARK_END);
    }

    /**
     * 从OSS读取数据
     *
     * @param record 数据
     * @param input OSS输入源
     * @param context 上下文
     * @return 文本字符串
     * @throws IOException I/O异常
     */
    private String read(URLRecord record, OSSInput input, Context context) throws IOException {
        long startTime = System.currentTimeMillis();
        try (ByteArrayOutput byteArrayOutput = new ByteArrayOutput()) {
            if (!input.isOpened()) input.open();
            input.transport(byteArrayOutput);
            byte[] byteArray = byteArrayOutput.getBytes();
            if (record.contentLength == null || record.contentLength == -1) {
                record.contentLength = (long) byteArray.length;
            }
            return new String(byteArray, record.charset);
        } finally {
            if (context != null) context.put(Constants.DARWIN_READ_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 抓取数据
     *
     * @param record 数据
     * @param input HTTP数据输入源
     * @param context 上下文
     * @return 文本字符串
     * @throws IOException 异常
     */
    private String fetch(URLRecord record, HTTPInput input, Context context) throws IOException {
        long startTime = System.currentTimeMillis();
        try (ByteArrayOutput byteArrayOutput = new ByteArrayOutput()) {
            if (!input.isOpened()) input.open();
            if (record.contentLength > spiderConfig.maxContentLength) throw new IllegalStateException("内容长度超过最大限制");
            input.transport(byteArrayOutput);
            byte[] byteArray = byteArrayOutput.getBytes();
            if (record.contentLength == -1) record.contentLength = (long) byteArray.length;
            record.charset = speculateCharset(byteArray, record);
            return new String(byteArray, Charset.forName(record.charset));
        } finally {
            record.downTime = System.currentTimeMillis() - startTime;
            if (context != null) context.put(Constants.DARWIN_DOWN_TIME, record.downTime);
        }
    }

    /**
     * 推测字符集，顺序如下
     * 1. 使用HTTP response header中设定字符集
     * 2. 使用HTML header中设定字符集
     * 3. 猜测HTML字符集
     * 4. 以上都失败，使用UTF-8字符集
     *
     * @param byteArray HTML字节数组
     * @param record URL数据
     * @return 字符集
     */
    private String speculateCharset(byte[] byteArray, URLRecord record) {
        if (record.mediaType != null && StringUtils.isNotEmpty(
                record.mediaType.charset)) {
            return record.mediaType.charset;
        }
        String charset = HTMLCharsetParser.parse(byteArray);
        if (StringUtils.isNotEmpty(charset)) record.htmlCharset = charset;
        if (StringUtils.isEmpty(charset)) charset = CharsetSpeculator.speculate(byteArray, 0, 1024);
        if (StringUtils.isNotEmpty(charset)) return charset;
        logger.warn("Speculate charset failed, using UTF-8 charset for url: {}", record.url);
        return StandardCharsets.UTF_8.name();
    }
}
