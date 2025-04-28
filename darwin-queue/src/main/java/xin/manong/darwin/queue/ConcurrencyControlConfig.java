package xin.manong.darwin.queue;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.weapon.spring.boot.etcd.WatchValue;

import java.util.HashMap;
import java.util.Map;

/**
 * 连接并发管理器配置
 *
 * @author frankcl
 * @date 2023-03-09 17:32:32
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.queue.concurrency-control")
public class ConcurrencyControlConfig {

    private static final int DEFAULT_CONCURRENCY_CONNECTIONS = 50;
    private static final int DEFAULT_CONCURRENT_CONNECTION_TTL_SECOND = 3600;

    public int concurrentConnectionTtlSecond = DEFAULT_CONCURRENT_CONNECTION_TTL_SECOND;
    @WatchValue(namespace = "darwin", key = "queue/defaultConcurrency")
    public int defaultConcurrencyConnections = DEFAULT_CONCURRENCY_CONNECTIONS;
    @WatchValue(namespace = "darwin", key = "queue/concurrencyConnectionMap")
    public Map<String, Integer> concurrencyConnectionMap = new HashMap<>();

    @Bean
    public ConcurrencyControl buildConcurrencyControl() {
        return new ConcurrencyControl(concurrencyConnectionMap,
                defaultConcurrencyConnections, concurrentConnectionTtlSecond);
    }
}
