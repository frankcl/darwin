package xin.manong.darwin.service.impl;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import xin.manong.darwin.service.iface.ConcurrencyService;
import xin.manong.weapon.base.etcd.EtcdClient;

import java.util.Map;

/**
 * 并发连接服务实现
 *
 * @author frankcl
 * @date 2025-04-22 17:19:04
 */
@Service
public class ConcurrencyServiceImpl implements ConcurrencyService {

    private static final String DEFAULT_CRAWL_DELAY = "darwin/queue/defaultCrawlDelay";
    private static final String CRAWL_DELAY_MAP = "darwin/queue/crawlDelayMap";
    private static final String DEFAULT_CONCURRENCY = "darwin/queue/defaultConcurrency";
    private static final String CONCURRENCY_CONNECTION_MAP = "darwin/queue/concurrencyConnectionMap";

    @Resource
    private EtcdClient etcdClient;

    @Override
    public long defaultCrawlDelay() {
        Long v = etcdClient.get(DEFAULT_CRAWL_DELAY, Long.class);
        return v == null ? 1000L : v;
    }

    @Override
    public void defaultCrawlDelay(long crawlDelay) {
        if (crawlDelay <= 0) throw new IllegalArgumentException("默认抓取间隔必须大于0");
        if (!etcdClient.put(DEFAULT_CRAWL_DELAY, String.valueOf(crawlDelay))) {
            throw new RuntimeException("更新默认抓取间隔失败");
        }
    }

    @Override
    public Map<String, Long> crawlDelayMap() {
        Map<String, Long> map = etcdClient.getMap(CRAWL_DELAY_MAP, String.class, Long.class);
        return map == null ? Map.of() : map;
    }

    @Override
    public void crawlDelayMap(Map<String, Long> crawlDelayMap) {
        if (!etcdClient.put(CRAWL_DELAY_MAP, JSON.toJSONString(crawlDelayMap))) {
            throw new RuntimeException("更新抓取间隔配置失败");
        }
    }

    @Override
    public int defaultConcurrency() {
        Integer v = etcdClient.get(DEFAULT_CONCURRENCY, Integer.class);
        return v == null ? 5 : v;
    }

    @Override
    public void defaultConcurrency(int concurrency) {
        if (concurrency <= 0) throw new IllegalArgumentException("默认并发连接必须大于0");
        if (!etcdClient.put(DEFAULT_CONCURRENCY, String.valueOf(concurrency))) {
            throw new RuntimeException("更新默认并发连接失败");
        }
    }

    @Override
    public Map<String, Integer> concurrencyConnectionMap() {
        Map<String, Integer> map = etcdClient.getMap(CONCURRENCY_CONNECTION_MAP, String.class, Integer.class);
        return map == null ? Map.of() : map;
    }

    @Override
    public void concurrencyConnectionMap(@NotNull Map<String, Integer> concurrencyConnectionMap) {
        if (!etcdClient.put(CONCURRENCY_CONNECTION_MAP, JSON.toJSONString(concurrencyConnectionMap))) {
            throw new RuntimeException("更新并发连接配置失败");
        }
    }
}
