package xin.manong.darwin.spider;

import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.weapon.base.log.JSONLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * 爬虫配置信息
 *
 * @author frankcl
 * @date 2023-03-24 14:48:53
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.spider")
public class SpiderConfig {

    private static final int DEFAULT_KEEP_ALIVE_MINUTES = 3;
    private static final int DEFAULT_MAX_IDLE_CONNECTIONS = 100;
    private static final int DEFAULT_RETRY_CNT = 3;
    private static final long DEFAULT_REUSE_EXPIRED_TIME_MS = 86400 * 1000L;
    private static final long DEFAULT_CONNECT_TIMEOUT_SECONDS = 5L;
    private static final long DEFAULT_READ_TIMEOUT_SECONDS = 10L;
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36";

    public long reuseExpiredTimeMs = DEFAULT_REUSE_EXPIRED_TIME_MS;
    public long connectTimeoutSeconds = DEFAULT_CONNECT_TIMEOUT_SECONDS;
    public long readTimeoutSeconds = DEFAULT_READ_TIMEOUT_SECONDS;
    public int keepAliveMinutes = DEFAULT_KEEP_ALIVE_MINUTES;
    public int maxIdleConnections = DEFAULT_MAX_IDLE_CONNECTIONS;
    public int retryCnt = DEFAULT_RETRY_CNT;
    public String userAgent = DEFAULT_USER_AGENT;
    public String contentRegion;
    public String contentBucket;
    public String contentDirectory;
    public String tempDirectory;

    @Resource(name = "urlAspectLogger")
    protected JSONLogger aspectLogger;

    /**
     * 构建长效代理选择器
     *
     * @param proxyService 代理服务
     * @return 长效代理选择器
     */
    @Bean(name = "spiderLongProxySelector")
    public SpiderProxySelector buildSpiderLongProxySelector(ProxyService proxyService) {
        return new SpiderProxySelector(Constants.PROXY_CATEGORY_LONG, proxyService);
    }

    /**
     * 构建短效代理选择器
     *
     * @param proxyService 代理服务
     * @return 短效代理选择器
     */
    @Bean(name = "spiderShortProxySelector")
    public SpiderProxySelector buildSpiderShortProxySelector(ProxyService proxyService) {
        return new SpiderProxySelector(Constants.PROXY_CATEGORY_SHORT, proxyService);
    }

    /**
     * 构建HTML URL接收器
     *
     * @param spiderFactory 爬虫工厂
     * @return HTML URL接收器
     */
    @Bean(name = "htmlURLReceiver")
    public URLReceiver buildHTMLURLReceiver(SpiderFactory spiderFactory) {
        Set<String> supportedCategory = new HashSet<>();
        supportedCategory.add(String.valueOf(Constants.CONTENT_CATEGORY_CONTENT));
        supportedCategory.add(String.valueOf(Constants.CONTENT_CATEGORY_LIST));
        return new URLReceiver(spiderFactory, aspectLogger, supportedCategory);
    }

    /**
     * 构建资源URL接收器
     *
     * @param spiderFactory 爬虫工厂
     * @return 资源URL接收器
     */
    @Bean(name = "resourceURLReceiver")
    public URLReceiver buildResourceURLReceiver(SpiderFactory spiderFactory) {
        Set<String> supportedCategory = new HashSet<>();
        supportedCategory.add(String.valueOf(Constants.CONTENT_CATEGORY_RESOURCE));
        supportedCategory.add(String.valueOf(Constants.CONTENT_CATEGORY_STREAM));
        return new URLReceiver(spiderFactory, aspectLogger, supportedCategory);
    }
}
