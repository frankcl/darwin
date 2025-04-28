package xin.manong.darwin.spider.core;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.spider.proxy.SpiderProxySelector;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpClientConfig;
import xin.manong.weapon.base.http.HttpProxyAuthenticator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP client工厂
 *
 * @author frankcl
 * @date 2025-04-12 13:36:29
 */
@Component
public class HttpClientFactory {

    @Resource
    private SpiderConfig spiderConfig;
    @Resource(name = "longProxySelector")
    private SpiderProxySelector longProxySelector;
    @Resource(name = "shortProxySelector")
    private SpiderProxySelector shortProxySelector;
    private final HttpProxyAuthenticator authenticator;
    private final Map<Integer, HttpClient> httpClientMap;

    public HttpClientFactory() {
        authenticator = new HttpProxyAuthenticator();
        httpClientMap = new ConcurrentHashMap<>();
    }

    /**
     * 根据URL抓取方式获取HTTPClient
     * 0. 本地IP HttpClient
     * 1. 长效代理HttpClient
     * 2. 短效代理HttpClient
     *
     * @param record URL数据
     * @return HttpClient
     */
    public HttpClient getHttpClient(URLRecord record) {
        int fetchMethod = record.fetchMethod == null ? Constants.FETCH_METHOD_COMMON : record.fetchMethod;
        if (httpClientMap.containsKey(fetchMethod)) return httpClientMap.get(fetchMethod);
        synchronized (this) {
            if (httpClientMap.containsKey(fetchMethod)) return httpClientMap.get(fetchMethod);
            HttpClientConfig httpClientConfig = new HttpClientConfig();
            httpClientConfig.connectTimeoutSeconds = spiderConfig.connectTimeoutSeconds;
            httpClientConfig.readTimeoutSeconds = spiderConfig.readTimeoutSeconds;
            httpClientConfig.keepAliveMinutes = spiderConfig.keepAliveMinutes;
            httpClientConfig.maxIdleConnections = spiderConfig.maxIdleConnections;
            httpClientConfig.retryCnt = spiderConfig.retryCnt;
            HttpClient httpClient;
            if (fetchMethod == Constants.FETCH_METHOD_LONG_PROXY) {
                httpClient = new HttpClient(httpClientConfig, longProxySelector, authenticator);
            } else if (fetchMethod == Constants.FETCH_METHOD_SHORT_PROXY) {
                httpClient = new HttpClient(httpClientConfig, shortProxySelector, authenticator);
            } else {
                httpClient = new HttpClient(httpClientConfig);
            }
            httpClientMap.put(fetchMethod, httpClient);
            return httpClient;
        }
    }
}
