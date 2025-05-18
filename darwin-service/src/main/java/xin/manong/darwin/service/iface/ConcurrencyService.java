package xin.manong.darwin.service.iface;

import java.util.Map;

/**
 * 并发管理服务
 * 管理并发配置
 *
 * @author frankcl
 * @date 2025-04-22 17:12:49
 */
public interface ConcurrencyService {

    /**
     * 获取默认抓取间隔
     *
     * @return 默认抓取间隔
     */
    long defaultCrawlDelay();

    /**
     * 设置默认抓取间隔
     *
     * @param crawlDelay 默认抓取间隔
     */
    void defaultCrawlDelay(long crawlDelay);

    /**
     * 获取抓取间隔配置
     *
     * @return 抓取间隔配置
     */
    Map<String, Long> crawlDelayMap();

    /**
     * 设置抓取间隔配置
     *
     * @param crawlDelayMap 抓取间隔配置
     */
    void crawlDelayMap(Map<String, Long> crawlDelayMap);

    /**
     * 获取默认并发数
     *
     * @return 默认并发数
     */
    int defaultConcurrency();

    /**
     * 设置默认并发数
     *
     * @param concurrency 默认并发数
     */
    void defaultConcurrency(int concurrency);

    /**
     * 获取并发连接配置
     *
     * @return 并发连接配置
     */
    Map<String, Integer> concurrencyConnectionMap();

    /**
     * 设置并发连接配置
     *
     * @param concurrencyConnectionMap 并发连接配置
     */
    void concurrencyConnectionMap(Map<String, Integer> concurrencyConnectionMap);
}
