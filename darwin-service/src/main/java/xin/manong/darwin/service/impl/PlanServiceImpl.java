package xin.manong.darwin.service.impl;

import com.alibaba.fastjson.JSON;
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
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.iface.URLService;
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
    protected MultiQueue multiQueue;
    @Resource(name = "recordAspectLogger")
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
        if (planMapper.selectCount(query) > 0) throw new IllegalStateException("同名计划已存在");
        return planMapper.insert(plan) > 0;
    }

    @Override
    public boolean update(Plan plan) {
        if (planMapper.selectById(plan.planId) == null) throw new NotFoundException("计划不存在");
        return planMapper.updateById(plan) > 0;
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
        if (plan.ruleIds != null && !plan.ruleIds.isEmpty()) {
            List<Integer> ruleIds = new ArrayList<>(plan.ruleIds);
            for (Integer ruleId : ruleIds) {
                if (!ruleService.delete(ruleId)) throw new IllegalStateException("删除规则失败");
            }
        }
        return planMapper.deleteById(planId) > 0;
    }

    @Override
    public boolean addRule(String planId, Integer ruleId) {
        Plan plan = planMapper.selectById(planId);
        if (plan == null) throw new NotFoundException("计划不存在");
        List<Integer> ruleIds = plan.ruleIds;
        if (ruleIds == null) ruleIds = new ArrayList<>();
        if (ruleIds.contains(ruleId)) throw new IllegalStateException("规则已存在");
        ruleIds.add(ruleId);
        LambdaUpdateWrapper<Plan> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Plan::getPlanId, planId);
        wrapper.set(Plan::getRuleIds, JSON.toJSONString(ruleIds));
        return planMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean removeRule(String planId, Integer ruleId) {
        Plan plan = planMapper.selectById(planId);
        if (plan == null) throw new NotFoundException("计划不存在");
        List<Integer> ruleIds = plan.ruleIds;
        if (ruleIds == null || !ruleIds.contains(ruleId)) throw new IllegalStateException("规则不存在");
        ruleIds.remove(ruleId);
        LambdaUpdateWrapper<Plan> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Plan::getPlanId, planId);
        wrapper.set(Plan::getRuleIds, JSON.toJSONString(ruleIds));
        return planMapper.update(null, wrapper) > 0;
    }

    @Override
    public Pager<Plan> search(PlanSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new PlanSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(Plan.class, searchRequest);
        QueryWrapper<Plan> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (searchRequest.category != null) query.eq("category", searchRequest.category);
        if (searchRequest.status != null) query.eq("status", searchRequest.status);
        if (searchRequest.priority != null) query.eq("priority", searchRequest.priority);
        if (searchRequest.appId != null) query.eq("app_id", searchRequest.appId);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        IPage<Plan> page = planMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(Plan plan) {
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
