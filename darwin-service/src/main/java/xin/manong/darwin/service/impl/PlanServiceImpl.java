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
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.PlanMapper;
import xin.manong.darwin.service.iface.*;
import xin.manong.darwin.service.request.JobSearchRequest;
import xin.manong.darwin.service.request.PlanSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

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
    private PlanMapper planMapper;
    @Resource
    @Lazy
    private RuleService ruleService;
    @Resource
    @Lazy
    private JobService jobService;

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
        if (plan.category != Constants.PLAN_CATEGORY_PERIOD) throw new IllegalStateException("单次型计划不支持设置下次调度时间");
        try {
            long currentTime = System.currentTimeMillis();
            Date date = new Date(baseTime == null ? currentTime : baseTime);
            long nextTime = new CronExpression(plan.crontabExpression).getNextValidTimeAfter(date).getTime();
            LambdaUpdateWrapper<Plan> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Plan::getPlanId, plan.planId).
                    set(Plan::getUpdateTime, currentTime).
                    set(Plan::getNextTime, nextTime);
            if (planMapper.update(updateWrapper) <= 0) logger.warn("Update next time failed for plan:{}", plan.planId);
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
        if (!ruleService.deleteRules(planId)) throw new IllegalStateException("删除规则失败");
        return planMapper.deleteById(planId) > 0;
    }

    @Override
    public Pager<Plan> search(PlanSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new PlanSearchRequest();
        if (searchRequest.pageNum == null || searchRequest.pageNum < 1) searchRequest.pageNum = Constants.DEFAULT_PAGE_NUM;
        if (searchRequest.pageSize == null || searchRequest.pageSize <= 0) searchRequest.pageSize = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(Plan.class, searchRequest);
        searchRequest.appList = ModelValidator.validateListField(searchRequest.appIds, String.class);
        if (searchRequest.appList != null && searchRequest.appList.isEmpty()) {
            return Pager.empty(searchRequest.pageNum, searchRequest.pageSize);
        }
        QueryWrapper<Plan> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (searchRequest.category != null) query.eq("category", searchRequest.category);
        if (searchRequest.status != null) query.eq("status", searchRequest.status);
        if (searchRequest.priority != null) query.eq("priority", searchRequest.priority);
        if (searchRequest.fetchMethod != null) query.eq("fetch_method", searchRequest.fetchMethod);
        if (searchRequest.appId != null) query.eq("app_id", searchRequest.appId);
        if (!StringUtils.isEmpty(searchRequest.name)) {
            query.like("name", searchRequest.name).or().eq("plan_id", searchRequest.name);
        }
        if (searchRequest.appList != null) query.in("app_id", searchRequest.appList);
        IPage<Plan> page = planMapper.selectPage(new Page<>(searchRequest.pageNum, searchRequest.pageSize), query);
        return Converter.convert(page);
    }

    @Override
    public List<Plan> getOpenPlanList(int pageNum, int pageSize) {
        PlanSearchRequest searchRequest = new PlanSearchRequest();
        searchRequest.category = Constants.PLAN_CATEGORY_PERIOD;
        searchRequest.status = true;
        searchRequest.pageNum = pageNum;
        searchRequest.pageSize = pageSize;
        Pager<Plan> pager = search(searchRequest);
        if (pager == null || pager.records == null) return new ArrayList<>();
        return pager.records;
    }
}
