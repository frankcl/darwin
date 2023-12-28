package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.queue.multi.MultiQueueStatus;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.PlanMapper;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.impl.ots.JobServiceImpl;
import xin.manong.darwin.service.impl.ots.URLServiceImpl;
import xin.manong.darwin.service.request.JobSearchRequest;
import xin.manong.darwin.service.request.PlanSearchRequest;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MySQL计划服务实现
 *
 * @author frankcl
 * @date 2023-03-15 15:11:01
 */
@Service
public class PlanServiceImpl implements PlanService {

    private static final Logger logger = LoggerFactory.getLogger(PlanServiceImpl.class);

    @Resource
    protected PlanMapper planMapper;
    @Resource
    protected JobService jobService;
    @Resource
    protected URLService urlService;
    @Resource
    protected MultiQueue multiQueue;

    @Override
    public Plan get(String planId) {
        if (StringUtils.isEmpty(planId)) {
            logger.error("plan id is empty");
            throw new RuntimeException("计划ID为空");
        }
        return planMapper.selectById(planId);
    }

    @Override
    public Boolean add(Plan plan) {
        LambdaQueryWrapper<Plan> query = new LambdaQueryWrapper<>();
        query.eq(Plan::getName, plan.name).eq(Plan::getAppId, plan.appId);
        if (planMapper.selectCount(query) > 0) {
            logger.error("plan[{}] has existed for app[{}]", plan.name, plan.appId);
            throw new RuntimeException(String.format("同名计划[%s]已存在", plan.name));
        }
        return planMapper.insert(plan) > 0;
    }

    @Override
    public Boolean update(Plan plan) {
        if (planMapper.selectById(plan.planId) == null) {
            logger.error("plan[{}] is not found", plan.planId);
            return false;
        }
        return planMapper.updateById(plan) > 0;
    }

    @Override
    public Boolean delete(String planId) {
        if (planMapper.selectById(planId) == null) {
            logger.error("plan[{}] is not found", planId);
            return false;
        }
        JobSearchRequest searchRequest = new JobSearchRequest();
        searchRequest.current = 1;
        searchRequest.size = 1;
        searchRequest.planId = planId;
        Pager<Job> pager = jobService.search(searchRequest);
        if (pager.total > 0) {
            logger.error("jobs are not empty for plan[{}]", planId);
            throw new RuntimeException(String.format("计划[%s]中任务不为空", planId));
        }
        return planMapper.deleteById(planId) > 0;
    }

    @Override
    public Pager<Plan> search(PlanSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new PlanSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        LambdaQueryWrapper<Plan> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Plan::getCreateTime);
        if (searchRequest.category != null) query.eq(Plan::getCategory, searchRequest.category);
        if (searchRequest.status != null) query.eq(Plan::getStatus, searchRequest.status);
        if (searchRequest.priority != null) query.eq(Plan::getPriority, searchRequest.priority);
        if (searchRequest.appId != null) query.eq(Plan::getAppId, searchRequest.appId);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like(Plan::getName, searchRequest.name);
        IPage<Plan> page = planMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }

    @Override
    @Transactional
    public Job execute(Plan plan) {
        if (plan == null) return null;
        if (plan.status != Constants.PLAN_STATUS_RUNNING) {
            logger.warn("plan[{}] is not running", plan.planId);
            return null;
        }
        Job job = plan.buildJob();
        List<URLRecord> addRecords = new ArrayList<>(), pushQueueRecords = new ArrayList<>();
        try {
            if (!jobService.add(job)) throw new RuntimeException("添加任务失败");
            for (URLRecord seedURL : job.seedURLs) {
                MultiQueueStatus status = multiQueue.push(seedURL, 3);
                if (status == MultiQueueStatus.OK) pushQueueRecords.add(seedURL);
                logger.info("push record[{}] into queue, status[{}]", seedURL.url,
                        Constants.SUPPORT_URL_STATUSES.get(seedURL.status));
                if (urlService.add(new URLRecord(seedURL))) addRecords.add(seedURL);
                else throw new RuntimeException("添加任务种子失败");
            }
            if (plan.category != Constants.PLAN_CATEGORY_PERIOD) return job;
            if (!updateNextTime(plan.planId, plan.crontabExpression)) {
                logger.warn("update next time failed for plan[{}]", plan.planId);
            }
            return job;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            rollback(pushQueueRecords, addRecords, job.jobId);
            return null;
        }
    }

    /**
     * 更新计划下次调度时间
     *
     * @param planId 计划ID
     * @param crontabExpression crontab表达式
     * @return 更新成功返回true，否则返回false
     */
    private Boolean updateNextTime(String planId, String crontabExpression) throws ParseException {
        Date date = new Date();
        Plan plan = new Plan();
        plan.planId = planId;
        plan.updateTime = date.getTime();
        plan.nextTime = new CronExpression(crontabExpression).getNextValidTimeAfter(date).getTime();
        return update(plan);
    }

    /**
     * 回滚数据
     *
     * @param pushQueueRecords 进入MultiQueue数据
     * @param addRecords 添加数据库成功数据
     * @param jobId 任务ID
     */
    private void rollback(List<URLRecord> pushQueueRecords, List<URLRecord> addRecords, String jobId) {
        for (URLRecord pushQueueRecord : pushQueueRecords) multiQueue.remove(pushQueueRecord);
        if (urlService instanceof URLServiceImpl) for (URLRecord record : addRecords) urlService.delete(record.key);
        if (jobService instanceof JobServiceImpl) jobService.delete(jobId);
    }
}
