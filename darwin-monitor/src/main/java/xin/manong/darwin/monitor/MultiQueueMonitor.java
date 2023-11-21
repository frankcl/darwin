package xin.manong.darwin.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.listener.JobListener;

import javax.annotation.Resource;
import java.util.HashSet;
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

    private volatile boolean running;
    private long checkTimeIntervalMs;
    private long expiredTimeIntervalMs;
    private Thread thread;
    private Set<Integer> updateStatusList;
    @Resource
    protected URLService urlService;
    @Resource
    protected MultiQueue multiQueue;
    @Resource
    protected JobListener jobListener;

    public MultiQueueMonitor(long checkTimeIntervalMs, long expiredTimeIntervalMs) {
        this.running = false;
        this.checkTimeIntervalMs = checkTimeIntervalMs;
        this.expiredTimeIntervalMs = expiredTimeIntervalMs;
        this.updateStatusList = new HashSet<>();
        this.updateStatusList.add(Constants.URL_STATUS_FETCHING);
        this.updateStatusList.add(Constants.URL_STATUS_QUEUING);
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
                sweepExpiredJobs();
                sweepExpiredConcurrentUnits();
                logger.info("finish sweeping, sleep {} seconds", checkTimeIntervalMs / 1000);
                Thread.sleep(checkTimeIntervalMs);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 清理过期并发单元
     */
    private void sweepExpiredConcurrentUnits() {
        Set<String> concurrentUnits = multiQueue.copyCurrentConcurrentUnits();
        int sweepConcurrentUnitCount = 0;
        for (String concurrentUnit : concurrentUnits) {
            if (multiQueue.expiredConcurrentUnit(concurrentUnit)) sweepConcurrentUnitCount++;
        }
        logger.info("scanning concurrent unit num[{}], sweeping concurrent unit num[{}]",
                concurrentUnits.size(), sweepConcurrentUnitCount);
    }

    /**
     * 清理过期任务
     */
    private void sweepExpiredJobs() {
        Set<String> jobIds = multiQueue.copyCurrentJobs();
        int sweepRecordCount = 0, sweepJobCount = 0;
        for (String jobId : jobIds) {
            List<URLRecord> sweepRecords = multiQueue.sweepExpiredJobRecords(
                    jobId, expiredTimeIntervalMs);
            updateTimeoutStatus(sweepRecords);
            sweepRecordCount += sweepRecords.size();
            if (multiQueue.isEmptyJobRecordMap(jobId)) {
                multiQueue.deleteJobRecordMap(jobId);
                if (!sweepRecords.isEmpty()) {
                    sweepJobCount++;
                    jobListener.onFinish(new Job(jobId, sweepRecords.get(0).appId));
                }
            }
        }
        logger.info("scanning job num[{}], sweeping job num[{}], record num[{}]",
                jobIds.size(), sweepJobCount, sweepRecordCount);
    }

    /**
     * 更新URL超时状态
     *
     * @param sweepRecords 清理URL数据
     */
    private void updateTimeoutStatus(List<URLRecord> sweepRecords) {
        for (URLRecord sweepRecord : sweepRecords) {
            URLRecord record = urlService.get(sweepRecord.key);
            if (!updateStatusList.contains(record.status)) continue;
            if (!urlService.updateStatus(record.key, Constants.URL_STATUS_TIMEOUT)) {
                logger.warn("update timeout status failed for url[{}]", record.key);
            }
        }
    }
}
