package xin.manong.darwin.spider;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 抓取内容资源
 *
 * @author frankcl
 * @date 2023-08-22 15:24:58
 */
public class SpiderResource implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(SpiderResource.class);

    /**
     * 是否需要猜测编码
     */
    public boolean guessCharset;
    /**
     * URL记录
     */
    public URLRecord record;
    /**
     * 资源编码
     */
    public Charset charset;
    /**
     * 资源输入流
     */
    public InputStream inputStream;
    /**
     * HTTP响应对象
     */
    private Response httpResponse;

    private SpiderResource(boolean guessCharset) {
        this.guessCharset = guessCharset;
    }

    /**
     * 构建爬虫资源
     *
     * @param tempFile 本地临时文件
     * @return 爬虫资源
     * @throws FileNotFoundException 如果本地文件不存在抛出此异常
     */
    public static SpiderResource buildFrom(String tempFile) throws FileNotFoundException {
        SpiderResource spiderResource = new SpiderResource(false);
        spiderResource.inputStream = new FileInputStream(tempFile);
        spiderResource.record = new URLRecord();
        spiderResource.record.status = Constants.URL_STATUS_SUCCESS;
        spiderResource.record.fetchTime = System.currentTimeMillis();
        spiderResource.record.mimeType = Spider.MIME_TYPE_VIDEO;
        spiderResource.record.subMimeType = Spider.SUB_MIME_TYPE_MP4;
        spiderResource.record.httpCode = Spider.HTTP_CODE_OK;
        return spiderResource;
    }

    /**
     * 构建爬虫资源
     *
     * @param record 数据
     * @param ossClient oss客户端
     * @return 成功返回爬虫资源，否则返回null
     */
    public static SpiderResource buildFrom(URLRecord record, OSSClient ossClient) {
        if (StringUtils.isEmpty(record.fetchContentURL)) return null;
        OSSMeta ossMeta = OSSClient.parseURL(record.fetchContentURL);
        if (ossMeta == null || !ossClient.exist(ossMeta.bucket, ossMeta.key)) return null;
        InputStream inputStream = ossClient.getObjectStream(ossMeta.bucket, ossMeta.key);
        if (inputStream == null) return null;
        SpiderResource spiderResource = new SpiderResource(false);
        spiderResource.inputStream = inputStream;
        spiderResource.charset = StandardCharsets.UTF_8;
        spiderResource.record = record;
        return spiderResource;
    }

    /**
     * 构建爬虫资源
     *
     * @param requestURL 请求URL
     * @param httpResponse HTTP响应
     * @return 成功返回爬虫资源，否则返回null
     */
    public static SpiderResource buildFrom(String requestURL, Response httpResponse) {
        SpiderResource spiderResource = new SpiderResource(true);
        spiderResource.record = new URLRecord();
        spiderResource.httpResponse = httpResponse;
        spiderResource.record.status = Constants.URL_STATUS_FETCH_FAIL;
        spiderResource.record.fetchTime = System.currentTimeMillis();
        if (httpResponse == null) return spiderResource;
        spiderResource.record.httpCode = httpResponse.code();
        if (!httpResponse.isSuccessful()) return spiderResource;
        spiderResource.record.status = Constants.URL_STATUS_SUCCESS;
        String targetURL = httpResponse.request().url().url().toString();
        if (!StringUtils.isEmpty(targetURL) && !targetURL.equals(requestURL)) {
            spiderResource.record.redirectURL = targetURL;
        }
        ResponseBody responseBody = httpResponse.body();
        assert responseBody != null;
        spiderResource.inputStream = responseBody.byteStream();
        MediaType mediaType = responseBody.contentType();
        if (mediaType == null) return spiderResource;
        spiderResource.record.mimeType = mediaType.type();
        spiderResource.record.subMimeType = mediaType.subtype();
        spiderResource.charset = mediaType.charset();
        return spiderResource;
    }

    /**
     * 拷贝抓取结果
     *
     * @param record URL数据
     */
    public void copyTo(URLRecord record) {
        if (this.record == null) return;
        record.status = this.record.status;
        record.fetchTime = this.record.fetchTime;
        record.redirectURL = this.record.redirectURL;
        record.mimeType = this.record.mimeType;
        record.subMimeType = this.record.subMimeType;
        record.httpCode = this.record.httpCode;
    }

    /**
     * 销毁抓取资源
     */
    @Override
    public void close() throws IOException {
        try {
            if (inputStream != null) inputStream.close();
            if (httpResponse != null) httpResponse.close();
        } catch (Exception e) {
            logger.error("close spider resource failed");
            logger.error(e.getMessage(), e);
        }
    }
}
