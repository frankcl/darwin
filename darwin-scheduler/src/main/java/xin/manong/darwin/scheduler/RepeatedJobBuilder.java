package xin.manong.darwin.scheduler;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.queue.multi.MultiQueueConstants;
import xin.manong.darwin.queue.multi.MultiQueueStatus;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.TransactionService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.request.PlanSearchRequest;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.log.JSONLogger;

import javax.annotation.Resource;
import java.util.*;

/**
 * 周期性任务构建器
 *
 * @author frankcl
 * @date 2023-03-22 16:25:08
 */
public class RepeatedJobBuilder implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RepeatedJobBuilder.class);

    private int lastMemoryLevel = -1;
    private boolean running;
    private Thread thread;
    @Resource
    protected RepeatedJobBuilderConfig config;
    @Resource
    protected URLService urlService;
    protected PlanService planService;
    @Resource
    protected TransactionService transactionService;
    @Resource
    protected MultiQueue multiQueue;
    @Resource(name = "buildAspectLogger")
    protected JSONLogger aspectLogger;

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
            if (!multiQueue.tryLockInQueue()) {
                logger.info("acquire in queue lock failed");
                waitMoment();
                continue;
            }
            try {
                logger.info("begin building repeated jobs");
                int current = 1, size = 100;
                long startTime = System.currentTimeMillis();
                Set<String> processedIds = new HashSet<>();
                while (true) {
                    try {
                        lastMemoryLevel = -1;
                        Pager<Plan> pager = getPlans(current, size);
                        for (Plan plan : pager.records) {
                            if (processedIds.contains(plan.planId)) continue;
                            processedIds.add(plan.planId);
                            buildJobRepeatedPlan(plan);
                        }
                        if (pager.records == null || pager.records.isEmpty() || pager.records.size() < size) break;
                    } catch (Exception e) {
                        logger.error("building repeated jobs failed, cause[{}]", e.getMessage());
                        logger.error(e.getMessage(), e);
                    } finally {
                        current++;
                    }
                }
                long spendTime = System.currentTimeMillis() - startTime;
                logger.info("finish building repeated jobs, process plan num[{}], spend time[{}]ms",
                        processedIds.size(), spendTime);
            } finally {
                multiQueue.unlockInQueue();
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
     * 根据周期性计划构建任务
     *
     * @param plan 周期性计划
     * @return 成功返回true，否则返回false
     */
    private boolean buildJobRepeatedPlan(Plan plan) {
        Context context = null;
        try {
            if (plan.nextTime != null && plan.nextTime > 0L &&
                    plan.nextTime > System.currentTimeMillis()) return false;
            logger.info("begin building job for repeated plan[{}]", plan.planId);
            if (lastMemoryLevel == -1 || lastMemoryLevel >= MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_WARN) {
                int currentMemoryLevel = multiQueue.getCurrentMemoryLevel();
                lastMemoryLevel = currentMemoryLevel;
                if (currentMemoryLevel == MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_REFUSED) {
                    logger.warn("multiQueue refuse service");
                    return false;
                }
            }
            context = new Context();
            Job job = transactionService.buildJobRepeatedPlan(plan);
            if (job == null) {
                context.put(Constants.BUILD_STATUS, Constants.BUILD_STATUS_FAIL);
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "构建周期性计划任务失败");
                logger.error("build job failed for repeated plan[{}]", plan.planId);
                return false;
            }
            List<URLRecord> records = pushMultiQueue(job.seedURLs);
            for (URLRecord record : records) {
                commitAspectLog(record);
                if (urlService.updateQueueTime(record)) continue;
                logger.warn("update seed record[{}] failed", record.key);
            }
            context.put(Constants.BUILD_STATUS, Constants.BUILD_STATUS_SUCCESS);
            logger.info("build job success for repeated plan[{}]", plan.planId);
            return true;
        } catch (Exception e) {
            context.put(Constants.BUILD_STATUS, Constants.BUILD_STATUS_FAIL);
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "构建周期性计划任务异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("build job failed for repeated plan[{}]", plan.planId);
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            if (context != null) commitAspectLog(context, plan);
        }
    }

    /**
     * 提交URL记录切面日志
     *
     * @param record URL记录
     */
    private void commitAspectLog(URLRecord record) {
        if (record == null || aspectLogger == null) return;
        Context context = new Context();
        DarwinUtil.putContext(context, record);
        aspectLogger.commit(context.getFeatureMap());
    }

    /**
     * 提交计划记录切面日志
     *
     * @param context 上下文
     * @param plan 计划
     */
    private void commitAspectLog(Context context, Plan plan) {
        if (plan == null || aspectLogger == null) return;
        DarwinUtil.putContext(context, plan);
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
    private List<URLRecord> pushMultiQueue(List<URLRecord> records) {
        List<URLRecord> processRecords = new ArrayList<>();
        if (records == null || records.isEmpty()) return processRecords;
        List<URLRecord> pushRecords = new ArrayList<>(records);
        List<URLRecord> failRecords = new ArrayList<>();
        for (int i = 0; i < config.retryCnt; i++) {
            failRecords.clear();
            List<MultiQueueStatus> statusList = multiQueue.push(pushRecords);
            for (int j = 0; j < statusList.size(); j++) {
                MultiQueueStatus status = statusList.get(j);
                URLRecord record = pushRecords.get(j);
                if (status == MultiQueueStatus.ERROR) {
                    logger.error("push record[{}] error", record.url);
                    record.status = Constants.URL_STATUS_INVALID;
                    processRecords.add(record);
                } else if (status == MultiQueueStatus.REFUSED || status == MultiQueueStatus.FULL) {
                    logger.warn("push record[{}] refused", record.url);
                    failRecords.add(pushRecords.get(j));
                } else {
                    record.status = Constants.URL_STATUS_QUEUING;
                    processRecords.add(record);
                }
            }
            if (failRecords.isEmpty()) break;
            pushRecords = failRecords;
        }
        for (URLRecord record : failRecords) {
            record.status = Constants.URL_STATUS_QUEUING_REFUSED;
            processRecords.add(record);
        }
        return processRecords;
    }
}
