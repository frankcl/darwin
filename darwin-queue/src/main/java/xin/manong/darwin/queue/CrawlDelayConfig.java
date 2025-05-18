package xin.manong.darwin.queue;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import xin.manong.weapon.spring.boot.etcd.WatchValue;

import java.util.HashMap;
import java.util.Map;

/**
 * 抓取间隔配置
 *
 * @author frankcl
 * @date 2023-03-09 17:32:32
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.queue.crawl-delay")
public class CrawlDelayConfig {

    private static final long DEFAULT_CRAWL_DELAY_MS = 1000L;

    @WatchValue(namespace = "darwin", key = "queue/defaultCrawlDelay")
    public long defaultCrawlDelayMs = DEFAULT_CRAWL_DELAY_MS;
    @WatchValue(namespace = "darwin", key = "queue/crawlDelayMap")
    public Map<String, Long> crawlDelayMap = new HashMap<>();
}
