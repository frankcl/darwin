package xin.manong.darwin.runner.manage;

import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.Message;
import xin.manong.darwin.runner.monitor.ConcurrencyQueueMonitor;
import xin.manong.darwin.runner.monitor.ProxyMonitor;
import xin.manong.darwin.runner.core.Allocator;
import xin.manong.darwin.runner.core.PlanExecutor;
import xin.manong.darwin.service.iface.MessageService;
import xin.manong.weapon.base.etcd.EtcdClient;
import xin.manong.weapon.base.etcd.LockApproval;
import xin.manong.weapon.base.etcd.LockRequest;
import xin.manong.weapon.base.event.ErrorEvent;
import xin.manong.weapon.base.event.EventListener;
import xin.manong.weapon.base.executor.ExecuteRunner;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 执行器包装
 * 使用分布式锁保证全局执行器实例唯一
 *
 * @author frankcl
 * @date 2025-03-07 19:45:12
 */
public class ExecuteRunnerShell implements EventListener {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteRunnerShell.class);
    private static final long LEASE_TTL = 60L;
    private static final long ACQUIRE_LOCK_TIMEOUT = 15L;
    private static final String LOCK_KEY_PREFIX = "darwin/runner/";
    private static final String MESSAGE_STOP = "<STOP>";

    public static final int RUNNER_TYPE_CORE = 1;
    public static final int RUNNER_TYPE_MONITOR = 2;

    public static final String LOCK_KEY_PLAN_EXECUTOR = LOCK_KEY_PREFIX + PlanExecutor.ID;
    public static final String LOCK_KEY_ALLOCATOR = LOCK_KEY_PREFIX + Allocator.ID;
    public static final String LOCK_KEY_CONCURRENCY_QUEUE_MONITOR = LOCK_KEY_PREFIX + ConcurrencyQueueMonitor.ID;
    public static final String LOCK_KEY_PROXY_MONITOR = LOCK_KEY_PREFIX + ProxyMonitor.KEY;

    @Getter
    private final int runnerType;
    private final String lockKey;
    private final ExecuteRunner runner;
    private final EtcdClient etcdClient;
    private final MessageService messageService;
    private final ExecuteLeaseAliveObserver observer;
    private final LockPathRemoveConsumer consumer;
    private final BlockingQueue<String> messageQueue;
    private LockApproval approval;
    /**
     * 停止线程，解决Etcd WatchConsumer中调用Etcd相关方法发生的阻塞问题
     */
    private Thread stopThread;

    public ExecuteRunnerShell(String lockKey, ExecuteRunner runner,
                              int runnerType, MessageService messageService,
                              EtcdClient etcdClient) {
        this.lockKey = lockKey;
        this.runner = runner;
        this.runnerType = runnerType;
        this.messageService = messageService;
        this.etcdClient = etcdClient;
        this.runner.addEventListener(this);
        this.messageQueue = new ArrayBlockingQueue<>(1);
        this.observer = new ExecuteLeaseAliveObserver(this);
        this.consumer = new LockPathRemoveConsumer(this);
    }

    /**
     * 启动执行器
     */
    public void start() {
        if (runner.isRunning()) {
            logger.warn("Execute runner:{} has been started", runner.getId());
            throw new IllegalStateException("当前已是运行状态");
        }
        LockRequest request = new LockRequest(lockKey, LEASE_TTL, observer);
        approval = etcdClient.applyLock(request, ACQUIRE_LOCK_TIMEOUT);
        if (approval == null) {
            logger.warn("Apply lock failed for key:{}", lockKey);
            throw new IllegalStateException("获取启动权限失败");
        }
        boolean success = runner.start();
        if (!success) {
            etcdClient.releaseLock(approval);
            throw new IllegalStateException("启动失败");
        }
        etcdClient.addWatch(approval.getPath(), consumer);
        stopThread = new Thread(this::handleMessage);
        stopThread.start();
    }

    /**
     * 异步停止：发送停止信号
     */
    public void asyncStop() {
        try {
            messageQueue.put(MESSAGE_STOP);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * 停止执行器
     */
    public void stop() {
        try {
            if (!runner.isRunning()) {
                logger.warn("Execute runner:{} is not running", runner.getId());
                throw new IllegalStateException("当前已是停止状态");
            }
            if (runner.isRunning()) runner.stop();
            else {
                List<String> keys = etcdClient.getKeysWithPrefix(lockKey);
                if (!keys.isEmpty()) etcdClient.delete(keys.get(0));
            }
        } finally {
            if (stopThread != null) {
                if (stopThread.isAlive()) stopThread.interrupt();
                stopThread = null;
            }
            if (approval != null) {
                etcdClient.removeWatch(approval.getPath());
                etcdClient.releaseLock(approval);
                approval = null;
            }
        }
    }

    /**
     * 获取执行器key
     *
     * @return 执行器key
     */
    public String getKey() {
        return runner.getId();
    }

    /**
     * 获取执行器名
     *
     * @return 执行器名
     */
    public String getName() {
        return runner.getName();
    }

    /**
     * 获取描述信息
     *
     * @return 描述信息
     */
    public String getDescription() {
        return runner.getDescription();
    }

    /**
     * 是否运行状态
     *
     * @return 运行返回true，否则返回false
     */
    public boolean isRunning() {
        if (runner.isRunning()) return true;
        LockRequest request = new LockRequest(lockKey, LEASE_TTL);
        LockApproval approval = etcdClient.applyLock(request, 1L);
        if (approval == null) return true;
        etcdClient.releaseLock(approval);
        return false;
    }

    @Override
    public void onError(@NotNull ErrorEvent errorEvent) {
        Message message = new Message();
        message.sourceKey = runner.getId();
        message.sourceType = Message.SOURCE_TYPE_RUNNER;
        message.message = errorEvent.getMessage();
        message.exception = errorEvent.getThrowable() == null ? null :
                ExceptionUtils.getStackTrace(errorEvent.getThrowable());
        messageService.push(message);
    }

    /**
     * 停止线程处理逻辑
     * 负责接收异步停止信号并停止执行器
     */
    private void handleMessage() {
        logger.info("Stop thread of {} is running", getKey());
        while (true) {
            try {
                String message = messageQueue.poll(1, TimeUnit.SECONDS);
                if (message == null || !message.equalsIgnoreCase(MESSAGE_STOP)) continue;
                stop();
            } catch (InterruptedException e) {
                break;
            }
        }
        logger.info("Stop thread of {} has been stopped", getKey());
    }
}
