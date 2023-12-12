package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.ProxyMapper;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.darwin.service.request.ProxySearchRequest;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代理服务实现
 *
 * @author frankcl
 * @date 2023-12-11 17:20:18
 */
@Service
public class ProxyServiceImpl implements ProxyService {

    private static final Logger logger = LoggerFactory.getLogger(ProxyServiceImpl.class);

    private Map<Integer, ProxyCache> proxyCacheMap;
    @Resource
    protected ProxyMapper proxyMapper;

    public ProxyServiceImpl() {
        proxyCacheMap = new HashMap<>();
        for (Integer category : Constants.SUPPORT_PROXY_CATEGORIES.keySet()) {
            proxyCacheMap.put(category, new ProxyCache());
        }
    }

    @Override
    public Boolean add(Proxy proxy) {
        LambdaQueryWrapper<Proxy> query = new LambdaQueryWrapper<>();
        query.eq(Proxy::getAddress, proxy.address).eq(Proxy::getPort, proxy.getPort());
        if (proxy.selectCount(query) > 0) {
            logger.error("proxy has existed for address[{}] and port[{}]", proxy.address, proxy.port);
            throw new RuntimeException(String.format("代理[%s:%d]已存在", proxy.address, proxy.port));
        }
        int n = proxyMapper.insert(proxy);
        ProxyCache proxyCache = proxyCacheMap.getOrDefault(proxy.category, null);
        if (n > 0 && proxyCache != null) proxyCache.add(proxy);
        return n > 0;
    }

    @Override
    public Boolean update(Proxy proxy) {
        Proxy prev = proxyMapper.selectById(proxy.id);
        if (prev == null) {
            logger.error("proxy[{}] is not found", proxy.id);
            return false;
        }
        int n = proxyMapper.updateById(proxy);
        ProxyCache proxyCache = proxyCacheMap.getOrDefault(prev.category, null);
        if (n > 0 && proxyCache != null) proxyCache.update(proxy);
        return n > 0;
    }

    @Override
    public Boolean delete(int id) {
        Proxy proxy = proxyMapper.selectById(id);
        if (proxy == null) {
            logger.error("proxy[{}] is not found", id);
            return false;
        }
        int n = proxyMapper.deleteById(id);
        ProxyCache proxyCache = proxyCacheMap.getOrDefault(proxy.category, null);
        if (n > 0 && proxyCache != null) proxyCache.remove(id);
        return n > 0;
    }

    @Override
    public Boolean refresh(int category) {
        ProxyCache proxyCache = proxyCacheMap.getOrDefault(category, null);
        if (proxyCache == null) return false;
        LambdaQueryWrapper<Proxy> query = new LambdaQueryWrapper<>();
        query.eq(Proxy::getCategory, category);
        List<Proxy> proxies = proxyMapper.selectList(query);
        proxyCache.rebuild(proxies);
        return true;
    }

    @Override
    public Proxy randomGet(int category) {
        ProxyCache proxyCache = proxyCacheMap.get(category);
        if (proxyCache == null) return null;
        Proxy proxy = proxyCache.randomGet();
        while (proxy != null && proxy.isExpired()) {
            proxyCache.remove(proxy.id);
            proxy = proxyCache.randomGet();
        }
        return proxy;
    }

    @Override
    public Proxy get(int id) {
        return proxyMapper.selectById(id);
    }

    @Override
    public Pager<Proxy> search(ProxySearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new ProxySearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        LambdaQueryWrapper<Proxy> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Proxy::getCreateTime);
        if (searchRequest.category != null) query.eq(Proxy::getCategory, searchRequest.category);
        if (searchRequest.expired != null) {
            long currentTime = System.currentTimeMillis();
            if (!searchRequest.expired) query.gt(Proxy::getExpiredTime, currentTime);
            else {
                query.lt(Proxy::getExpiredTime, currentTime).or().eq(Proxy::getExpiredTime, currentTime);
            }
        }
        IPage<Proxy> page = proxyMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
