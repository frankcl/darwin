package xin.manong.darwin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.model.Dashboard;
import xin.manong.darwin.service.dao.mapper.DashboardMapper;
import xin.manong.darwin.service.iface.DashboardService;

import java.util.List;

/**
 * 首页大盘统计服务实现
 *
 * @author frankcl
 * @date 2025-04-23 15:56:34
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private DashboardMapper dashboardMapper;

    @Override
    public Dashboard get(String hour, int category) {
        LambdaQueryWrapper<Dashboard> query = new LambdaQueryWrapper<>();
        query.eq(Dashboard::getCategory, category);
        query.eq(Dashboard::getHour, hour);
        return dashboardMapper.selectOne(query, false);
    }

    @Override
    public List<Dashboard> betweenList(String fromHour, String toHour, int category) {
        LambdaQueryWrapper<Dashboard> query = new LambdaQueryWrapper<>();
        query.eq(Dashboard::getCategory, category);
        query.ge(Dashboard::getHour, fromHour);
        query.le(Dashboard::getHour, toHour);
        query.orderByAsc(Dashboard::getHour);
        return dashboardMapper.selectList(query);
    }

    @Override
    public boolean upsert(Dashboard dashboard) {
        Dashboard prev = get(dashboard.hour, dashboard.category);
        if (prev != null) {
            LambdaUpdateWrapper<Dashboard> update = new LambdaUpdateWrapper<>();
            update.eq(Dashboard::getCategory, dashboard.category);
            update.eq(Dashboard::getHour, dashboard.hour);
            update.set(Dashboard::getValues, JSON.toJSONString(dashboard.values));
            return dashboardMapper.update(null, update) > 0;
        }
        return dashboardMapper.insert(dashboard) > 0;
    }

    @Override
    public void delete(String beforeHour) {
        LambdaQueryWrapper<Dashboard> query = new LambdaQueryWrapper<>();
        query.lt(Dashboard::getHour, beforeHour);
        dashboardMapper.delete(query);
    }
}
