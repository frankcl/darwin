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
import xin.manong.darwin.common.util.DarwinUtil;
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
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.log.JSONLogger;

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
    @Resource(name = "recordAspectLogger")
    protected JSONLogger aspectLogger;

    @Override
    public Plan get(String planId) {
        if (StringUtils.isEmpty(planId)) {
            logger.error("plan id is empty");
            throw new IllegalArgumentException("计划ID为空");
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
    public boolean execute(Plan plan) {
        if (plan == null) return false;
        if (plan.status != Constants.PLAN_STATUS_RUNNING) {
            logger.warn("plan[{}] is not running", plan.planId);
            return false;
        }
        Job job = plan.buildJob();
        List<URLRecord> pushDBRecords = new ArrayList<>();
        List<URLRecord> pushQueueRecords = new ArrayList<>();
        try {
            if (!jobService.add(job)) throw new RuntimeException("添加任务失败");
            for (URLRecord seedURL : job.seedURLs) {
                pushJobRecord(seedURL, pushDBRecords, pushQueueRecords);
                commitAspectLog(seedURL);
            }
            if (plan.category != Constants.PLAN_CATEGORY_PERIOD) return true;
            updateNextTime(plan.planId, plan.crontabExpression);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            rollBackJob(pushQueueRecords, pushDBRecords, job.jobId);
            return false;
        }
    }

    /**
     * 推送任务种子数据
     *
     * @param record 种子数据
     * @param pushDBRecords 推送数据库成功数据列表
     * @param pushQueueRecords 推送队列成功数据列表
     */
    private void pushJobRecord(URLRecord record, List<URLRecord> pushDBRecords,
                               List<URLRecord> pushQueueRecords) {
        MultiQueueStatus status = multiQueue.push(record, 3);
        if (status == MultiQueueStatus.OK) pushQueueRecords.add(record);
        logger.info("push record[{}] into queue, status[{}]", record.url,
                Constants.SUPPORT_URL_STATUSES.get(record.status));
        if (urlService.add(new URLRecord(record))) pushDBRecords.add(record);
        else throw new RuntimeException("添加任务种子失败");
    }

    /**
     * 更新计划下次调度时间
     *
     * @param planId 计划ID
     * @param crontabExpression crontab表达式
     */
    private void updateNextTime(String planId, String crontabExpression) throws ParseException {
        Date date = new Date();
        Plan plan = new Plan();
        plan.planId = planId;
        plan.updateTime = date.getTime();
        plan.nextTime = new CronExpression(crontabExpression).getNextValidTimeAfter(date).getTime();
        if (!update(plan)) logger.warn("update next time failed for plan[{}]", planId);
    }

    /**
     * 回滚任务
     * 1. 回滚添加到MultiQueue数据
     * 2. 回滚添加到数据库数据
     * 3. 回滚添加任务
     *
     * @param pushQueueRecords 推送队列成功数据列表
     * @param pushDBRecords 推送数据库成功数据列表
     * @param jobId 任务ID
     */
    private void rollBackJob(List<URLRecord> pushQueueRecords, List<URLRecord> pushDBRecords, String jobId) {
        for (URLRecord pushQueueRecord : pushQueueRecords) multiQueue.remove(pushQueueRecord);
        if (urlService instanceof URLServiceImpl) for (URLRecord record : pushDBRecords) urlService.delete(record.key);
        if (jobService instanceof JobServiceImpl) jobService.delete(jobId);
    }

    /**
     * 提交URL记录切面日志
     *
     * @param record URL记录
     */
    private void commitAspectLog(URLRecord record) {
        if (record == null || aspectLogger == null) return;
        Context context = new Context();
        context.put(Constants.DARWIN_STAGE, Constants.STAGE_PUSH);
        DarwinUtil.putContext(context, record);
        aspectLogger.commit(context.getFeatureMap());
    }
}
