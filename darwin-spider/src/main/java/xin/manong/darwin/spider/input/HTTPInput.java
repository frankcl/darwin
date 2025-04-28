package xin.manong.darwin.spider.input;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.spider.core.SpiderConfig;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestMethod;
import xin.manong.weapon.base.util.CommonUtil;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * HTTP数据源输入
 *
 * @author frankcl
 * @date 2025-04-11 12:01:27
 */
public class HTTPInput extends Input {

    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String HEADER_REFERER = "Referer";
    private static final String HEADER_HOST = "Host";

    private final URLRecord record;
    private final HttpClient httpClient;
    private final SpiderConfig config;

    private Response httpResponse;

    public HTTPInput(URLRecord record,
                     HttpClient httpClient,
                     SpiderConfig config) {
        this.record = record;
        this.httpClient = httpClient;
        this.config = config;
    }

    @Override
    public void open() throws IOException {
        close();
        HttpRequest.Builder builder = new HttpRequest.Builder();
        HttpRequest httpRequest = builder.requestURL(record.url).method(RequestMethod.GET).build();
        if (!StringUtils.isEmpty(config.userAgent)) httpRequest.headers.put(HEADER_USER_AGENT, config.userAgent);
        if (!StringUtils.isEmpty(record.parentURL)) httpRequest.headers.put(HEADER_REFERER, record.parentURL);
        String host = CommonUtil.getHost(record.url);
        if (!StringUtils.isEmpty(host) && !CommonUtil.isValidIP(host)) httpRequest.headers.put(HEADER_HOST, host);
        if (record.headers != null && !record.headers.isEmpty()) httpRequest.headers.putAll(record.headers);
        if (record.timeout != null && record.timeout > 0) {
            httpRequest.connectTimeoutMs = record.timeout;
            httpRequest.readTimeoutMs = record.timeout;
        }
        httpResponse = httpClient.execute(httpRequest);
        if (httpResponse == null || !httpResponse.isSuccessful()) throw new IOException("获取HTTP响应失败");
        record.httpCode = httpResponse.code();
        String targetURL = httpResponse.request().url().url().toString();
        if (!StringUtils.isEmpty(targetURL) && !targetURL.equals(record.url)) record.redirectURL = targetURL;
        ResponseBody responseBody = httpResponse.body();
        assert responseBody != null;
        record.contentLength = responseBody.contentLength();
        MediaType mediaType = responseBody.contentType();
        if (mediaType != null) {
            Charset charset = mediaType.charset();
            record.mimeType = mediaType.type();
            record.subMimeType = mediaType.subtype();
            record.primitiveCharset = charset == null ? null : charset.name();
        }
        inputStream = responseBody.byteStream();
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (httpResponse != null) {
            httpResponse.close();
            httpResponse = null;
        }
    }
}
