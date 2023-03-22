package xin.manong.darwin.scheduler;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.queue.multi.MultiQueueStatus;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.request.PlanSearchRequest;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;

/**
 * 周期性任务构建器
 *
 * @author frankcl
 * @date 2023-03-22 16:25:08
 */
public class RepeatedJobBuilder implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RepeatedJobBuilder.class);

    private boolean running;
    private Thread thread;
    @Resource
    protected RepeatedJobBuilderConfig config;
    @Resource
    protected PlanService planService;
    @Resource
    protected JobService jobService;
    @Resource
    protected MultiQueue multiQueue;

    public RepeatedJobBuilder() {
        this.running = false;
    }

    /**
     * 启动周期任务构建器
     */
    public void start() {
        logger.info("{} is starting ...", this.getClass().getSimpleName());
        running = true;
        thread = new Thread(this, this.getClass().getSimpleName());
        thread.start();
        logger.info("{} has been started", this.getClass().getSimpleName());
    }

    /**
     * 停止周期任务构建器
     */
    public void stop() {
        logger.info("{} is stopping ...", this.getClass().getSimpleName());
        running = false;
        if (thread.isAlive()) thread.interrupt();
        try {
            thread.join();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        logger.info("{} has been stopped", this.getClass().getSimpleName());
    }

    @Override
    public void run() {
        while (running) {
            if (!multiQueue.tryLockQueue(config.multiQueueLockExpiredTimeSeconds)) {
                logger.info("acquire lock failed for multiQueue");
                waitMoment();
                continue;
            }
            try {
                logger.info("begin building repeated jobs");
                int current = 1, size = 100;
                long startTime = System.currentTimeMillis();
                Set<String> processedIds = new HashSet<>();
                while (true) {
                    boolean lockSuccess = false;
                    try {
                        Pager<Plan> pager = getPlans(current, size);
                        for (Plan plan : pager.records) {
                            if (processedIds.contains(plan.planId)) continue;
                            buildRepeatedJob(plan);
                            processedIds.add(plan.planId);
                        }
                        if (pager.records == null || pager.records.isEmpty() || pager.records.size() < size) break;
                        lockSuccess = multiQueue.tryLockQueue(config.multiQueueLockExpiredTimeSeconds);
                    } catch (Exception e) {
                        logger.error("building repeated jobs failed, cause[{}]", e.getMessage());
                        logger.error(e.getMessage(), e);
                    } finally {
                        current++;
                        if (lockSuccess) multiQueue.unlockQueue();
                    }
                }
                long spendTime = System.currentTimeMillis() - startTime;
                logger.info("finish building repeated jobs, process plan num[{}], spend time[{}]ms",
                        processedIds.size(), spendTime);
            } finally {
                multiQueue.unlockQueue();
                waitMoment();
            }
        }
    }

    /**
     * 等待
     */
    private void waitMoment() {
        try {
            Thread.sleep(config.repeatedJobBuildTimeIntervalMs);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * 根据计划构建周期性任务
     *
     * @param plan 计划
     */
    private void buildRepeatedJob(Plan plan) {
        try {
            if (plan.nextTime != null && plan.nextTime > 0L &&
                    plan.nextTime > System.currentTimeMillis()) return;
            logger.info("begin building job for plan[{}]", plan.planId);
            Job job = plan.buildJob();
            if (!jobService.add(job)) {
                logger.error("add job[{}] failed from plan[{}]", job.name, plan.planId);
                return;
            }
            pushMultiQueue(job.seedURLs);
            updateNextTime(plan);
            logger.info("building job success for plan[{}]", plan.planId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 更新计划下次调度时间
     *
     * @param plan 原始计划
     * @throws ParseException
     */
    private void updateNextTime(Plan plan) throws ParseException {
        Plan updatePlan = new Plan();
        updatePlan.planId = plan.planId;
        updatePlan.createTime = null;
        updatePlan.updateTime = System.currentTimeMillis();
        updatePlan.status = null;
        updatePlan.priority = null;
        updatePlan.nextTime = new CronExpression(plan.crontabExpression).getNextValidTimeAfter(new Date()).getTime();
        if (!planService.update(updatePlan)) logger.warn("update next time failed for plan[{}]", updatePlan.planId);
    }

    /**
     * 分页获取周期计划
     *
     * @param current 页码
     * @param size 每页数量
     * @return 分页列表
     */
    private Pager<Plan> getPlans(int current, int size) {
        PlanSearchRequest searchRequest = new PlanSearchRequest();
        searchRequest.category = Constants.PLAN_CATEGORY_REPEAT;
        searchRequest.status = Constants.PLAN_STATUS_RUNNING;
        return planService.search(searchRequest, current, size);
    }

    /**
     * 推送任务种子到多级队列
     *
     * @param records 种子URL列表
     */
    private void pushMultiQueue(List<URLRecord> records) {
        if (records == null || records.isEmpty()) return;
        List<URLRecord> pushRecords = new ArrayList<>(records);
        for (int i = 0; i < config.retryCnt; i++) {
            List<URLRecord> failRecords = new ArrayList<>();
            List<MultiQueueStatus> statusList = multiQueue.push(pushRecords);
            for (int j = 0; j < statusList.size(); j++) {
                MultiQueueStatus status = statusList.get(j);
                if (status == MultiQueueStatus.ERROR) {
                    logger.error("push record[{}] error", pushRecords.get(j).url);
                    continue;
                }
                if (status == MultiQueueStatus.REFUSED) {
                    logger.warn("push record[{}] refused", pushRecords.get(j).url);
                    failRecords.add(pushRecords.get(j));
                }
            }
            if (failRecords.isEmpty()) break;
            pushRecords = failRecords;
        }
    }
}
