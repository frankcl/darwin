package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.model.App;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.AppMapper;
import xin.manong.darwin.service.iface.AppService;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.request.PlanSearchRequest;

import javax.annotation.Resource;

/**
 * MySQL应用服务实现
 *
 * @author frankcl
 * @date 2023-03-21 20:16:33
 */
@Service
public class AppServiceImpl implements AppService {

    private static final Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);

    @Resource
    protected AppMapper appMapper;
    @Resource
    protected PlanService planService;

    @Override
    public Boolean add(App app) {
        LambdaQueryWrapper<App> query = new LambdaQueryWrapper<>();
        query.eq(App::getName, app.name);
        if (appMapper.selectCount(query) > 0) {
            logger.error("app has existed for same name[{}]", app.name);
            throw new RuntimeException(String.format("同名应用[%s]已存在", app.name));
        }
        return appMapper.insert(app) > 0;
    }

    @Override
    public Boolean update(App app) {
        if (appMapper.selectById(app.id) == null) {
            logger.error("app[{}] is not found", app.id);
            return false;
        }
        return appMapper.updateById(app) > 0;
    }

    @Override
    public Boolean delete(Integer id) {
        if (appMapper.selectById(id) == null) {
            logger.error("app[{}] is not found", id);
            return false;
        }
        PlanSearchRequest searchRequest = new PlanSearchRequest();
        searchRequest.current = 1;
        searchRequest.size = 1;
        searchRequest.appId = id;
        Pager<Plan> pager = planService.search(searchRequest);
        if (pager.total > 0) {
            logger.error("plans are not empty for app[{}]", id);
            throw new RuntimeException(String.format("应用[%s]中计划不为空", id));
        }
        return appMapper.deleteById(id) > 0;
    }

    @Override
    public App get(Integer id) {
        if (id == null) {
            logger.error("app id is null");
            throw new RuntimeException("应用ID为空");
        }
        return appMapper.selectById(id);
    }

    @Override
    public Pager<App> getList(int current, int size) {
        LambdaQueryWrapper<App> query = new LambdaQueryWrapper<>();
        query.orderByDesc(App::getCreateTime).orderByAsc(App::getName);
        IPage<App> page = appMapper.selectPage(new Page<>(current, size), query);
        return Converter.convert(page);
    }

    @Override
    public Pager<App> search(String name, int current, int size) {
        LambdaQueryWrapper<App> query = new LambdaQueryWrapper<>();
        query.orderByDesc(App::getCreateTime);
        if (!StringUtils.isEmpty(name)) query.like(App::getName, name);
        IPage<App> page = appMapper.selectPage(new Page<>(current, size), query);
        return Converter.convert(page);
    }
}
