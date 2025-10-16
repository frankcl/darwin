package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.model.AppSecret;
import xin.manong.darwin.service.dao.mapper.AppSecretMapper;
import xin.manong.darwin.service.iface.AppSecretService;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.base.util.ShortKeyBuilder;

import java.util.List;

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
        if (exists(appSecret.appId, appSecret.accessKey, appSecret.secretKey)) {
            throw new IllegalStateException("应用秘钥已存在");
        }
        return appSecretMapper.insert(appSecret) > 0;
    }

    @Override
    public boolean update(AppSecret appSecret) {
        AppSecret prev = get(appSecret.id);
        if (prev == null) throw new NotFoundException("应用秘钥不存在");
        if (appSecret.appId != null || StringUtils.isNotEmpty(appSecret.accessKey) ||
                StringUtils.isNotEmpty(appSecret.secretKey)) {
            if (exists(appSecret.appId != null ? appSecret.appId : prev.appId,
                    StringUtils.isNotEmpty(appSecret.accessKey) ? appSecret.accessKey : prev.accessKey,
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
    public List<AppSecret> getAppSecrets(Integer appId) {
        LambdaQueryWrapper<AppSecret> query = new LambdaQueryWrapper<>();
        query.eq(AppSecret::getAppId, appId);
        return appSecretMapper.selectList(query);
    }

    /**
     * 是否存在应用秘钥
     *
     * @param appId 应用ID
     * @param accessKey Access key
     * @param secretKey Secret key
     * @return 存在返回true，否则返回false
     */
    private boolean exists(Integer appId, String accessKey, String secretKey) {
        LambdaQueryWrapper<AppSecret> query = new LambdaQueryWrapper<>();
        query.eq(AppSecret::getAppId, appId).eq(AppSecret::getAccessKey, accessKey).
                eq(AppSecret::getSecretKey, secretKey);
        return appSecretMapper.exists(query);
    }
}
