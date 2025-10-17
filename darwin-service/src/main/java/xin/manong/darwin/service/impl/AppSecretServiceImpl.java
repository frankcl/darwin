package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.App;
import xin.manong.darwin.common.model.AppSecret;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.AppSecretMapper;
import xin.manong.darwin.service.iface.AppSecretService;
import xin.manong.darwin.service.request.AppSecretSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.base.util.ShortKeyBuilder;

/**
 * 应用秘钥服务实现
 *
 * @author frankcl
 * @date 2025-10-16 15:00:43
 */
@Service
public class AppSecretServiceImpl implements AppSecretService {

    @Resource
    private AppSecretMapper appSecretMapper;

    @Override
    public boolean add(AppSecret appSecret) {
        if (exists(appSecret.accessKey, appSecret.secretKey)) {
            throw new IllegalStateException("应用秘钥已存在");
        }
        return appSecretMapper.insert(appSecret) > 0;
    }

    @Override
    public boolean update(AppSecret appSecret) {
        AppSecret prev = get(appSecret.id);
        if (prev == null) throw new NotFoundException("应用秘钥不存在");
        if ((StringUtils.isNotEmpty(appSecret.accessKey) && !appSecret.accessKey.equals(prev.accessKey)) ||
                (StringUtils.isNotEmpty(appSecret.secretKey) && !appSecret.secretKey.equals(prev.secretKey))) {
            if (exists(StringUtils.isNotEmpty(appSecret.accessKey) ? appSecret.accessKey : prev.accessKey,
                    StringUtils.isNotEmpty(appSecret.secretKey) ? appSecret.secretKey : prev.secretKey)) {
                throw new IllegalStateException("应用秘钥已存在");
            }
        }
        return appSecretMapper.updateById(appSecret) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        if (get(id) == null) throw new NotFoundException("应用秘钥不存在");
        return appSecretMapper.deleteById(id) > 0;
    }

    @Override
    public String randomAccessKey() {
        return ShortKeyBuilder.build(RandomID.build());
    }

    @Override
    public String randomSecretKey() {
        return RandomID.build();
    }

    @Override
    public AppSecret get(Integer id) {
        return appSecretMapper.selectById(id);
    }

    @Override
    public AppSecret get(String accessKey, String secretKey) {
        LambdaQueryWrapper<AppSecret> query = new LambdaQueryWrapper<>();
        query.eq(AppSecret::getAccessKey, accessKey).eq(AppSecret::getSecretKey, secretKey);
        return appSecretMapper.selectOne(query);
    }

    @Override
    public Pager<AppSecret> search(AppSecretSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new AppSecretSearchRequest();
        if (searchRequest.pageNum == null || searchRequest.pageNum < 1) searchRequest.pageNum = Constants.DEFAULT_PAGE_NUM;
        if (searchRequest.pageSize == null || searchRequest.pageSize <= 0) searchRequest.pageSize = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(App.class, searchRequest);
        QueryWrapper<AppSecret> query = new QueryWrapper<>();
        query.select("id", "app_id", "name", "access_key", "create_time", "update_time");
        searchRequest.prepareOrderBy(query);
        if (searchRequest.appId != null) query.eq("app_id", searchRequest.appId);
        IPage<AppSecret> page = appSecretMapper.selectPage(new Page<>(searchRequest.pageNum, searchRequest.pageSize), query);
        return Converter.convert(page);
    }

    @Override
    public boolean exists(String accessKey, String secretKey) {
        LambdaQueryWrapper<AppSecret> query = new LambdaQueryWrapper<>();
        query.eq(AppSecret::getAccessKey, accessKey).
                eq(AppSecret::getSecretKey, secretKey);
        return appSecretMapper.exists(query);
    }
}
