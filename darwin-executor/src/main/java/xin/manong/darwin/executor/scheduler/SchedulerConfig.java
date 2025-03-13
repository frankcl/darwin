package xin.manong.darwin.executor.scheduler;

import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.darwin.executor.GlobalExecutorRegistry;
import xin.manong.darwin.executor.GlobalExecutor;
import xin.manong.darwin.service.iface.ExecutorService;
import xin.manong.weapon.base.etcd.EtcdClient;

/**
 * 调度器配置信息
 *
 * @author frankcl
 * @date 2023-07-28 10:55:26
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.executor.scheduler")
public class SchedulerConfig {

    private static final Long DEFAULT_URL_SCHEDULER_EXECUTE_INTERVAL_MS = 10000L;
    private static final Long DEFAULT_PLAN_SCHEDULER_EXECUTE_INTERVAL_MS = 60000L;
    private static final Long DEFAULT_PROXY_REFRESHER_EXECUTE_INTERVAL_MS = 300000L;
    private static final Long DEFAULT_MAX_OVERFLOW_TIME_MS = 7200000L;

    public Long maxOverflowTimeMs = DEFAULT_MAX_OVERFLOW_TIME_MS;
    public Long planSchedulerExecuteIntervalMs = DEFAULT_PLAN_SCHEDULER_EXECUTE_INTERVAL_MS;
    public Long urlSchedulerExecuteIntervalMs = DEFAULT_URL_SCHEDULER_EXECUTE_INTERVAL_MS;
    public Long proxyRefresherExecuteTimeIntervalMs = DEFAULT_PROXY_REFRESHER_EXECUTE_INTERVAL_MS;
    public String topicURL;
    @Resource
    private ExecutorService executorService;
    @Resource
    private GlobalExecutorRegistry registry;
    @Resource
    private EtcdClient etcdClient;

    @Bean
    public PlanScheduler buildPlanScheduler() {
        PlanScheduler scheduler = new PlanScheduler(planSchedulerExecuteIntervalMs);
        registry.register(new GlobalExecutor(
                GlobalExecutor.LOCK_KEY_PLAN_SCHEDULER, scheduler, executorService, etcdClient));
        return scheduler;
    }

    @Bean
    public URLScheduler buildURLScheduler() {
        URLScheduler scheduler = new URLScheduler(topicURL, urlSchedulerExecuteIntervalMs, maxOverflowTimeMs);
        registry.register(new GlobalExecutor(
                GlobalExecutor.LOCK_KEY_URL_SCHEDULER, scheduler, executorService, etcdClient));
        return scheduler;
    }

    @Bean
    public ProxyRefresher buildProxyRefresher() {
        ProxyRefresher proxyRefresher = new ProxyRefresher(proxyRefresherExecuteTimeIntervalMs);
        registry.register(new GlobalExecutor(
                GlobalExecutor.LOCK_KEY_PROXY_REFRESHER, proxyRefresher, executorService, etcdClient));
        return proxyRefresher;
    }
}
