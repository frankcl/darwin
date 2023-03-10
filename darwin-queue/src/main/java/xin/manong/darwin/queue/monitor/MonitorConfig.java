package xin.manong.darwin.queue.monitor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 监控配置
 *
 * @author frankcl
 * @date 2023-03-10 10:31:59
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.queue.monitor")
public class MonitorConfig {

    private static final long DEFAULT_MULTI_QUEUE_MONITOR_CHECK_TIME_INTERVAL_MS = 3600000L;
    private static final long DEFAULT_CONCURRENT_MONITOR_CHECK_TIME_INTERVAL_MS = 600000L;
    private static final long DEFAULT_MULTI_QUEUE_EXPIRED_TIME_INTERVAL_MS = 86400000L;
    private static final long DEFAULT_CONCURRENT_EXPIRED_TIME_INTERVAL_MS = 600000L;

    public long multiQueueMonitorCheckTimeIntervalMs = DEFAULT_MULTI_QUEUE_MONITOR_CHECK_TIME_INTERVAL_MS;
    public long multiQueueExpiredTimeIntervalMs = DEFAULT_MULTI_QUEUE_EXPIRED_TIME_INTERVAL_MS;
    public long concurrentMonitorCheckTimeIntervalMs = DEFAULT_CONCURRENT_MONITOR_CHECK_TIME_INTERVAL_MS;
    public long concurrentExpiredTimeIntervalMs = DEFAULT_CONCURRENT_EXPIRED_TIME_INTERVAL_MS;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public MultiQueueMonitor buildMultiQueueMonitor() {
        return new MultiQueueMonitor(multiQueueMonitorCheckTimeIntervalMs, multiQueueExpiredTimeIntervalMs);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ConcurrentConnectionMonitor buildConcurrentConnectionMonitor() {
        return new ConcurrentConnectionMonitor(concurrentMonitorCheckTimeIntervalMs, concurrentExpiredTimeIntervalMs);
    }
}
