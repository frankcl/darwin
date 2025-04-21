package xin.manong.darwin.runner.core;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.queue.ConcurrencyQueue;
import xin.manong.darwin.queue.ConcurrencyConstants;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.weapon.base.common.Context;

import java.util.*;

/**
 * 周期型计划调度器
 *
 * @author frankcl
 * @date 2023-03-22 16:25:08
 */
public class PlanExecutor extends AspectLogSupport {

    private static final Logger logger = LoggerFactory.getLogger(PlanExecutor.class);
    public static final String ID = "plan_scheduler";

    private int memoryWaterLevel = ConcurrencyConstants.MEMORY_WATER_LEVEL_UNKNOWN;

    @Resource
    protected PlanService planService;
    @Resource
    protected ConcurrencyQueue concurrencyQueue;

    public PlanExecutor(Long executeIntervalMs) {
        super(ID, executeIntervalMs);
        this.setName("周期型计划调度器");
        this.setDescription("负责调度周期型计划，生成数据抓取任务和推送种子链接到多级队列");
    }

    @Override
    public void execute() {
        if (!concurrencyQueue.acquirePushLock()) {
            logger.info("Acquired MultiLevelQueue lock for pushing failed");
            return;
        }
        try {
            int pageNum = 1, pageSize = 100;
            Set<String> planIds = new HashSet<>();
            while (true) {
                memoryWaterLevel = ConcurrencyConstants.MEMORY_WATER_LEVEL_UNKNOWN;
                List<Plan> plans = planService.getOpenPlanList(pageNum++, pageSize);
                if (plans == null || plans.isEmpty()) break;
                for (Plan plan : plans) {
                    if (planIds.contains(plan.planId)) continue;
                    planIds.add(plan.planId);
                    execute(plan);
                }
                if (plans.size() < pageSize) break;
            }
        } finally {
            concurrencyQueue.releasePushLock();
        }
    }

    /**
     * 执行周期型计划
     *
     * @param plan 周期性计划
     */
    private void execute(Plan plan) {
        if (plan.nextTime != null && plan.nextTime > System.currentTimeMillis()) return;
        if (memoryWaterLevel == ConcurrencyConstants.MEMORY_WATER_LEVEL_UNKNOWN ||
                memoryWaterLevel >= ConcurrencyConstants.MEMORY_WATER_LEVEL_WARNING) {
            memoryWaterLevel = concurrencyQueue.getMemoryWaterLevel();
            if (memoryWaterLevel == ConcurrencyConstants.MEMORY_WATER_LEVEL_DANGER) {
                logger.warn("Redis memory water level is in danger, can't push");
                return;
            }
        }
        Context context = new Context();
        try {
            if (!planService.execute(plan)) {
                context.put(Constants.BUILD_STATUS, Constants.BUILD_STATUS_FAIL);
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "执行周期型计划失败");
                logger.error("Execute plan failed for plan id:{}", plan.planId);
                return;
            }
            context.put(Constants.BUILD_STATUS, Constants.BUILD_STATUS_SUCCESS);
            logger.info("Execute plan success for plan id:{}", plan.planId);
        } catch (Exception e) {
            context.put(Constants.BUILD_STATUS, Constants.BUILD_STATUS_FAIL);
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "执行周期型计划异常");
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("Execute plan error for plan_id:{}", plan.planId);
            logger.error(e.getMessage(), e);
        } finally {
            commitAspectLog(context, plan);
        }
    }
}
