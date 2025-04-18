package xin.manong.darwin.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Executor;
import xin.manong.darwin.executor.monitor.ConcurrentConnectionMonitor;
import xin.manong.darwin.executor.monitor.MultiQueueMonitor;
import xin.manong.darwin.executor.monitor.ProxyMonitor;
import xin.manong.darwin.executor.scheduler.PlanScheduler;
import xin.manong.darwin.executor.scheduler.ProxyRefresher;
import xin.manong.darwin.executor.scheduler.URLScheduler;
import xin.manong.darwin.service.iface.ExecutorService;
import xin.manong.weapon.base.etcd.EtcdClient;
import xin.manong.weapon.base.etcd.EtcdLock;
import xin.manong.weapon.base.executor.ExecuteRunner;

/**
 * 全局执行器
 * 使用分布式锁保证多服务实例执行器唯一
 *
 * @author frankcl
 * @date 2025-03-07 19:45:12
 */
public class GlobalExecutor {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExecutor.class);
    private static final long LEASE_TTL = 60L;
    private static final long ACQUIRE_LOCK_TIMEOUT = 15L;
    private static final String LOCK_KEY_PREFIX = "darwin/";
    public static final String LOCK_KEY_PLAN_SCHEDULER = LOCK_KEY_PREFIX + PlanScheduler.NAME;
    public static final String LOCK_KEY_URL_SCHEDULER = LOCK_KEY_PREFIX + URLScheduler.NAME;
    public static final String LOCK_KEY_PROXY_REFRESHER = LOCK_KEY_PREFIX + ProxyRefresher.NAME;
    public static final String LOCK_KEY_CONCURRENT_CONNECTION_MONITOR = LOCK_KEY_PREFIX + ConcurrentConnectionMonitor.NAME;
    public static final String LOCK_KEY_MULTI_QUEUE_MONITOR = LOCK_KEY_PREFIX + MultiQueueMonitor.NAME;
    public static final String LOCK_KEY_PROXY_MONITOR = LOCK_KEY_PREFIX + ProxyMonitor.NAME;

    private final String lockKey;
    private final ExecuteRunner runner;
    private final EtcdClient etcdClient;
    private final ExecutorService executorService;
    private final ExecutorLeaseAliveObserver observer;
    private boolean running;
    private EtcdLock etcdLock;

    public GlobalExecutor(String lockKey, ExecuteRunner runner,
                          ExecutorService executorService, EtcdClient etcdClient) {
        this.running = false;
        this.lockKey = lockKey;
        this.runner = runner;
        this.executorService = executorService;
        this.etcdClient = etcdClient;
        observer = new ExecutorLeaseAliveObserver(runner.getName(), executorService);
    }

    /**
     * 启动执行器
     */
    public void start() {
        if (running) {
            logger.warn("global executor[{}] has been started", runner.getName());
            throw new IllegalStateException("执行器处于运行状态");
        }
        etcdLock = etcdClient.lock(lockKey, LEASE_TTL, ACQUIRE_LOCK_TIMEOUT, observer);
        if (etcdLock == null) {
            logger.warn("apply lock failed for key[{}]", lockKey);
            throw new IllegalStateException("获取锁失败");
        }
        boolean success = runner.start();
        if (!success) {
            etcdClient.unlock(etcdLock);
            throw new IllegalStateException("启动失败");
        }
        boolean add = false;
        Executor executor = executorService.get(runner.getName());
        if (executor == null) {
            add = true;
            executor = new Executor();
            executor.setName(runner.getName());
        }
        executor.status = Constants.EXECUTOR_STATUS_RUNNING;
        executor.cause = "";
        boolean status = add ? executorService.add(executor) : executorService.update(executor);
        if (!status) {
            logger.error("add/update status failed for global executor[{}]", runner.getName());
            runner.stop();
            throw new IllegalStateException("更新执行器状态失败");
        }
        running = true;
    }

    /**
     * 停止执行器
     */
    public void stop() {
        try {
            if (!running) {
                logger.warn("global executor[{}] is not running", runner.getName());
                throw new IllegalStateException("执行器处于停止状态");
            }
            runner.stop();
            running = false;
            Executor executor = new Executor();
            executor.name = runner.getName();
            executor.status = Constants.EXECUTOR_STATUS_STOPPED;
            if (!executorService.updateByName(executor.name, executor)) {
                logger.error("update stopped status failed for global executor[{}]", runner.getName());
                throw new IllegalStateException("更新执行器状态失败");
            }
        } finally {
            if (etcdLock != null) etcdClient.unlock(etcdLock);
        }
    }

    /**
     * 获取执行器名称
     *
     * @return 执行器名称
     */
    public String getName() {
        return runner.getName();
    }
}
