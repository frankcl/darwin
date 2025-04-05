package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.queue.multi.MultiQueueStatus;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.PlanMapper;
import xin.manong.darwin.service.iface.*;
import xin.manong.darwin.service.impl.ots.JobServiceImpl;
import xin.manong.darwin.service.impl.ots.URLServiceImpl;
import xin.manong.darwin.service.request.JobSearchRequest;
import xin.manong.darwin.service.request.PlanSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.log.JSONLogger;

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
    @Lazy
    protected RuleService ruleService;
    @Resource
    @Lazy
    protected JobService jobService;
    @Resource
    @Lazy
    protected URLService urlService;
    @Resource
    @Lazy
    protected SeedService seedService;
    @Resource
    @Lazy
    protected MultiQueue multiQueue;
    @Resource(name = "urlAspectLogger")
    protected JSONLogger aspectLogger;

    @Override
    public Plan get(String planId) {
        if (StringUtils.isEmpty(planId)) throw new BadRequestException("计划ID为空");
        return planMapper.selectById(planId);
    }

    @Override
    public boolean add(Plan plan) {
        LambdaQueryWrapper<Plan> query = new LambdaQueryWrapper<>();
        query.eq(Plan::getName, plan.name).eq(Plan::getAppId, plan.appId);
        if (planMapper.selectCount(query) > 0) throw new IllegalStateException("计划已存在");
        return planMapper.insert(plan) > 0;
    }

    @Override
    public boolean update(Plan plan) {
        Plan prevPlan = planMapper.selectById(plan.planId);
        if (prevPlan == null) throw new NotFoundException("计划不存在");
        if (StringUtils.isNotEmpty(plan.name)) {
            LambdaQueryWrapper<Plan> query = new LambdaQueryWrapper<>();
            query.eq(Plan::getName, plan.name).eq(Plan::getAppId, plan.appId).
                    ne(Plan::getName, prevPlan.name).ne(Plan::getAppId, prevPlan.appId);
            if (planMapper.selectCount(query) > 0) throw new IllegalStateException("计划已存在");
        }
        return planMapper.updateById(plan) > 0;
    }

    @Override
    public void updateNextTime(Plan plan, Long baseTime) {
        if (plan.category != Constants.PLAN_CATEGORY_PERIOD) {
            throw new IllegalStateException("单次型计划不支持设置下次调度时间");
        }
        try {
            long currentTime = System.currentTimeMillis();
            Date date = new Date(baseTime == null ? currentTime : baseTime);
            long nextTime = new CronExpression(plan.crontabExpression).getNextValidTimeAfter(date).getTime();
            LambdaUpdateWrapper<Plan> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Plan::getPlanId, plan.planId).set(Plan::getUpdateTime, currentTime).
                    set(Plan::getNextTime, nextTime);
            if (planMapper.update(updateWrapper) <= 0) logger.warn("update next time failed for plan[{}]", plan.planId);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void updateAppName(int appId, String appName) {
        LambdaUpdateWrapper<Plan> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Plan::getAppId, appId).set(Plan::getAppName, appName);
        planMapper.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String planId) {
        Plan plan = planMapper.selectById(planId);
        if (plan == null) throw new NotFoundException("计划不存在");
        JobSearchRequest searchRequest = new JobSearchRequest();
        searchRequest.planId = planId;
        Pager<Job> pager = jobService.search(searchRequest);
        if (pager.total > 0) throw new ForbiddenException("计划任务不为空");
        if (!ruleService.deletePlanRules(planId)) throw new IllegalStateException("删除规则失败");
        return planMapper.deleteById(planId) > 0;
    }

    @Override
    public Pager<Plan> search(PlanSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new PlanSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(Plan.class, searchRequest);
        searchRequest.appList = ModelValidator.validateListField(searchRequest.appIds, String.class);
        QueryWrapper<Plan> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (searchRequest.category != null) query.eq("category", searchRequest.category);
        if (searchRequest.status != null) query.eq("status", searchRequest.status);
        if (searchRequest.priority != null) query.eq("priority", searchRequest.priority);
        if (searchRequest.fetchMethod != null) query.eq("fetch_method", searchRequest.fetchMethod);
        if (searchRequest.appId != null) query.eq("app_id", searchRequest.appId);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        if (searchRequest.appList != null) query.in("app_id", searchRequest.appList);
        IPage<Plan> page = planMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(Plan plan) {
        if (plan.status == null || !plan.status) {
            logger.warn("plan[{}] is not opened", plan.planId);
            return false;
        }
        Job job = plan.buildJob();
        List<URLRecord> databaseRecords = new ArrayList<>();
        List<URLRecord> queueRecords = new ArrayList<>();
        try {
            if (!jobService.add(job)) throw new RuntimeException("添加任务失败");
            List<SeedRecord> seedRecords = seedService.getList(plan.planId);
            for (SeedRecord seedRecord : seedRecords) {
                URLRecord record = Converter.convert(seedRecord);
                record.appId = plan.appId;
                record.jobId = job.jobId;
                if (record.fetchMethod == null) record.fetchMethod = job.fetchMethod;
                if (record.concurrentLevel == null) record.concurrentLevel = Constants.CONCURRENT_LEVEL_DOMAIN;
                if (record.priority == null) record.priority = job.priority == null ? Constants.PRIORITY_NORMAL : job.priority;
                pushQueue(record, databaseRecords, queueRecords);
                commitAspectLog(record);
            }
            if (plan.category != Constants.PLAN_CATEGORY_PERIOD) return true;
            updateNextTime(plan, System.currentTimeMillis());
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            rollbackJob(queueRecords, databaseRecords, job.jobId);
            return false;
        }
    }

    /**
     * 推送任务种子数据
     *
     * @param record 种子数据
     * @param databaseRecords 推送数据库成功数据列表
     * @param queueRecords 推送队列成功数据列表
     */
    private void pushQueue(URLRecord record, List<URLRecord> databaseRecords, List<URLRecord> queueRecords) {
        MultiQueueStatus status = multiQueue.push(record, 3);
        if (status != MultiQueueStatus.OK) {
            logger.error("push record[{}] into queue failed, status[{}]", record.url,
                    Constants.SUPPORT_URL_STATUSES.get(record.status));
            throw new IllegalStateException("添加任务种子到队列失败");
        }
        queueRecords.add(record);
        if (!urlService.add(new URLRecord(record))) throw new IllegalStateException("添加任务种子到数据库失败");
        databaseRecords.add(record);
    }

    /**
     * 回滚任务
     * 1. 回滚添加到MultiQueue数据
     * 2. 回滚添加到数据库数据
     * 3. 回滚添加任务
     *
     * @param queueRecords 推送队列成功数据列表
     * @param databaseRecords 推送数据库成功数据列表
     * @param jobId 任务ID
     */
    private void rollbackJob(List<URLRecord> queueRecords, List<URLRecord> databaseRecords, String jobId) {
        for (URLRecord record : queueRecords) multiQueue.remove(record);
        if (urlService instanceof URLServiceImpl) for (URLRecord record : databaseRecords) urlService.delete(record.key);
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
