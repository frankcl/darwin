package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.AppUser;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.AppUserMapper;
import xin.manong.darwin.service.iface.AppUserService;
import xin.manong.darwin.service.request.AppUserSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

import java.util.List;

/**
 * 应用用户关系服务实现
 *
 * @author frankcl
 * @date 2023-10-20 11:36:36
 */
@Service
public class AppUserServiceImpl implements AppUserService {

    @Resource
    private AppUserMapper appUserMapper;

    @Override
    public boolean add(AppUser appUser) {
        LambdaQueryWrapper<AppUser> query = new LambdaQueryWrapper<>();
        query.eq(AppUser::getAppId, appUser.appId).eq(AppUser::getUserId, appUser.userId);
        if (appUserMapper.selectCount(query) > 0) throw new IllegalStateException("应用用户关系已存在");
        return appUserMapper.insert(appUser) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        if (appUserMapper.selectById(id) == null) throw new NotFoundException("应用用户关系不存在");
        return appUserMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByApp(Integer appId) {
        LambdaQueryWrapper<AppUser> query = new LambdaQueryWrapper<>();
        query.eq(AppUser::getAppId, appId);
        long n = appUserMapper.selectCount(query);
        if (n == 0) return true;
        return appUserMapper.delete(query) > 0;
    }

    @Override
    public AppUser get(Integer id) {
        if (id == null) throw new BadRequestException("应用用户关系ID为空");
        return appUserMapper.selectById(id);
    }

    @Override
    public boolean hasAppPermission(String userId, Integer appId) {
        AppUserSearchRequest searchRequest = new AppUserSearchRequest();
        searchRequest.userId = userId;
        searchRequest.appId = appId;
        Pager<AppUser> pager = search(searchRequest);
        return pager != null && pager.records != null && pager.records.size() == 1;
    }

    @Override
    public List<AppUser> getAppUsers(Integer appId) {
        LambdaQueryWrapper<AppUser> query = new LambdaQueryWrapper<>();
        query.eq(AppUser::getAppId, appId);
        return appUserMapper.selectList(query);
    }

    @Override
    public List<AppUser> getAppUsers(String userId) {
        LambdaQueryWrapper<AppUser> query = new LambdaQueryWrapper<>();
        query.eq(AppUser::getUserId, userId);
        return appUserMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<AppUser> addAppUsers, List<Integer> removeAppUsers) {
        for (AppUser appUser : addAppUsers) {
            if (!add(appUser)) throw new InternalServerErrorException("添加应用用户关系失败");
        }
        for (Integer id : removeAppUsers) {
            if (!delete(id)) throw new InternalServerErrorException("删除应用用户关系失败");
        }
    }

    @Override
    public Pager<AppUser> search(AppUserSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new AppUserSearchRequest();
        if (searchRequest.pageNum == null || searchRequest.pageNum < 1) searchRequest.pageNum = Constants.DEFAULT_PAGE_NUM;
        if (searchRequest.pageSize == null || searchRequest.pageSize <= 0) searchRequest.pageSize = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(AppUser.class, searchRequest);
        QueryWrapper<AppUser> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (searchRequest.appId != null) query.eq("app_id", searchRequest.appId);
        if (StringUtils.isNotEmpty(searchRequest.userId)) query.eq("user_id", searchRequest.userId);
        if (StringUtils.isNotEmpty(searchRequest.nickName)) query.like("nick_name", searchRequest.nickName);
        IPage<AppUser> page = appUserMapper.selectPage(new Page<>(searchRequest.pageNum, searchRequest.pageSize), query);
        return Converter.convert(page);
    }
}
