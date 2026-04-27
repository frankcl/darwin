package xin.manong.darwin.spider.input;

import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.spider.fetcher.Fetcher;
import xin.manong.darwin.spider.fetcher.Response;

import java.io.IOException;

/**
 * HTTP数据源输入
 *
 * @author frankcl
 * @date 2025-04-11 12:01:27
 */
public class HTTPInput extends Input {

    private final URLRecord record;
    private final Fetcher<?> fetcher;

    private Response<?> response;

    public HTTPInput(URLRecord record,
                     Fetcher<?> fetcher) {
        this.record = record;
        this.fetcher = fetcher;
    }

    @Override
    public void open() throws IOException {
        close();
        response = fetcher.fetch(record);
        record.httpCode = response.getHttpCode();
        if (!response.isStatus()) {
            response.close();
            throw new IOException(String.format("执行HTTP请求失败，http状态码：%d", record.httpCode));
        }
        String targetURL = response.getUrl();
        if (!StringUtils.isEmpty(targetURL) && !targetURL.equals(record.url)) record.redirectURL = targetURL;
        if (response.getHeaders() != null) record.responseHeaders = response.getHeaders();
        record.contentLength = response.getContentLength();
        record.mediaType = response.getMediaType();
        inputStream = response.getResponseBody();
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (response != null) {
            response.close();
            response = null;
        }
    }
}
