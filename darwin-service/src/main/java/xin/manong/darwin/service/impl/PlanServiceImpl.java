package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.PlanMapper;
import xin.manong.darwin.service.iface.PlanService;

import javax.annotation.Resource;

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
        QueryWrapper<Plan> query = new QueryWrapper<>();
        query.lambda().eq(Plan::getName, plan.name);
        query.lambda().eq(Plan::getAppId, plan.appId);
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
            throw new RuntimeException(String.format("计划[%d]不存在", plan.planId));
        }
        return planMapper.updateById(plan) > 0;
    }

    @Override
    public Boolean delete(String planId) {
        if (planMapper.selectById(planId) == null) {
            logger.error("plan[{}] is not found", planId);
            throw new RuntimeException(String.format("计划[%d]不存在", planId));
        }
        return planMapper.deleteById(planId) > 0;
    }

    @Override
    public Pager<Plan> getList(int current, int size) {
        QueryWrapper<Plan> query = new QueryWrapper<>();
        query.lambda().orderByDesc(Plan::getCreateTime);
        query.lambda().orderByAsc(Plan::getName);
        IPage<Plan> page = planMapper.selectPage(new Page<>(current, size), query);
        return Converter.convert(page);
    }
}
