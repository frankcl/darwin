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
