package xin.manong.darwin.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 执行监控器定义
 *
 * @author frankcl
 * @date 2023-12-13 11:52:28
 */
public abstract class ExecuteMonitor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteMonitor.class);

    protected volatile boolean running;
    private long checkTimeIntervalMs;
    private Thread thread;

    public ExecuteMonitor(long checkTimeIntervalMs) {
        this.running = false;
        this.checkTimeIntervalMs = checkTimeIntervalMs;
    }

    /**
     * 启动监控
     *
     * @return 成功返回true，否则返回false
     */
    public boolean start() {
        logger.info("{} is starting ...", this.getClass().getSimpleName());
        running = true;
        thread = new Thread(this, this.getClass().getSimpleName());
        thread.start();
        logger.info("{} has been started", this.getClass().getSimpleName());
        return true;
    }

    /**
     * 停止监控
     */
    public void stop() {
        logger.info("{} is stopping", this.getClass().getSimpleName());
        running = false;
        if (thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.info("{} has been stopped", this.getClass().getSimpleName());
    }

    @Override
    public void run() {
        while (running) {
            try {
                execute();
                logger.info("finish processing, sleep {} seconds", checkTimeIntervalMs / 1000);
                Thread.sleep(checkTimeIntervalMs);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 执行逻辑
     *
     * @throws Exception 异常
     */
    public abstract void execute() throws Exception;
}
