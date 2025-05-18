package xin.manong.darwin.queue;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 抓取间隔控制器
 *
 * @author frankcl
 * @date 2025-05-16 10:09:57
 */
@Component
public class CrawlDelayControl {

    private static final Logger logger = LoggerFactory.getLogger(CrawlDelayControl.class);

    @Resource
    private CrawlDelayConfig config;
    private final ThreadLocal<Cache<String, Long>> concurrencyFetchCache;

    public CrawlDelayControl() {
        concurrencyFetchCache = new ThreadLocal<>();
    }

    /**
     * 记录并发单元抓取时间
     *
     * @param concurrencyUnit 并发单元
     */
    public void put(String concurrencyUnit) {
        if (concurrencyFetchCache.get() == null) {
            CacheBuilder<String, Long> builder = CacheBuilder.newBuilder()
                    .concurrencyLevel(1)
                    .maximumSize(200)
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .removalListener(this::onRemoval);
            concurrencyFetchCache.set(builder.build());
        }
        concurrencyFetchCache.get().put(concurrencyUnit, System.currentTimeMillis());
    }

    /**
     * 抓取时间移除
     *
     * @param notification 移除通知
     */
    private void onRemoval(RemovalNotification<String, Long> notification) {
        logger.debug("Fetch cache expired for concurrency:{}, cause:{}",
                notification.getKey(), notification.getCause().name());
    }

    /**
     * 延迟抓取
     * 查看并发单元最近抓取时间
     * 如果时间间隔小于抓取间隔则延迟抓取
     * 否则不做任何操作返回
     *
     * @param concurrencyUnit 并发单元
     */
    public void delay(String concurrencyUnit) {
        Cache<String, Long> fetchCache = concurrencyFetchCache.get();
        if (fetchCache == null) return;
        Long fetchTime = fetchCache.getIfPresent(concurrencyUnit);
        long currentTime = System.currentTimeMillis();
        long interval = fetchTime == null ? currentTime : currentTime - fetchTime;
        long crawlDelay = getCrawlDelay(concurrencyUnit);
        if (interval >= crawlDelay) return;
        logger.info("Crawl delay limit {} ms for concurrency:{}", crawlDelay - interval, concurrencyUnit);
        try {
            Thread.sleep(crawlDelay - interval);
        } catch (InterruptedException e) {
            logger.warn("Crawl delay interrupted", e);
        }
    }

    /**
     * 获取并发单元抓取间隔
     *
     * @param concurrencyUnit 并发单元
     * @return 抓取间隔
     */
    public long getCrawlDelay(String concurrencyUnit) {
        Long crawlDelay = config.crawlDelayMap.get(concurrencyUnit);
        return crawlDelay == null ? config.defaultCrawlDelayMs : crawlDelay;
    }
}
