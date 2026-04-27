package xin.manong.darwin.spider.fetcher;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.model.HTTPRequest;
import xin.manong.darwin.common.model.PostMediaType;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.CookieService;
import xin.manong.darwin.spider.core.HttpClientFactory;
import xin.manong.darwin.spider.core.SpiderConfig;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;
import xin.manong.weapon.base.http.RequestMethod;

import java.io.IOException;

/**
 * 基于HttpClient抓取器
 *
 * @author frankcl
 * @date 2026-04-23 09:49:23
 */
@Component
public class HttpClientFetcher extends Fetcher<okhttp3.Response> {

    @Resource
    private SpiderConfig config;
    @Resource
    private CookieService cookieService;
    @Resource
    private HttpClientFactory httpClientFactory;

    @Override
    public Response<okhttp3.Response> fetch(URLRecord record) throws IOException {
        HttpRequest.Builder builder = new HttpRequest.Builder();
        RequestMethod requestMethod = buildRequestMethod(record);
        builder.requestURL(record.url).method(requestMethod);
        if (requestMethod == RequestMethod.POST) builder.params(record.requestBody).format(buildRequestFormat(record));
        HttpRequest httpRequest = builder.build();
        httpRequest.headers.putAll(buildDefaultHeaders(config, record, cookieService));
        if (record.headers != null) httpRequest.headers.putAll(record.headers);
        if (record.timeout != null && record.timeout > 0) {
            httpRequest.connectTimeoutMs = httpRequest.readTimeoutMs = record.timeout;
        }
        HttpClient httpClient = httpClientFactory.getHttpClient(record);
        okhttp3.Response httpResponse = httpClient.execute(httpRequest);
        return Response.buildResponse(httpResponse);
    }

    /**
     * 构建POST请求格式
     *
     * @return POST请求格式
     */
    private RequestFormat buildRequestFormat(URLRecord record) {
        if (record.postMediaType == PostMediaType.FORM) return RequestFormat.FORM;
        return RequestFormat.JSON;
    }

    /**
     * 构建请求方法
     *
     * @return 请求方法
     */
    private RequestMethod buildRequestMethod(URLRecord record) {
        if (record.httpRequest == HTTPRequest.POST) return RequestMethod.POST;
        return RequestMethod.GET;
    }
}
