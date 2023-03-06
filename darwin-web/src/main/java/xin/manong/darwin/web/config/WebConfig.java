package xin.manong.darwin.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.weapon.base.log.JSONLogger;

/**
 * web应用配置
 *
 * @author frankcl
 * @date 2022-08-24 13:04:15
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.web")
public class WebConfig {

    public String name;
    public String aspectLogFile;

    @Bean
    public JSONLogger webAspectLogger() {
        return new JSONLogger(aspectLogFile, null);
    }
}
