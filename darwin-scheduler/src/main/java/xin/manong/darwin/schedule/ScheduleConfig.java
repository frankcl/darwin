package xin.manong.darwin.schedule;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    private static final Long DEFAULT_MAX_OVERFLOW_TIME_MS = 7200000L;

    public Long maxOverflowTimeMs = DEFAULT_MAX_OVERFLOW_TIME_MS;
    public Long planExecuteIntervalMs = DEFAULT_PLAN_EXECUTE_INTERVAL_MS;
    public Long queueScheduleIntervalMs = DEFAULT_QUEUE_SCHEDULE_INTERVAL_MS;
    public String topic;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public PeriodicPlanScheduler buildPeriodicPlanExecutor() {
        return new PeriodicPlanScheduler(planExecuteIntervalMs);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public URLQueueScheduler buildURLQueueScheduler() {
        return new URLQueueScheduler(topic, queueScheduleIntervalMs);
    }
}
