package xin.manong.darwin.monitor;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.notify.JobCompleteNotifier;
import xin.manong.darwin.service.notify.URLCompleteNotifier;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.executor.ExecuteRunner;

import java.util.List;
import java.util.Set;

/**
 * 多级队列监控器：负责清理队列中过期数据
 *
 * @author frankcl
 * @date 2023-03-07 17:57:48
 */
public class MultiQueueMonitor extends ExecuteRunner {

    private static final Logger logger = LoggerFactory.getLogger(MultiQueueMonitor.class);

    private final long expiredTimeIntervalMs;
    @Resource
    protected URLService urlService;
    @Resource
    protected JobService jobService;
    @Resource
    protected MultiQueue multiQueue;
    @Resource
    protected URLCompleteNotifier urlCompleteNotifier;
    @Resource
    protected JobCompleteNotifier jobCompleteNotifier;

    public MultiQueueMonitor(long checkTimeIntervalMs, long expiredTimeIntervalMs) {
        super("MultiQueueMonitor", checkTimeIntervalMs);
        this.expiredTimeIntervalMs = expiredTimeIntervalMs;
    }

    @Override
    public void execute() throws Exception {
        sweepExpiredJobs();
        sweepExpiredConcurrentUnits();
    }

    /**
     * 清理过期并发单元
     */
    private void sweepExpiredConcurrentUnits() {
        Set<String> concurrentUnits = multiQueue.concurrentUnitsSnapshots();
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
        Long before = System.currentTimeMillis() - expiredTimeIntervalMs;
        List<Job> jobs = jobService.getRunningJobs(before, 100);
        int sweepRecordCount = 0, sweepJobCount = 0;
        for (Job job : jobs) {
            List<URLRecord> expiredRecords = urlService.getJobExpiredRecords(job.jobId, before, 100);
            sweepRecordCount += expiredRecords.size();
            for (URLRecord expiredRecord : expiredRecords) {
                URLRecord record = new URLRecord();
                record.key = expiredRecord.key;
                record.status = Constants.URL_STATUS_TIMEOUT;
                record.createTime = null;
                Context context = new Context();
                context.put(Constants.DARWIN_STAGE, Constants.STAGE_MONITOR);
                urlCompleteNotifier.onComplete(record, context);
            }
            if (jobService.finish(job.jobId)) {
                sweepJobCount++;
                jobCompleteNotifier.onComplete(job.jobId, new Context());
            }
        }
        logger.info("scanning job num[{}], sweeping job num[{}], record num[{}]",
                jobs.size(), sweepJobCount, sweepRecordCount);
    }
}
