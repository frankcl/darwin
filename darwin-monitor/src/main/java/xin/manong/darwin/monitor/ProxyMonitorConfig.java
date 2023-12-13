package xin.manong.darwin.monitor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多级队列监控配置
 *
 * @author frankcl
 * @date 2023-03-10 10:31:59
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.monitor.proxy")
public class ProxyMonitorConfig {

    private static final long DEFAULT_CHECK_TIME_INTERVAL_MS = 300000;

    public long checkTimeIntervalMs = DEFAULT_CHECK_TIME_INTERVAL_MS;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ProxyMonitor buildProxyMonitor() {
        return new ProxyMonitor(checkTimeIntervalMs);
    }
}
