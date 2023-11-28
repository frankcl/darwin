package xin.manong.darwin.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存配置
 *
 * @author frankcl
 * @date 2023-11-28 13:47:32
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.service.cache")
public class CacheConfig {

    private static final int DEFAULT_RULE_CACHE_NUM = 100;
    private static final int DEFAULT_RULE_EXPIRED_MINUTES = 5;
    private static final int DEFAULT_JOB_CACHE_NUM = 100;
    private static final int DEFAULT_JOB_EXPIRED_MINUTES = 5;
    private static final int DEFAULT_URL_CACHE_NUM = 500;
    private static final int DEFAULT_URL_EXPIRED_MINUTES = 60;

    public int ruleCacheNum = DEFAULT_RULE_CACHE_NUM;
    public int ruleExpiredMinutes = DEFAULT_RULE_EXPIRED_MINUTES;
    public int jobCacheNum = DEFAULT_JOB_CACHE_NUM;
    public int jobExpiredMinutes = DEFAULT_JOB_EXPIRED_MINUTES;
    public int urlCacheNum = DEFAULT_URL_CACHE_NUM;
    public int urlExpiredMinutes = DEFAULT_URL_EXPIRED_MINUTES;
}
