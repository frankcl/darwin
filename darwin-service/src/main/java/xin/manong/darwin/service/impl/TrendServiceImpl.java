package xin.manong.darwin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.model.Trend;
import xin.manong.darwin.service.dao.mapper.TrendMapper;
import xin.manong.darwin.service.iface.TrendService;

import java.util.List;

/**
 * 首页大盘统计服务实现
 *
 * @author frankcl
 * @date 2025-04-23 15:56:34
 */
@Service
public class TrendServiceImpl implements TrendService {

    @Resource
    private TrendMapper trendMapper;

    @Override
    public Trend get(String key, int category) {
        LambdaQueryWrapper<Trend> query = new LambdaQueryWrapper<>();
        query.eq(Trend::getCategory, category);
        query.eq(Trend::getKey, key);
        return trendMapper.selectOne(query, false);
    }

    @Override
    public List<Trend> between(String startKey, String endKey, int category) {
        LambdaQueryWrapper<Trend> query = new LambdaQueryWrapper<>();
        query.eq(Trend::getCategory, category);
        query.ge(Trend::getKey, startKey);
        query.le(Trend::getKey, endKey);
        query.orderByAsc(Trend::getKey);
        return trendMapper.selectList(query);
    }

    @Override
    public boolean upsert(Trend trend) {
        Trend prev = get(trend.key, trend.category);
        if (prev != null) {
            LambdaUpdateWrapper<Trend> update = new LambdaUpdateWrapper<>();
            update.eq(Trend::getCategory, trend.category);
            update.eq(Trend::getKey, trend.key);
            update.set(Trend::getValues, JSON.toJSONString(trend.values));
            return trendMapper.update(null, update) > 0;
        }
        return trendMapper.insert(trend) > 0;
    }

    @Override
    public void delete(String maxKey) {
        LambdaQueryWrapper<Trend> query = new LambdaQueryWrapper<>();
        query.lt(Trend::getKey, maxKey);
        trendMapper.delete(query);
    }
}
