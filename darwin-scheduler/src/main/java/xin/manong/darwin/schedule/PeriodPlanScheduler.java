package xin.manong.darwin.schedule;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.queue.multi.MultiQueueConstants;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.request.PlanSearchRequest;
import xin.manong.weapon.base.common.Context;

import javax.annotation.Resource;
import java.util.*;

/**
 * 周期性计划调度器
 *
 * @author frankcl
 * @date 2023-03-22 16:25:08
 */
public class PeriodPlanScheduler extends ExecuteRunner {

    private static final Logger logger = LoggerFactory.getLogger(PeriodPlanScheduler.class);

    private int multiQueueMemoryLevel = MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_UNKNOWN;
    @Resource
    protected PlanService planService;
    @Resource
    protected MultiQueue multiQueue;

    public PeriodPlanScheduler(Long executeIntervalMs) {
        super(executeIntervalMs);
    }

    @Override
    public void execute() {
        try {
            if (!multiQueue.tryLockInQueue()) {
                logger.info("acquired in queue lock failed");
                return;
            }
            logger.info("begin building period jobs");
            int n = buildPeriodJobs();
            logger.info("finish building period jobs, involved plan[{}]", n);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            multiQueue.unlockInQueue();
        }
    }

    /**
     * 构建周期性任务
     *
     * @return 构建周期性任务数量
     */
    private int buildPeriodJobs() {
        int current = 1, size = 100;
        Set<String> planIds = new HashSet<>();
        while (true) {
            try {
                multiQueueMemoryLevel = MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_UNKNOWN;
                Pager<Plan> pager = getPeriodPlans(current, size);
                for (Plan plan : pager.records) {
                    if (planIds.contains(plan.planId)) continue;
                    planIds.add(plan.planId);
                    buildPeriodJob(plan);
                }
                if (pager.records == null || pager.records.isEmpty() || pager.records.size() < size) break;
            } catch (Exception e) {
                logger.error("building period jobs failed, cause[{}]", e.getMessage());
                logger.error(e.getMessage(), e);
            } finally {
                current++;
            }
        }
        return planIds.size();
    }

    /**
     * 根据周期性计划构建任务
     *
     * @param plan 周期性计划
     * @return 成功返回true，否则返回false
     */
    private boolean buildPeriodJob(Plan plan) {
        if (plan.nextTime != null && plan.nextTime > 0L && plan.nextTime > System.currentTimeMillis()) return false;
        if (multiQueueMemoryLevel == MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_UNKNOWN ||
                multiQueueMemoryLevel >= MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_WARN) {
            int currentMemoryLevel = multiQueue.getCurrentMemoryLevel();
            multiQueueMemoryLevel = currentMemoryLevel;
            if (currentMemoryLevel == MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_REFUSED) {
                logger.warn("multi queue refused service");
                return false;
            }
        }
        Context context = new Context();
        try {
            logger.info("begin building period job for plan[{}]", plan.planId);
            Job job = planService.execute(plan);
            if (job == null) {
                context.put(Constants.BUILD_STATUS, Constants.BUILD_STATUS_FAIL);
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "构建周期性任务失败");
                logger.error("build period job failed for plan[{}]", plan.planId);
                return false;
            }
            if (job.seedURLs != null) for (URLRecord seedURL : job.seedURLs) commitAspectLog(new Context(), seedURL);
            context.put(Constants.BUILD_STATUS, Constants.BUILD_STATUS_SUCCESS);
            logger.info("build period job success for plan[{}]", plan.planId);
            return true;
        } catch (Exception e) {
            context.put(Constants.BUILD_STATUS, Constants.BUILD_STATUS_FAIL);
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "构建周期性任务异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("build period job failed for plan[{}]", plan.planId);
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            commitAspectLog(context, plan);
        }
    }

    /**
     * 分页获取周期计划
     *
     * @param current 页码
     * @param size 每页数量
     * @return 分页列表
     */
    private Pager<Plan> getPeriodPlans(int current, int size) {
        PlanSearchRequest searchRequest = new PlanSearchRequest();
        searchRequest.category = Constants.PLAN_CATEGORY_PERIOD;
        searchRequest.status = Constants.PLAN_STATUS_RUNNING;
        searchRequest.current = current;
        searchRequest.size = size;
        return planService.search(searchRequest);
    }
}
