package xin.manong.darwin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.CookieService;
import xin.manong.weapon.base.etcd.EtcdClient;
import xin.manong.weapon.spring.boot.etcd.WatchValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Cookie服务实现
 *
 * @author frankcl
 * @date 2026-01-29 09:40:34
 */
@Service
public class CookieServiceImpl implements CookieService {

    private static final String NAMESPACE = "darwin";
    private static final String KEY = "cookie/cookieMap";
    private static final String COOKIE_MAP = String.format("%s/%s", NAMESPACE, KEY);

    @WatchValue(namespace = NAMESPACE, key = KEY)
    private Map<String, String> cookieMap;
    @Resource
    private EtcdClient etcdClient;

    @Override
    public String getCookie(URLRecord record) {
        String cookie = getCookie(record.host);
        if (StringUtils.isEmpty(cookie)) cookie = getCookie(record.domain);
        return cookie;
    }

    @Override
    public String getCookie(String key) {
        return cookieMap == null ? null : cookieMap.get(key);
    }

    @Override
    public void setCookie(String key, String cookie) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(cookie)) return;
        Map<String, String> cookieMap = this.cookieMap == null ? new HashMap<>() : new HashMap<>(this.cookieMap);
        cookieMap.put(key, cookie);
        cookieMap(cookieMap);
    }

    @Override
    public Map<String, String> cookieMap() {
        return cookieMap == null ? Map.of() : cookieMap;
    }

    @Override
    public void cookieMap(@NotNull Map<String, String> cookieMap) {
        if (!etcdClient.put(COOKIE_MAP, JSON.toJSONString(cookieMap, SerializerFeature.PrettyFormat))) {
            throw new IllegalStateException("更新Cookie配置失败");
        }
    }
}
