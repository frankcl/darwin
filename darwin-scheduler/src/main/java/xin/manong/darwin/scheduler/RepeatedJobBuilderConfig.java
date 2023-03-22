package xin.manong.darwin.scheduler;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 周期性任务构建配置
 *
 * @author frankcl
 * @date 2023-03-22 16:27:27
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.scheduler.job-builder")
public class RepeatedJobBuilderConfig {

    private static final Long DEFAULT_REPEATED_JOB_BUILD_TIME_INTERVAL_MS = 60000L;
    private static final Long DEFAULT_MULTI_QUEUE_LOCK_EXPIRED_TIME_SECONDS = 300L;

    public int retryCnt = 3;
    public Long repeatedJobBuildTimeIntervalMs = DEFAULT_REPEATED_JOB_BUILD_TIME_INTERVAL_MS;
    public Long multiQueueLockExpiredTimeSeconds = DEFAULT_MULTI_QUEUE_LOCK_EXPIRED_TIME_SECONDS;
}
