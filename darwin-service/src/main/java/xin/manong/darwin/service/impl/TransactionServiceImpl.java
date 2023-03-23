package xin.manong.darwin.service.impl;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.TransactionService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.impl.ots.JobServiceImpl;
import xin.manong.darwin.service.impl.ots.URLServiceImpl;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 事务型服务实现
 *
 * @author frankcl
 * @date 2023-03-23 11:17:52
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Resource
    protected PlanService planService;
    @Resource
    protected JobService jobService;
    @Resource
    protected URLService urlService;

    @Override
    @Transactional
    public Job buildJobRepeatedPlan(Plan plan) {
        if (plan == null || plan.category != Constants.PLAN_CATEGORY_REPEAT) {
            logger.error("plan is null or is not repeated plan");
            return null;
        }
        Job job = null;
        try {
            job = plan.buildJob();
            if (!jobService.add(job)) {
                throw new RuntimeException(String.format("add job[%s] failed for repeated plan[%s]",
                        job.name, plan.planId));
            }
            for (URLRecord seedURL : job.seedURLs) {
                if (urlService.add(seedURL)) continue;
                throw new RuntimeException(String.format("add seed record failed for job[%s]", job.jobId));
            }
            Plan updatePlan = new Plan();
            updatePlan.planId = plan.planId;
            updatePlan.updateTime = System.currentTimeMillis();
            updatePlan.nextTime = new CronExpression(plan.crontabExpression).getNextValidTimeAfter(new Date()).getTime();
            if (!planService.update(updatePlan)) {
                throw new RuntimeException(String.format("update next time failed for repeated plan[%s]",
                        updatePlan.planId));
            }
            return job;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            if (job != null && urlService instanceof URLServiceImpl) {
                for (URLRecord seedURL : job.seedURLs) urlService.delete(seedURL.key);
            }
            if (job != null && jobService instanceof JobServiceImpl) jobService.delete(job.jobId);
            return null;
        }
    }
}
