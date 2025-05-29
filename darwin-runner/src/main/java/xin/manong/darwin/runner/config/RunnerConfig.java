package xin.manong.darwin.runner.config;

import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.darwin.runner.core.Allocator;
import xin.manong.darwin.runner.core.DashboardRunner;
import xin.manong.darwin.runner.core.PlanRunner;
import xin.manong.darwin.runner.manage.ExecuteRunnerRegistry;
import xin.manong.darwin.runner.manage.ExecuteRunnerShell;
import xin.manong.darwin.runner.monitor.ConcurrencyQueueMonitor;
import xin.manong.darwin.runner.monitor.ExpiredCleaner;
import xin.manong.darwin.runner.monitor.ProxyMonitor;
import xin.manong.darwin.service.iface.MessageService;
import xin.manong.weapon.base.etcd.EtcdClient;

/**
 * 运行线程配置信息
 *
 * @author frankcl
 * @date 2023-07-28 10:55:26
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.runner")
public class RunnerConfig {

    private static final long DEFAULT_MAX_OVERFLOW_INTERVAL_MS = 7200000L;
    private static final long DEFAULT_ALLOCATOR_EXECUTE_INTERVAL_MS = 10000L;
    private static final long DEFAULT_PLAN_RUNNER_EXECUTE_INTERVAL_MS = 60000L;
    private static final long DEFAULT_DASHBOARD_RUNNER_EXECUTE_INTERVAL_MS = 600000L;
    private static final long DEFAULT_CONCURRENCY_QUEUE_EXECUTE_TIME_INTERVAL_MS = 300000;
    private static final long DEFAULT_CONCURRENCY_QUEUE_EXPIRED_TIME_INTERVAL_MS = 600000;
    private static final long DEFAULT_PROXY_EXECUTE_TIME_INTERVAL_MS = 300000L;
    private static final long DEFAULT_EXPIRED_CLEANER_EXECUTE_INTERVAL_MS = 3600000L;
    private static final long DEFAULT_MAX_EXPIRED_INTERVAL_MS = 86400000L * 30;

    public long maxOverflowIntervalMs = DEFAULT_MAX_OVERFLOW_INTERVAL_MS;
    public long planRunnerExecuteIntervalMs = DEFAULT_PLAN_RUNNER_EXECUTE_INTERVAL_MS;
    public long allocatorExecuteIntervalMs = DEFAULT_ALLOCATOR_EXECUTE_INTERVAL_MS;
    public long dashboardRunnerExecuteIntervalMs = DEFAULT_DASHBOARD_RUNNER_EXECUTE_INTERVAL_MS;
    public long concurrencyQueueExecuteTimeIntervalMs = DEFAULT_CONCURRENCY_QUEUE_EXECUTE_TIME_INTERVAL_MS;
    public long concurrencyQueueExpiredTimeIntervalMs = DEFAULT_CONCURRENCY_QUEUE_EXPIRED_TIME_INTERVAL_MS;
    public long proxyExecuteTimeIntervalMs = DEFAULT_PROXY_EXECUTE_TIME_INTERVAL_MS;
    public long expiredCleanerExecuteIntervalMs = DEFAULT_EXPIRED_CLEANER_EXECUTE_INTERVAL_MS;
    public long maxExpiredIntervalMs = DEFAULT_MAX_EXPIRED_INTERVAL_MS;
    public String topicURL;
    @Resource
    private MessageService messageService;
    @Resource
    private ExecuteRunnerRegistry registry;
    @Resource
    private EtcdClient etcdClient;

    /**
     * 构建周期计划运行器
     *
     * @return 周期计划运行器
     */
    @Bean(destroyMethod = "stop")
    public PlanRunner buildPlanRunner() {
        PlanRunner scheduler = new PlanRunner(planRunnerExecuteIntervalMs);
        registry.register(new ExecuteRunnerShell(
                ExecuteRunnerShell.LOCK_KEY_PLAN_RUNNER,
                scheduler, ExecuteRunnerShell.RUNNER_TYPE_CORE,
                messageService, etcdClient));
        return scheduler;
    }

    /**
     * 构建抓取链接分配器
     *
     * @return 抓取链接分配器
     */
    @Bean(destroyMethod = "stop")
    public Allocator buildAllocator() {
        Allocator allocator = new Allocator(topicURL, allocatorExecuteIntervalMs, maxOverflowIntervalMs);
        registry.register(new ExecuteRunnerShell(
                ExecuteRunnerShell.LOCK_KEY_ALLOCATOR,
                allocator, ExecuteRunnerShell.RUNNER_TYPE_CORE,
                messageService, etcdClient));
        return allocator;
    }

    /**
     * 构建首页数据大盘运行器
     *
     * @return 首页数据大盘运行器
     */
    @Bean(destroyMethod = "stop")
    public DashboardRunner buildDashboardRunner() {
        DashboardRunner runner = new DashboardRunner(dashboardRunnerExecuteIntervalMs);
        registry.register(new ExecuteRunnerShell(
                ExecuteRunnerShell.LOCK_KEY_DASHBOARD_RUNNER,
                runner, ExecuteRunnerShell.RUNNER_TYPE_CORE,
                messageService, etcdClient));
        return runner;
    }

    /**
     * 构建并发连接监控器
     *
     * @return 并发连接监控器
     */
    @Bean(destroyMethod = "stop")
    public ConcurrencyQueueMonitor buildConcurrencyQueueMonitor() {
        ConcurrencyQueueMonitor monitor = new ConcurrencyQueueMonitor(
                concurrencyQueueExecuteTimeIntervalMs, concurrencyQueueExpiredTimeIntervalMs);
        registry.register(new ExecuteRunnerShell(
                ExecuteRunnerShell.LOCK_KEY_CONCURRENCY_QUEUE_MONITOR,
                monitor, ExecuteRunnerShell.RUNNER_TYPE_MONITOR,
                messageService, etcdClient));
        return monitor;
    }

    /**
     * 构建过期数据清理器
     *
     * @return 过期数据清理器
     */
    @Bean(destroyMethod = "stop")
    public ExpiredCleaner buildExpiredCleaner() {
        ExpiredCleaner cleaner = new ExpiredCleaner(maxExpiredIntervalMs, expiredCleanerExecuteIntervalMs);
        registry.register(new ExecuteRunnerShell(
                ExecuteRunnerShell.LOCK_KEY_EXPIRED_CLEANER,
                cleaner, ExecuteRunnerShell.RUNNER_TYPE_MONITOR,
                messageService, etcdClient));
        return cleaner;
    }

    /**
     * 构建代理监控器
     *
     * @return 代理监控器
     */
    @Bean(destroyMethod = "stop")
    public ProxyMonitor buildProxyMonitor() {
        ProxyMonitor monitor = new ProxyMonitor(proxyExecuteTimeIntervalMs);
        registry.register(new ExecuteRunnerShell(
                ExecuteRunnerShell.LOCK_KEY_PROXY_MONITOR,
                monitor, ExecuteRunnerShell.RUNNER_TYPE_MONITOR,
                messageService, etcdClient));
        return monitor;
    }
}
