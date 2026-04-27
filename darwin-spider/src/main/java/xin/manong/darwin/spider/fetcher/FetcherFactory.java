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
        int fetchMethod = record.fetchMethod == null ? Constants.FETCH_METHOD_COMMON : record.fetchMethod;
        if (fetcherMap.containsKey(fetchMethod)) return fetcherMap.get(fetchMethod);
        synchronized (this) {
            if (fetcherMap.containsKey(fetchMethod)) return fetcherMap.get(fetchMethod);
            Fetcher<?> fetcher;
            if (fetchMethod == Constants.FETCH_METHOD_RENDER) {
                fetcher = browserFetcher;
                fetcherMap.put(fetchMethod, browserFetcher);
            } else {
                fetcher = httpClientFetcher;
                fetcherMap.put(fetchMethod, httpClientFetcher);
            }
            return fetcher;
        }
    }
}
