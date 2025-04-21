package xin.manong.darwin.runner.monitor;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.ConcurrencyControl;
import xin.manong.darwin.queue.ConcurrencyQueue;
import xin.manong.darwin.service.event.JobEventListener;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.event.URLEventListener;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.executor.ExecuteRunner;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 多级队列监控器：负责清理队列中过期数据
 *
 * @author frankcl
 * @date 2023-03-07 17:57:48
 */
public class ConcurrencyQueueMonitor extends ExecuteRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyQueueMonitor.class);

    private static final int BATCH_GET_SIZE = 100;
    public static final String ID = "concurrency_queue_monitor";

    private final long expiredTimeIntervalMs;
    @Resource
    protected URLService urlService;
    @Resource
    protected JobService jobService;
    @Resource
    protected ConcurrencyQueue concurrencyQueue;
    @Resource
    protected ConcurrencyControl concurrencyControl;
    @Resource
    protected URLEventListener urlEventListener;
    @Resource
    protected JobEventListener jobEventListener;

    public ConcurrencyQueueMonitor(long executeTimeIntervalMs, long expiredTimeIntervalMs) {
        super(ID, executeTimeIntervalMs);
        this.expiredTimeIntervalMs = expiredTimeIntervalMs;
        this.setName("并发队列监控器");
        this.setDescription("负责清理长期积累且未抓取完成的数据和任务，以及未释放的并发连接");
    }

    @Override
    public void execute() throws Exception {
        sweepExpiredJobs();
        sweepExpiredConnections();
        sweepExpiredConcurrentUnits();
    }

    /**
     * 清理过期并发连接
     */
    private void sweepExpiredConnections() {
        int releaseConnections = 0;
        Set<String> concurrentUnits = concurrencyQueue.concurrentUnitsSnapshots();
        for (String concurrentUnit : concurrentUnits) {
            releaseConnections += releaseExpiredConnections(concurrentUnit);
        }
        logger.info("Scanning concurrent unit num:{}, releasing connection num:{}",
                concurrentUnits.size(), releaseConnections);
    }

    /**
     * 清理过期并发单元
     */
    private void sweepExpiredConcurrentUnits() {
        Set<String> concurrentUnits = concurrencyQueue.concurrentUnitsSnapshots();
        int sweepConcurrentUnitCount = 0;
        for (String concurrentUnit : concurrentUnits) {
            if (concurrencyQueue.removeConcurrentUnit(concurrentUnit)) sweepConcurrentUnitCount++;
        }
        logger.info("Scanning concurrent unit num:{}, sweeping concurrent unit num:{}",
                concurrentUnits.size(), sweepConcurrentUnitCount);
    }

    /**
     * 清理过期任务
     */
    private void sweepExpiredJobs() {
        long minExpiredTime = System.currentTimeMillis() - expiredTimeIntervalMs;
        List<Job> jobs = jobService.getRunningJobs(minExpiredTime, BATCH_GET_SIZE);
        int sweepCount = 0;
        for (Job job : jobs) {
            List<URLRecord> records = urlService.getExpiredRecords(job.jobId, minExpiredTime, BATCH_GET_SIZE);
            sweepCount += records.size();
            for (URLRecord record : records) handleExpiredRecord(record);
            jobEventListener.onComplete(job.jobId, new Context());
        }
        logger.info("Scanning job num:{}, sweeping record num:{}", jobs.size(), sweepCount);
    }

    /**
     * 释放并发单元的过期连接
     *
     * @param concurrentUnit 并发单元
     * @return 释放连接数
     */
    private int releaseExpiredConnections(String concurrentUnit) {
        int releaseConnections = 0;
        Map<String, Long> concurrentRecordMap = concurrencyControl.getConcurrentRecordMap(concurrentUnit);
        Iterator<Map.Entry<String, Long>> iterator = concurrentRecordMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String recordKey = entry.getKey();
            Long recordTime = entry.getValue();
            Long currentTime = System.currentTimeMillis();
            long timeInterval = recordTime == null ? currentTime : currentTime - recordTime;
            if (timeInterval < expiredTimeIntervalMs) continue;
            iterator.remove();
            releaseConnections++;
            logger.info("Release expired connection:{} for concurrent unit:{}", recordKey, concurrentUnit);
        }
        return releaseConnections;
    }

    /**
     * 处理过期数据
     *
     * @param record 数据
     */
    private void handleExpiredRecord(URLRecord record) {
        Context context = new Context();
        context.put(Constants.DARWIN_STAGE, Constants.STAGE_MONITOR);
        if (!urlService.updateStatus(record.key, Constants.URL_STATUS_TIMEOUT)) {
            logger.warn("update timeout status failed for url:{}", record.url);
        }
        urlEventListener.onComplete(record.key, context);
    }
}
