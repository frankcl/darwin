package xin.manong.darwin.queue.concurrent;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 连接并发管理器配置
 *
 * @author frankcl
 * @date 2023-03-09 17:32:32
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.queue.concurrent")
public class ConcurrentManagerConfig {

    private static final int DEFAULT_MAX_CONCURRENT_CONNECTION_NUM = 50;
    private static final int DEFAULT_CONCURRENT_CONNECTION_TTL_SECOND = 3600;
    private static final long DEFAULT_MAX_UPDATE_EXPIRED_TIME_INTERVAL_MS = 600000L;

    public int maxConcurrentConnectionNum = DEFAULT_MAX_CONCURRENT_CONNECTION_NUM;
    public int concurrentConnectionTtlSecond = DEFAULT_CONCURRENT_CONNECTION_TTL_SECOND;
    public long maxUpdateExpiredTimeIntervalMs = DEFAULT_MAX_UPDATE_EXPIRED_TIME_INTERVAL_MS;

    @Bean
    public ConcurrentManager buildConcurrentManager() {
        return new ConcurrentManager(maxConcurrentConnectionNum,
                concurrentConnectionTtlSecond, maxUpdateExpiredTimeIntervalMs);
    }
}
