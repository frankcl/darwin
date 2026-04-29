package xin.manong.darwin.spider.fetcher;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抓取器工厂
 *
 * @author frankcl
 * @date 2026-04-27 13:17:27
 */
@Component
public class FetcherFactory {

    @Resource
    private HttpClientFetcher httpClientFetcher;
    @Resource
    private BrowserFetcher browserFetcher;
    private final Map<Integer, Fetcher<?>> fetcherMap;

    public FetcherFactory() {
        fetcherMap = new ConcurrentHashMap<>();
    }

    /**
     * 根据数据获取抓取器
     *
     * @param record 数据
     * @return 抓取器
     */
    public Fetcher<?> getFetcher(URLRecord record) {
        int fetcherType = record.fetcherType == null ? Constants.FETCHER_TYPE_HTTP_CLIENT : record.fetcherType;
        if (fetcherMap.containsKey(fetcherType)) return fetcherMap.get(fetcherType);
        synchronized (this) {
            if (fetcherMap.containsKey(fetcherType)) return fetcherMap.get(fetcherType);
            Fetcher<?> fetcher;
            if (fetcherType == Constants.FETCHER_TYPE_BROWSER) {
                fetcher = browserFetcher;
                fetcherMap.put(fetcherType, browserFetcher);
            } else {
                fetcher = httpClientFetcher;
                fetcherMap.put(fetcherType, httpClientFetcher);
            }
            return fetcher;
        }
    }
}
