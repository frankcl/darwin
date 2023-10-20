package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.AppUser;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.AppUserMapper;
import xin.manong.darwin.service.iface.AppUserService;
import xin.manong.darwin.service.request.AppUserSearchRequest;

import javax.annotation.Resource;

/**
 * 应用用户关系服务实现
 *
 * @author frankcl
 * @date 2023-10-20 11:36:36
 */
@Service
public class AppUserServiceImpl implements AppUserService {

    private static final Logger logger = LoggerFactory.getLogger(AppUserServiceImpl.class);

    @Resource
    protected AppUserMapper appUserMapper;

    @Override
    public Boolean add(AppUser appUser) {
        LambdaQueryWrapper<AppUser> query = new LambdaQueryWrapper<>();
        query.eq(AppUser::getAppId, appUser.appId).eq(AppUser::getUserId, appUser.userId);
        if (appUserMapper.selectCount(query) > 0) {
            logger.error("app user relation has existed for app id[{}] and user id[{}]",
                    appUser.appId, appUser.userId);
            throw new RuntimeException("应用用户关系已存在");
        }
        return appUserMapper.insert(appUser) > 0;
    }

    @Override
    public Boolean delete(Integer id) {
        if (appUserMapper.selectById(id) == null) {
            logger.error("app user relation[{}] is not found", id);
            return false;
        }
        return appUserMapper.deleteById(id) > 0;
    }

    @Override
    public AppUser get(Integer id) {
        if (id == null) {
            logger.error("app user id is null");
            throw new RuntimeException("应用用户关系ID为空");
        }
        return appUserMapper.selectById(id);
    }

    @Override
    public Boolean hasAppPermission(String userId, Integer appId) {
        AppUserSearchRequest searchRequest = new AppUserSearchRequest();
        searchRequest.current = 1;
        searchRequest.size = 1;
        searchRequest.userId = userId;
        searchRequest.appId = appId;
        Pager<AppUser> pager = search(searchRequest);
        return pager != null && pager.records != null && pager.records.size() == 1;
    }

    @Override
    public Pager<AppUser> search(AppUserSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new AppUserSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        LambdaQueryWrapper<AppUser> query = new LambdaQueryWrapper<>();
        query.orderByDesc(AppUser::getCreateTime);
        if (searchRequest.appId != null) query.eq(AppUser::getAppId, searchRequest.appId);
        if (!StringUtils.isEmpty(searchRequest.userId)) query.eq(AppUser::getUserId, searchRequest.userId);
        if (!StringUtils.isEmpty(searchRequest.realName)) query.like(AppUser::getUserRealName, searchRequest.realName);
        IPage<AppUser> page = appUserMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
