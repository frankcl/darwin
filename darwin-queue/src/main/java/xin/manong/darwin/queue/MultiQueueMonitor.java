package xin.manong.darwin.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.URLRecord;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 多级队列监控器：负责清理队列中过期数据
 *
 * @author frankcl
 * @date 2023-03-07 17:57:48
 */
public class MultiQueueMonitor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MultiQueueMonitor.class);

    private static final String name = "MultiQueueMonitor";

    private boolean running;
    private long checkTimeIntervalMs;
    private long expiredTimeIntervalMs;
    private Thread thread;
    @Resource
    private MultiQueue multiQueue;

    public MultiQueueMonitor(long checkTimeIntervalMs, long expiredTimeIntervalMs) {
        this.running = false;
        this.checkTimeIntervalMs = checkTimeIntervalMs;
        this.expiredTimeIntervalMs = expiredTimeIntervalMs;
    }

    /**
     * 启动监控
     *
     * @return 成功返回true，否则返回false
     */
    public boolean start() {
        logger.info("{} monitor is starting ...", name);
        running = true;
        thread = new Thread(this, name);
        thread.start();
        logger.info("{} monitor has been started", name);
        return true;
    }

    /**
     * 停止监控
     */
    public void stop() {
        logger.info("{} is stopping", name);
        running = false;
        if (thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.info("{} has been stopped", name);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Set<String> jobIds = multiQueue.jobInQueue();
                int sweepRecordCount = 0;
                for (String jobId : jobIds) {
                    List<URLRecord> sweepRecords = multiQueue.sweepExpiredJobRecords(
                            jobId, expiredTimeIntervalMs);
                    sweepRecordCount += sweepRecords.size();
                    //TODO 处理清理数据
                }
                logger.info("scanning job num[{}], sweeping record num[{}], sleep {} seconds",
                        jobIds.size(), sweepRecordCount, checkTimeIntervalMs / 1000);
                Thread.sleep(checkTimeIntervalMs);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
