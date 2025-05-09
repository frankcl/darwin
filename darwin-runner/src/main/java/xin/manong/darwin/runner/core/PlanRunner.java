package xin.manong.darwin.runner.core;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.log.core.AspectLogSupport;
import xin.manong.darwin.queue.ConcurrencyQueue;
import xin.manong.darwin.service.component.PlanExecutor;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.executor.ExecuteRunner;

import java.util.*;

/**
 * 周期型计划调度器
 *
 * @author frankcl
 * @date 2023-03-22 16:25:08
 */
public class PlanRunner extends ExecuteRunner {

    private static final Logger logger = LoggerFactory.getLogger(PlanRunner.class);
    public static final String ID = "plan_runner";

    @Resource
    private PlanService planService;
    @Resource
    private PlanExecutor planExecutor;
    @Resource
    private ConcurrencyQueue concurrencyQueue;
    @Resource
    private AspectLogSupport aspectLogSupport;

    public PlanRunner(Long executeIntervalMs) {
        super(ID, executeIntervalMs);
        this.setName("周期型计划运行器");
        this.setDescription("负责调度周期型计划，生成数据抓取任务和推送种子链接到并发队列");
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
                List<Plan> plans = planService.getOpenPlanList(pageNum++, pageSize);
                if (plans == null || plans.isEmpty()) break;
                boolean checkQueue = false;
                for (Plan plan : plans) {
                    if (plan.nextTime != null && plan.nextTime > System.currentTimeMillis()) continue;
                    if (!checkQueue && !(checkQueue = planExecutor.checkBeforeExecute())) continue;
                    if (planIds.contains(plan.planId)) continue;
                    planIds.add(plan.planId);
                    run(plan);
                }
                if (plans.size() < pageSize) break;
            }
        } finally {
            concurrencyQueue.releasePushLock();
        }
    }

    /**
     * 运行周期型计划
     *
     * @param plan 周期性计划
     */
    private void run(Plan plan) {
        Context context = new Context();
        try {
            if (!planExecutor.execute(plan)) {
                context.put(Constants.BUILD_STATUS, Constants.BUILD_STATUS_FAIL);
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "执行周期型计划失败");
                logger.error("Execute plan failed for plan id:{}", plan.planId);
                return;
            }
            planService.updateNextTime(plan, System.currentTimeMillis());
            context.put(Constants.BUILD_STATUS, Constants.BUILD_STATUS_SUCCESS);
            logger.info("Execute plan success for plan id:{}", plan.planId);
        } catch (Exception e) {
            context.put(Constants.BUILD_STATUS, Constants.BUILD_STATUS_FAIL);
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "执行周期型计划异常");
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("Execute plan error for plan_id:{}", plan.planId);
            logger.error(e.getMessage(), e);
        } finally {
            aspectLogSupport.commitAspectLog(context, plan);
        }
    }
}
