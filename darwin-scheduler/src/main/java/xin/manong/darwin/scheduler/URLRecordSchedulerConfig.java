package xin.manong.darwin.scheduler;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.weapon.base.log.JSONLogger;

/**
 * URL记录调度器配置
 *
 * @author frankcl
 * @date 2023-03-22 17:33:33
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.scheduler.url-scheduler")
public class URLRecordSchedulerConfig {

    private static final Long DEFAULT_SCHEDULE_TIME_INTERVAL_MS = 10000L;

    public Long scheduleTimeIntervalMs = DEFAULT_SCHEDULE_TIME_INTERVAL_MS;
    public String topic;
    public String tags = "*";
    public String aspectLogFile;

    @Bean(name = "scheduleAspectLogger")
    public JSONLogger scheduleAspectLogger() {
        return new JSONLogger(aspectLogFile, null);
    }
}
