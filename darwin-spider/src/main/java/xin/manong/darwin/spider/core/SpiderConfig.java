package xin.manong.darwin.spider.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.darwin.spider.proxy.SpiderProxySelector;

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
    private static final int DEFAULT_MAX_DEPTH = 3;
    private static final int DEFAULT_MAX_CONTENT_LENGTH = 10485760;
    private static final long DEFAULT_MAX_REPEAT_FETCH_TIME_INTERVAL_MS = 86400 * 1000L;
    private static final long DEFAULT_CONNECT_TIMEOUT_SECONDS = 5L;
    private static final long DEFAULT_READ_TIMEOUT_SECONDS = 10L;
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36";

    public long maxRepeatFetchTimeIntervalMs = DEFAULT_MAX_REPEAT_FETCH_TIME_INTERVAL_MS;
    public long connectTimeoutSeconds = DEFAULT_CONNECT_TIMEOUT_SECONDS;
    public long readTimeoutSeconds = DEFAULT_READ_TIMEOUT_SECONDS;
    public int keepAliveMinutes = DEFAULT_KEEP_ALIVE_MINUTES;
    public int maxIdleConnections = DEFAULT_MAX_IDLE_CONNECTIONS;
    public int maxContentLength = DEFAULT_MAX_CONTENT_LENGTH;
    public int maxDepth = DEFAULT_MAX_DEPTH;
    public int retryCnt = DEFAULT_RETRY_CNT;
    public String userAgent = DEFAULT_USER_AGENT;
    public String ossDirectory;
    public String tempDirectory;

    /**
     * 构建文本爬虫
     *
     * @param router 路由
     * @return 文本爬虫
     */
    @Bean
    public TextSpider buildTextSpider(Router router) {
        TextSpider textSpider = new TextSpider();
        textSpider.supportedMediaTypes().forEach(mediaType -> router.registerSpider(mediaType, textSpider));
        return textSpider;
    }

    /**
     * 构建资源爬虫
     *
     * @param router 路由
     * @return 资源爬虫
     */
    @Bean
    public ResourceSpider buildResourceSpider(Router router) {
        ResourceSpider resourceSpider = new ResourceSpider();
        resourceSpider.supportedMediaTypes().forEach(mediaType -> router.registerSpider(mediaType, resourceSpider));
        return resourceSpider;
    }

    /**
     * 构建M3U8爬虫
     *
     * @param router 路由
     * @return M3U8爬虫
     */
    @Bean
    public M3U8Spider buildM3U8Spider(Router router) {
        M3U8Spider m3U8Spider = new M3U8Spider();
        m3U8Spider.supportedMediaTypes().forEach(mediaType -> router.registerSpider(mediaType, m3U8Spider));
        return m3U8Spider;
    }

    /**
     * 构建长效代理选择器
     *
     * @param proxyService 代理服务
     * @return 长效代理选择器
     */
    @Bean(name = "longProxySelector")
    public SpiderProxySelector buildLongProxySelector(ProxyService proxyService) {
        return new SpiderProxySelector(Constants.PROXY_CATEGORY_LONG, proxyService);
    }

    /**
     * 构建短效代理选择器
     *
     * @param proxyService 代理服务
     * @return 短效代理选择器
     */
    @Bean(name = "shortProxySelector")
    public SpiderProxySelector buildShortProxySelector(ProxyService proxyService) {
        return new SpiderProxySelector(Constants.PROXY_CATEGORY_SHORT, proxyService);
    }
}
