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
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.App;
import xin.manong.darwin.common.model.AppUser;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.AppMapper;
import xin.manong.darwin.service.iface.AppService;
import xin.manong.darwin.service.iface.AppUserService;
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
    protected AppUserService appUserService;
    @Resource
    protected PlanService planService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(App app) {
        LambdaQueryWrapper<App> query = new LambdaQueryWrapper<>();
        query.eq(App::getName, app.name);
        if (appMapper.selectCount(query) > 0) throw new IllegalStateException("应用已存在");
        boolean success = appMapper.insert(app) > 0;
        if (success) {
            AppUser appUser = new AppUser();
            appUser.appId = app.id;
            appUser.userId = app.creatorId;
            appUser.nickName = app.creator;
            if (!appUserService.add(appUser)) throw new IllegalStateException("增加应用用户关系失败");
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(App app) {
        App prevApp = appMapper.selectById(app.id);
        if (prevApp == null) throw new NotFoundException("应用不存在");
        if (StringUtils.isNotEmpty(app.name)) {
            LambdaQueryWrapper<App> query = new LambdaQueryWrapper<>();
            query.eq(App::getName, app.name).ne(App::getName, prevApp.name);
            if (appMapper.selectCount(query) > 0) throw new IllegalStateException("应用已存在");
            if (!app.name.equals(prevApp.name)) planService.updateAppName(app.id, app.name);
        }
        return appMapper.updateById(app) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Integer id) {
        if (appMapper.selectById(id) == null) throw new NotFoundException("应用不存在");
        PlanSearchRequest searchRequest = new PlanSearchRequest();
        searchRequest.current = 1;
        searchRequest.size = 1;
        searchRequest.appId = id;
        Pager<Plan> pager = planService.search(searchRequest);
        if (pager.total > 0) throw new IllegalStateException("应用存在计划列表");
        if (!appUserService.deleteByApp(id)) throw new IllegalStateException("删除应用关联用户关系失败");
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
        searchRequest.appList = ModelValidator.validateListField(searchRequest.appIds, String.class);
        QueryWrapper<App> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (StringUtils.isNotEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        if (searchRequest.appList != null) query.in("id", searchRequest.appList);
        IPage<App> page = appMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
