package xin.manong.darwin.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.darwin.service.component.RefreshProxyKeeper;
import xin.manong.darwin.service.iface.ProxyService;

/**
 * 服务层配置
 *
 * @author frankcl
 * @date 2022-08-24 13:04:15
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.service")
public class ServiceConfig {

    private static final Long DEFAULT_REFRESH_PROXY_TIME_INTERVAL_MS = 300000L;

    public Long refreshProxyTimeIntervalMs = DEFAULT_REFRESH_PROXY_TIME_INTERVAL_MS;
    public String jobTable;
    public String jobIndexName;
    public String urlTable;
    public String urlIndexName;
    public String jobTopic;
    public String recordTopic;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RefreshProxyKeeper buildRefreshProxyKeeper(ProxyService proxyService) {
        return new RefreshProxyKeeper(proxyService, refreshProxyTimeIntervalMs);
    }
}
