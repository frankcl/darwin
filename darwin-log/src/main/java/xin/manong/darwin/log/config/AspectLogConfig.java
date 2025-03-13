package xin.manong.darwin.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.weapon.base.log.JSONLogger;

/**
 * 切面日志配置
 *
 * @author frankcl
 * @date 2022-08-24 13:04:15
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.log")
public class AspectLogConfig {

    public String jobLogFile;
    public String planLogFile;
    public String urlLogFile;
    public String concurrentLogFile;

    @Bean(name = "jobAspectLogger")
    public JSONLogger jobAspectLogger() {
        return new JSONLogger(jobLogFile, null);
    }

    @Bean(name = "planAspectLogger")
    public JSONLogger planAspectLogger() {
        return new JSONLogger(planLogFile, null);
    }

    @Bean(name = "urlAspectLogger")
    public JSONLogger urlAspectLogger() {
        return new JSONLogger(urlLogFile, null);
    }

    @Bean(name = "concurrentAspectLogger")
    public JSONLogger concurrentAspectLogger() {
        return new JSONLogger(concurrentLogFile, null);
    }
}
