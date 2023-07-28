package xin.manong.darwin.schedule;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.weapon.base.log.JSONLogger;

/**
 * 调度配置信息
 *
 * @author frankcl
 * @date 2023-07-28 10:55:26
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.schedule")
public class ScheduleConfig {

    private static final Long DEFAULT_QUEUE_SCHEDULE_INTERVAL_MS = 10000L;
    private static final Long DEFAULT_PLAN_EXECUTE_INTERVAL_MS = 60000L;

    public Long planExecuteIntervalMs = DEFAULT_PLAN_EXECUTE_INTERVAL_MS;
    public Long queueScheduleIntervalMs = DEFAULT_QUEUE_SCHEDULE_INTERVAL_MS;
    public String topic;
    public String planAspectLogFile;
    public String recordAspectLogFile;
    public String concurrentAspectLogFile;

    @Bean(name = "recordAspectLogger")
    public JSONLogger buildRecordAspectLogger() {
        return new JSONLogger(recordAspectLogFile, null);
    }

    @Bean(name = "planAspectLogger")
    public JSONLogger buildPlanAspectLogger() {
        return new JSONLogger(planAspectLogFile, null);
    }

    @Bean(name = "concurrentAspectLogger")
    public JSONLogger buildConcurrentAspectLogger() {
        return new JSONLogger(concurrentAspectLogFile, null);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public PeriodPlanScheduler buildPeriodPlanExecutor() {
        return new PeriodPlanScheduler(planExecuteIntervalMs);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public URLQueueScheduler buildURLQueueScheduler() {
        return new URLQueueScheduler(topic, queueScheduleIntervalMs);
    }
}
