package xin.manong.darwin.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.log.JSONLogger;

import javax.annotation.Resource;

/**
 * 调度执行器
 *
 * @author frankcl
 * @date 2023-07-28 11:14:56
 */
public abstract class ExecuteRunner implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteRunner.class);

    protected volatile boolean running;
    protected Long executeIntervalMs;
    protected String name;
    protected Thread workThread;
    @Resource(name = "planAspectLogger")
    protected JSONLogger planAspectLogger;
    @Resource(name = "recordAspectLogger")
    protected JSONLogger recordAspectLogger;
    @Resource(name = "concurrentAspectLogger")
    protected JSONLogger concurrentAspectLogger;

    public ExecuteRunner(Long executeIntervalMs) {
        this.running = false;
        this.executeIntervalMs = executeIntervalMs;
        this.name = this.getClass().getSimpleName();
    }

    /**
     * 启动线程运行
     */
    public void start() {
        logger.info("{} is starting ...", name);
        running = true;
        workThread = new Thread(this, name);
        workThread.start();
        logger.info("{} has been started", name);
    }

    /**
     * 停止线程运行
     */
    public void stop() {
        logger.info("{} is stopping ...", name);
        running = false;
        if (!workThread.isAlive()) {
            logger.info("{} is not alive", name);
            return;
        }
        try {
            workThread.interrupt();
            workThread.join();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        logger.info("{} has been stopped", name);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Long startTime = System.currentTimeMillis();
                execute();
                Long processTime = System.currentTimeMillis() - startTime;
                if (processTime >= executeIntervalMs) continue;
                logger.info("finish one round executing, sleep {} seconds", executeIntervalMs / 1000);
                Thread.sleep(executeIntervalMs);
            } catch (InterruptedException e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * 提交并发单元切面日志
     *
     * @param context 上下文
     * @param concurrentUnit 并发单元
     * @param appliedConnections 申请连接数
     * @param acquiredConnections 获取连接数
     * @param overflowConnections 溢出连接数
     */
    protected void commitAspectLog(Context context, String concurrentUnit,
                                   int appliedConnections, int acquiredConnections, int overflowConnections) {
        if (context == null || concurrentAspectLogger == null) return;
        context.put(Constants.DARWIN_RECORD_TYPE, Constants.RECORD_TYPE_CONCURRENT_UNIT);
        context.put(Constants.CONCURRENT_UNIT, concurrentUnit);
        context.put(Constants.APPLIED_CONNECTION_NUM, appliedConnections);
        context.put(Constants.ACQUIRED_CONNECTION_NUM, acquiredConnections);
        context.put(Constants.OVERFLOW_CONNECTION_NUM, overflowConnections);
        concurrentAspectLogger.commit(context.getFeatureMap());
    }

    /**
     * 提交URL记录切面日志
     *
     * @param context 上下文
     * @param record URL记录
     */
    protected void commitAspectLog(Context context, URLRecord record) {
        if (context == null || record == null || recordAspectLogger == null) return;
        DarwinUtil.putContext(context, record);
        recordAspectLogger.commit(context.getFeatureMap());
    }

    /**
     * 提交计划记录切面日志
     *
     * @param context 上下文
     * @param plan 计划
     */
    protected void commitAspectLog(Context context, Plan plan) {
        if (context == null || plan == null || planAspectLogger == null) return;
        DarwinUtil.putContext(context, plan);
        planAspectLogger.commit(context.getFeatureMap());
    }

    /**
     * 执行逻辑
     */
    public abstract void execute();
}
