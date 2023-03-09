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

    private static final int DEFAULT_CONCURRENT_CONNECTION_NUM = 50;

    public int concurrentConnectionNum = DEFAULT_CONCURRENT_CONNECTION_NUM;

    @Bean
    public ConcurrentManager buildConcurrentManager() {
        return new ConcurrentManager(concurrentConnectionNum);
    }
}
