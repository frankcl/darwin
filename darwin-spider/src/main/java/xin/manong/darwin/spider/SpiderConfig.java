package xin.manong.darwin.spider;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.weapon.base.log.JSONLogger;

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
    public String aspectLogFile;
    public String tempDirectory;
    public String jobTopic;
    public String recordTopic;

    @Bean(name = "spiderAspectLogger")
    public JSONLogger spiderAspectLogger() {
        return new JSONLogger(aspectLogFile, null);
    }
}
