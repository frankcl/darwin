package xin.manong.darwin.service.component;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.SeedRecord;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.log.core.AspectLogSupport;
import xin.manong.darwin.queue.ConcurrencyConstants;
import xin.manong.darwin.queue.ConcurrencyQueue;
import xin.manong.darwin.queue.PushResult;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.SeedService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.weapon.base.common.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * 计划执行器
 *
 * @author frankcl
 * @date 2025-04-22 07:55:33
 */
@Component
public class PlanExecutor {

    private static final Logger logger = LoggerFactory.getLogger(PlanExecutor.class);

    @Resource
    private JobService jobService;
    @Resource
    private URLService urlService;
    @Resource
    private SeedService seedService;
    @Resource
    private ConcurrencyComputer concurrencyComputer;
    @Resource
    private ConcurrencyQueue concurrencyQueue;
    @Resource
    private AspectLogSupport aspectLogSupport;

    /**
     * 执行前检测
     *
     * @return 如果Redis内存处于danger状态则检测不通过
     */
    public boolean checkBeforeExecute() {
        int memoryWaterLevel = concurrencyQueue.getMemoryWaterLevel();
        if (memoryWaterLevel == ConcurrencyConstants.MEMORY_WATER_LEVEL_DANGER) {
            logger.warn("Redis memory water level is in danger, can't push");
            return false;
        }
        return true;
    }

    public boolean execute(Plan plan) {
        if (!plan.status) {
            logger.warn("Plan:{} is not opened", plan.planId);
            return false;
        }
        Job job = Converter.convert(plan);
        List<URLRecord> pushRecords = new ArrayList<>(), commitRecords = new ArrayList<>();
        try {
            if (!jobService.add(job)) throw new IllegalStateException("添加任务失败");
            handleSeeds(plan, job, pushRecords, commitRecords);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            rollback(job.jobId, pushRecords, commitRecords);
            return false;
        }
    }

    /**
     * 处理任务种子
     *
     * @param plan 计划
     * @param job 任务
     * @param pushRecords 成功推送并发队列数据
     * @param commitRecords 成功提交数据库数据
     */
    private void handleSeeds(Plan plan, Job job,
                             List<URLRecord> pushRecords,
                             List<URLRecord> commitRecords) {
        List<SeedRecord> seedRecords = seedService.getList(plan.planId);
        for (SeedRecord seedRecord : seedRecords) {
            Context context = new Context();
            context.put(Constants.DARWIN_STAGE, Constants.PROCESS_STAGE_PUSH);
            URLRecord record = Converter.convert(seedRecord);
            try {
                concurrencyComputer.compute(record);
                record.appId = plan.appId;
                record.jobId = job.jobId;
                if (record.fetchMethod == null) record.fetchMethod = job.fetchMethod;
                if (record.priority == null) {
                    record.priority = job.priority == null ? Constants.PRIORITY_NORMAL : job.priority;
                }
                pushRecord(record, commitRecords, pushRecords);
            } finally {
                aspectLogSupport.commitAspectLog(context, record);
            }
        }
    }

    /**
     * 推送任务种子
     *
     * @param record 种子
     * @param commitRecords 成功提交数据库数据
     * @param pushRecords 成功推送并发队列数据
     */
    private void pushRecord(URLRecord record, List<URLRecord> commitRecords, List<URLRecord> pushRecords) {
        PushResult pushResult = concurrencyQueue.push(record, 3);
        if (pushResult != PushResult.SUCCESS) {
            logger.error("Push seed:{} into concurrency queue failed, push result is {}",
                    record.url, pushResult.name());
            throw new IllegalStateException("添加种子到并发队列失败");
        }
        pushRecords.add(record);
        if (!urlService.add(new URLRecord(record))) {
            logger.error("Add seed:{} into database failed", record.url);
            throw new IllegalStateException("添加种子到数据库失败");
        }
        commitRecords.add(record);
    }

    /**
     * 添加种子失败后回滚
     * 1. 回滚添加到ConcurrencyQueue数据
     * 2. 回滚添加到数据库数据
     * 3. 回滚任务
     *
     * @param jobId 任务ID
     * @param pushRecords 成功推送并发队列数据
     * @param commitRecords 成功提交数据库数据
     */
    private void rollback(String jobId, List<URLRecord> pushRecords, List<URLRecord> commitRecords) {
        for (URLRecord pushRecord : pushRecords) concurrencyQueue.remove(pushRecord);
        for (URLRecord commitRecord : commitRecords) urlService.delete(commitRecord.key);
        jobService.delete(jobId);
    }
}
