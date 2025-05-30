package xin.manong.darwin.parser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.darwin.parser.script.ScriptCache;
import xin.manong.darwin.parser.script.ScriptFactory;

/**
 * 解析配置
 *
 * @author frankcl
 * @date 2023-08-25 16:13:26
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.parser")
public class ParseConfig {

    private static final int DEFAULT_CACHE_MAX_SIZE = 1000;
    private static final int DEFAULT_CACHE_EXPIRED_TIME_MINUTES = 30;

    public int cacheMaxSize = DEFAULT_CACHE_MAX_SIZE;
    public int cacheExpiredTimeMinutes = DEFAULT_CACHE_EXPIRED_TIME_MINUTES;
    public String requireCwd;

    /**
     * 构建脚本工厂
     *
     * @return 脚本工厂
     */
    @Bean
    public ScriptFactory buildScriptFactory() {
        return new ScriptFactory(requireCwd);
    }

    /**
     * 构建脚本缓存
     *
     * @return 脚本缓存
     */
    @Bean
    public ScriptCache buildScriptCache() {
        return new ScriptCache(cacheMaxSize, cacheExpiredTimeMinutes);
    }
}
