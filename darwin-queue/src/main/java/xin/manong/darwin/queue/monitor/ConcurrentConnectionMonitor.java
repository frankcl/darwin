package xin.manong.darwin.queue.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.computer.ConcurrentUnitComputer;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.concurrent.ConcurrentManager;
import xin.manong.darwin.queue.multi.MultiQueue;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * 并发连接监控器
 *
 * @author frankcl
 * @date 2023-03-10 10:35:26
 */
public class ConcurrentConnectionMonitor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MultiQueueMonitor.class);

    private boolean running;
    private long checkTimeIntervalMs;
    private long expiredTimeIntervalMs;
    private Thread thread;
    @Resource
    protected MultiQueue multiQueue;
    @Resource
    protected ConcurrentManager concurrentManager;

    public ConcurrentConnectionMonitor(long checkTimeIntervalMs, long expiredTimeIntervalMs) {
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
        logger.info("{} monitor is starting ...", this.getClass().getSimpleName());
        running = true;
        thread = new Thread(this, this.getClass().getSimpleName());
        thread.start();
        logger.info("{} monitor has been started", this.getClass().getSimpleName());
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
                releaseExpiredConnections();
                logger.info("finish releasing, sleep {} seconds", checkTimeIntervalMs / 1000);
                Thread.sleep(checkTimeIntervalMs);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 释放过期连接
     */
    private void releaseExpiredConnections() {
        int releaseConnectionNum = 0, scanJobNum = 0;
        Set<String> jobIds = multiQueue.jobsInQueue();
        for (String jobId : jobIds) {
            scanJobNum++;
            Map<String, URLRecord> jobRecordMap = multiQueue.getJobRecordMap(jobId);
            for (URLRecord record : jobRecordMap.values()) {
                if (record.status != Constants.URL_STATUS_FETCHING) continue;
                long timeInterval = System.currentTimeMillis() - record.outQueueTime;
                if (timeInterval < expiredTimeIntervalMs) continue;
                String concurrentUnit = ConcurrentUnitComputer.compute(record);
                if (concurrentManager.decreaseConnections(concurrentUnit, 1) > 0) {
                    releaseConnectionNum++;
                    logger.info("release expired connection for concurrent unit[{}] and job[{}]",
                            concurrentUnit, jobId);
                }
            }
        }
        logger.info("scanning job num[{}], releasing connection num[{}]", scanJobNum, releaseConnectionNum);
    }
}
