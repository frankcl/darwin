package xin.manong.darwin.queue;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多级队列配置
 *
 * @author frankcl
 * @date 2023-03-08 14:38:05
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.queue")
public class MultiQueueConfig {

    private static final long DEFAULT_MONITOR_CHECK_TIME_INTERVAL_MS = 3600000L;
    private static final long DEFAULT_EXPIRED_TIME_INTERVAL_MS = 86400000L;
    private static final double DEFAULT_MAX_USED_MEMORY_RATIO = 0.95d;

    public double maxUsedMemoryRatio = DEFAULT_MAX_USED_MEMORY_RATIO;
    public long monitorCheckTimeIntervalMs = DEFAULT_MONITOR_CHECK_TIME_INTERVAL_MS;
    public long expiredTimeIntervalMs = DEFAULT_EXPIRED_TIME_INTERVAL_MS;

    @Bean
    public MultiQueue buildMultiQueue() {
        return new MultiQueue(maxUsedMemoryRatio);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public MultiQueueMonitor buildMultiQueueMonitor() {
        return new MultiQueueMonitor(monitorCheckTimeIntervalMs, expiredTimeIntervalMs);
    }
}
