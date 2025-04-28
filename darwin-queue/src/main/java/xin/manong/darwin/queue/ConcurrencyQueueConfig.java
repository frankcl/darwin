package xin.manong.darwin.queue;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 并发队列配置
 *
 * @author frankcl
 * @date 2023-03-08 14:38:05
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.queue.concurrency-queue")
public class ConcurrencyQueueConfig {

    private static final int DEFAULT_MAX_QUEUE_CAPACITY = 5000;
    private static final int DEFAULT_MAX_CONCURRENCY_UNIT_EXPIRED_TIME_SECONDS = 600;
    private static final double DEFAULT_WARNING_MEMORY_USED_RATIO = 0.85d;
    private static final double DEFAULT_DANGER_MEMORY_USED_RATIO = 0.95d;

    public int maxQueueCapacity = DEFAULT_MAX_QUEUE_CAPACITY;
    public double dangerMemoryUsedRatio = DEFAULT_DANGER_MEMORY_USED_RATIO;
    public double waringMemoryUsedRatio = DEFAULT_WARNING_MEMORY_USED_RATIO;

    @Bean
    public ConcurrencyQueue buildConcurrencyQueue() {
        return new ConcurrencyQueue(this);
    }
}
