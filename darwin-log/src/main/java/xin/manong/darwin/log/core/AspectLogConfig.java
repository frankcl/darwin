package xin.manong.darwin.log.core;

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

    public String jobAspectLogFile;
    public String planAspectLogFile;
    public String urlAspectLogFile;
    public String concurrencyAspectLogFile;
    public String webAspectLogFile;

    /**
     * 任务切面日志
     *
     * @return 任务切面日志
     */
    @Bean(name = "jobAspectLogger")
    public JSONLogger jobAspectLogger() {
        return new JSONLogger(jobAspectLogFile, null);
    }

    /**
     * 计划切面日志
     *
     * @return 计划切面日志
     */
    @Bean(name = "planAspectLogger")
    public JSONLogger planAspectLogger() {
        return new JSONLogger(planAspectLogFile, null);
    }

    /**
     * 数据切面日志
     *
     * @return 数据切面日志
     */
    @Bean(name = "urlAspectLogger")
    public JSONLogger urlAspectLogger() {
        return new JSONLogger(urlAspectLogFile, null);
    }

    /**
     * 并发单元切面日志
     *
     * @return 并发单元切面日志
     */
    @Bean(name = "concurrencyAspectLogger")
    public JSONLogger concurrencyAspectLogger() {
        return new JSONLogger(concurrencyAspectLogFile, null);
    }

    /**
     * web层切面日志
     *
     * @return web层切面日志
     */
    @Bean(name = "webAspectLogger")
    public JSONLogger webAspectLogger() {
        return new JSONLogger(webAspectLogFile, null);
    }
}
