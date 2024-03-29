package xin.manong.darwin.queue.multi;

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
@ConfigurationProperties(prefix = "app.queue.multi")
public class MultiQueueConfig {

    private static final int DEFAULT_MAX_QUEUE_SIZE = 5000;
    private static final int DEFAULT_MAX_CONCURRENT_UNIT_EXPIRED_TIME_SECONDS = 600;
    private static final double DEFAULT_WARN_USED_MEMORY_RATIO = 0.9d;
    private static final double DEFAULT_MAX_USED_MEMORY_RATIO = 0.95d;

    public int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
    public int maxConcurrentUnitExpiredTimeSeconds = DEFAULT_MAX_CONCURRENT_UNIT_EXPIRED_TIME_SECONDS;
    public double maxUsedMemoryRatio = DEFAULT_MAX_USED_MEMORY_RATIO;
    public double warnUsedMemoryRatio = DEFAULT_WARN_USED_MEMORY_RATIO;

    @Bean
    public MultiQueue buildMultiQueue() {
        return new MultiQueue(this);
    }
}
