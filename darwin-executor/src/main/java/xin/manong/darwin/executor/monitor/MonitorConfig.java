package xin.manong.darwin.executor.monitor;

import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.darwin.executor.GlobalExecutorRegistry;
import xin.manong.darwin.executor.GlobalExecutor;
import xin.manong.darwin.service.iface.ExecutorService;
import xin.manong.weapon.base.etcd.EtcdClient;

/**
 * 监控器配置
 *
 * @author frankcl
 * @date 2023-03-10 10:31:59
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.executor.monitor")
public class MonitorConfig {

    private static final long DEFAULT_CONCURRENT_EXECUTE_TIME_INTERVAL_MS = 60000L;
    private static final long DEFAULT_CONCURRENT_EXPIRED_TIME_INTERVAL_MS = 300000L;
    private static final long DEFAULT_MULTI_QUEUE_EXECUTE_TIME_INTERVAL_MS = 3600000L;
    private static final long DEFAULT_MULTI_QUEUE_EXPIRED_TIME_INTERVAL_MS = 86400000L;
    private static final long DEFAULT_PROXY_EXECUTE_TIME_INTERVAL_MS = 300000L;

    public long concurrentExecuteTimeIntervalMs = DEFAULT_CONCURRENT_EXECUTE_TIME_INTERVAL_MS;
    public long concurrentExpiredTimeIntervalMs = DEFAULT_CONCURRENT_EXPIRED_TIME_INTERVAL_MS;
    public long multiQueueExecuteTimeIntervalMs = DEFAULT_MULTI_QUEUE_EXECUTE_TIME_INTERVAL_MS;
    public long multiQueueExpiredTimeIntervalMs = DEFAULT_MULTI_QUEUE_EXPIRED_TIME_INTERVAL_MS;
    public long proxyExecuteTimeIntervalMs = DEFAULT_PROXY_EXECUTE_TIME_INTERVAL_MS;

    @Resource
    private ExecutorService executorService;
    @Resource
    private GlobalExecutorRegistry registry;
    @Resource
    private EtcdClient etcdClient;

    @Bean
    public MultiQueueMonitor buildMultiQueueMonitor() {
        MultiQueueMonitor monitor = new MultiQueueMonitor(
                multiQueueExecuteTimeIntervalMs, multiQueueExpiredTimeIntervalMs);
        registry.register(new GlobalExecutor(
                GlobalExecutor.LOCK_KEY_MULTI_QUEUE_MONITOR, monitor, executorService, etcdClient));
        return monitor;
    }

    @Bean
    public ConcurrentConnectionMonitor buildConcurrentConnectionMonitor() {
        ConcurrentConnectionMonitor monitor = new ConcurrentConnectionMonitor(
                concurrentExecuteTimeIntervalMs, concurrentExpiredTimeIntervalMs);
        registry.register(new GlobalExecutor(
                GlobalExecutor.LOCK_KEY_CONCURRENT_CONNECTION_MONITOR, monitor, executorService, etcdClient));
        return monitor;
    }

    @Bean
    public ProxyMonitor buildProxyMonitor() {
        ProxyMonitor monitor = new ProxyMonitor(proxyExecuteTimeIntervalMs);
        registry.register(new GlobalExecutor(
                GlobalExecutor.LOCK_KEY_PROXY_MONITOR, monitor, executorService, etcdClient));
        return monitor;
    }
}
