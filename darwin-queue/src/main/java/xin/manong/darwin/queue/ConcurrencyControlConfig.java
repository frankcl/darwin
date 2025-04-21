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

    private static final int DEFAULT_MAX_CONCURRENT_CONNECTION_NUM = 50;
    private static final int DEFAULT_CONCURRENT_CONNECTION_TTL_SECOND = 3600;

    public int concurrentConnectionTtlSecond = DEFAULT_CONCURRENT_CONNECTION_TTL_SECOND;
    @WatchValue(namespace = "darwin", key = "queue/maxConcurrentConnectionNum")
    public int maxConcurrentConnection = DEFAULT_MAX_CONCURRENT_CONNECTION_NUM;
    @WatchValue(namespace = "darwin", key = "queue/maxConcurrentConnectionMap")
    public Map<String, Integer> maxConcurrentConnectionMap = new HashMap<>();

    @Bean
    public ConcurrencyControl buildConcurrentControl() {
        return new ConcurrencyControl(maxConcurrentConnectionMap,
                maxConcurrentConnection, concurrentConnectionTtlSecond);
    }
}
