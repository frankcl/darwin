package xin.manong.darwin.spider.fetcher;

import jakarta.annotation.Resource;
import xin.manong.darwin.spider.core.HttpClientFactory;

/**
 * @author frankcl
 * @date 2026-04-23 09:49:23
 */
public class HttpClientFetcher implements Fetcher<okhttp3.Response> {

    @Resource
    private HttpClientFactory httpClientFactory;

    @Override
    public Response<okhttp3.Response> fetch(Request request) {
        return null;
    }
}
