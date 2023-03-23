package xin.manong.darwin.monitor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 并发连接监控配置
 *
 * @author frankcl
 * @date 2023-03-10 10:31:59
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.monitor.concurrent")
public class ConcurrentConnectionMonitorConfig {

    private static final long DEFAULT_CHECK_TIME_INTERVAL_MS = 60000L;
    private static final long DEFAULT_EXPIRED_TIME_INTERVAL_MS = 600000L;

    public long checkTimeIntervalMs = DEFAULT_CHECK_TIME_INTERVAL_MS;
    public long expiredTimeIntervalMs = DEFAULT_EXPIRED_TIME_INTERVAL_MS;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ConcurrentConnectionMonitor buildConcurrentConnectionMonitor() {
        return new ConcurrentConnectionMonitor(checkTimeIntervalMs, expiredTimeIntervalMs);
    }
}
