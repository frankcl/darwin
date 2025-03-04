package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.App;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.AppMapper;
import xin.manong.darwin.service.iface.AppService;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.request.AppSearchRequest;
import xin.manong.darwin.service.request.PlanSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

/**
 * MySQL应用服务实现
 *
 * @author frankcl
 * @date 2023-03-21 20:16:33
 */
@Service
public class AppServiceImpl implements AppService {

    @Resource
    protected AppMapper appMapper;
    @Resource
    protected PlanService planService;

    @Override
    public boolean add(App app) {
        LambdaQueryWrapper<App> query = new LambdaQueryWrapper<>();
        query.eq(App::getName, app.name);
        if (appMapper.selectCount(query) > 0) throw new IllegalStateException("应用已存在");
        return appMapper.insert(app) > 0;
    }

    @Override
    public boolean update(App app) {
        if (appMapper.selectById(app.id) == null) throw new NotFoundException("应用不存在");
        return appMapper.updateById(app) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        if (appMapper.selectById(id) == null) throw new NotFoundException("应用不存在");
        PlanSearchRequest searchRequest = new PlanSearchRequest();
        searchRequest.current = 1;
        searchRequest.size = 1;
        searchRequest.appId = id;
        Pager<Plan> pager = planService.search(searchRequest);
        if (pager.total > 0) throw new IllegalStateException("应用存在计划列表");
        return appMapper.deleteById(id) > 0;
    }

    @Override
    public App get(Integer id) {
        if (id == null) throw new BadRequestException("应用ID为空");
        return appMapper.selectById(id);
    }

    @Override
    public Pager<App> search(AppSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new AppSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(App.class, searchRequest);
        QueryWrapper<App> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (StringUtils.isNotEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        IPage<App> page = appMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
