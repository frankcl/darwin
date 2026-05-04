package xin.manong.darwin.spider.fetcher;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.model.HTTPRequest;
import xin.manong.darwin.common.model.PostMediaType;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.CookieService;
import xin.manong.darwin.spider.core.SpiderConfig;
import xin.manong.darwin.spider.playwright.FeignBrowser;
import xin.manong.darwin.spider.playwright.FetchRequest;
import xin.manong.darwin.spider.playwright.FetchResponse;

import java.util.Map;

/**
 * 基于浏览器的抓取器
 *
 * @author frankcl
 * @date 2026-04-24 18:04:01
 */
@Component
public class BrowserFetcher extends Fetcher<FetchResponse> {

    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    @Resource
    private SpiderConfig config;
    @Resource
    private CookieService cookieService;
    @Resource
    private FeignBrowser feignBrowser;

    @Override
    public Response<FetchResponse> fetch(URLRecord record) {
        FetchRequest.Builder builder = new FetchRequest.Builder();
        String method = buildRequestMethod(record);
        builder.requestURL(record.url).method(method);
        if (record.navigate != null) builder.navigate(record.navigate);
        Map<String, String> headers = buildDefaultHeaders(config, record, cookieService);
        if (method.equalsIgnoreCase(FetchRequest.METHOD_POST)) {
            builder.requestBody(record.requestBody);
            headers.put(HEADER_CONTENT_TYPE, buildRequestFormat(record));
        }
        if (record.headers != null) headers.putAll(record.headers);
        if (record.timeout != null && record.timeout > 0) builder.timeout(record.timeout);
        builder.headers(headers);
        FetchResponse response = feignBrowser.fetch(builder.build());
        return Response.buildResponse(response);
    }

    /**
     * 构建POST请求格式
     *
     * @return POST请求格式
     */
    private String buildRequestFormat(URLRecord record) {
        if (record.postMediaType == PostMediaType.FORM) return APPLICATION_FORM_URLENCODED;
        return APPLICATION_JSON;
    }

    /**
     * 构建请求方法
     *
     * @return 请求方法
     */
    private String buildRequestMethod(URLRecord record) {
        if (record.httpRequest == HTTPRequest.POST) return FetchRequest.METHOD_POST;
        return FetchRequest.METHOD_GET;
    }
}
