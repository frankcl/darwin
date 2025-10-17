package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.NotFoundException;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.ProxyMapper;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.darwin.service.request.ProxySearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

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

    private final Map<Integer, ProxyCache> proxyCacheMap;
    @Resource
    private ProxyMapper proxyMapper;

    public ProxyServiceImpl() {
        proxyCacheMap = new HashMap<>();
        for (Integer category : Constants.SUPPORT_PROXY_CATEGORIES.keySet()) {
            proxyCacheMap.put(category, new ProxyCache());
        }
    }

    @Override
    public boolean add(Proxy proxy) {
        LambdaQueryWrapper<Proxy> query = new LambdaQueryWrapper<>();
        query.eq(Proxy::getAddress, proxy.address).eq(Proxy::getPort, proxy.getPort());
        if (proxyMapper.selectCount(query) > 0) throw new IllegalStateException("代理已存在");
        int n = proxyMapper.insert(proxy);
        ProxyCache proxyCache = proxyCacheMap.getOrDefault(proxy.category, null);
        if (n > 0 && proxyCache != null) proxyCache.add(proxy);
        return n > 0;
    }

    @Override
    public boolean update(Proxy proxy) {
        Proxy prev = proxyMapper.selectById(proxy.id);
        if (prev == null) throw new NotFoundException("代理不存在");
        int n = proxyMapper.updateById(proxy);
        ProxyCache proxyCache = proxyCacheMap.getOrDefault(prev.category, null);
        if (n > 0 && proxyCache != null) proxyCache.update(proxy);
        return n > 0;
    }

    @Override
    public boolean delete(int id) {
        Proxy proxy = proxyMapper.selectById(id);
        if (proxy == null) throw new NotFoundException("代理不存在");
        int n = proxyMapper.deleteById(id);
        ProxyCache proxyCache = proxyCacheMap.getOrDefault(proxy.category, null);
        if (n > 0 && proxyCache != null) proxyCache.remove(id);
        return n > 0;
    }

    @Override
    public void refreshCache(int category) {
        ProxyCache proxyCache = proxyCacheMap.getOrDefault(category, null);
        if (proxyCache == null) return;
        LambdaQueryWrapper<Proxy> query = new LambdaQueryWrapper<>();
        query.eq(Proxy::getCategory, category);
        query.and(c -> c.isNull(Proxy::getExpiredTime).or().gt(Proxy::getExpiredTime, System.currentTimeMillis()));
        List<Proxy> proxies = proxyMapper.selectList(query);
        proxyCache.rebuild(proxies);
    }

    @Override
    public int deleteExpired() {
        LambdaQueryWrapper<Proxy> query = new LambdaQueryWrapper<>();
        query.isNotNull(Proxy::getExpiredTime).lt(Proxy::getExpiredTime, System.currentTimeMillis());
        return proxyMapper.delete(query);
    }

    @Override
    public Proxy randomGet(int category) {
        ProxyCache proxyCache = proxyCacheMap.get(category);
        if (proxyCache == null) return null;
        if (proxyCache.size() == 0) refreshCache(category);
        Proxy proxy = proxyCache.randomGet();
        while (proxy != null && proxy.isExpired()) {
            proxyCache.remove(proxy.id);
            proxy = proxyCache.randomGet();
        }
        return proxy;
    }

    @Override
    public Proxy get(int id) {
        Proxy proxy = proxyMapper.selectById(id);
        refreshCache(proxy);
        return proxy;
    }

    @Override
    public Proxy get(String address, int port) {
        LambdaQueryWrapper<Proxy> query = new LambdaQueryWrapper<>();
        query.eq(Proxy::getAddress, address).eq(Proxy::getPort, port);
        Proxy proxy = proxyMapper.selectOne(query, false);
        refreshCache(proxy);
        return proxy;
    }

    @Override
    public Pager<Proxy> search(ProxySearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new ProxySearchRequest();
        if (searchRequest.pageNum == null || searchRequest.pageNum < 1) searchRequest.pageNum = Constants.DEFAULT_PAGE_NUM;
        if (searchRequest.pageSize == null || searchRequest.pageSize <= 0) searchRequest.pageSize = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(Proxy.class, searchRequest);
        QueryWrapper<Proxy> query = new QueryWrapper<>();
        query.select("id", "address", "port", "category", "expired_time", "create_time", "update_time");
        searchRequest.prepareOrderBy(query);
        if (searchRequest.category != null) query.eq("category", searchRequest.category);
        if (searchRequest.expired != null) {
            long currentTime = System.currentTimeMillis();
            if (!searchRequest.expired) {
                query.and(wrapper -> wrapper.isNull("expired_time").or().
                        gt("expired_time", currentTime));
            } else {
                query.and(wrapper -> wrapper.lt("expired_time", currentTime).or().
                        eq("expired_time", currentTime));
            }
        }
        IPage<Proxy> page = proxyMapper.selectPage(new Page<>(searchRequest.pageNum, searchRequest.pageSize), query);
        return Converter.convert(page);
    }

    /**
     * 刷新cache
     *
     * @param proxy 代理
     */
    private void refreshCache(Proxy proxy) {
        if (proxy == null || proxy.isExpired()) return;
        ProxyCache proxyCache = proxyCacheMap.getOrDefault(proxy.category, null);
        if (proxyCache != null && !proxyCache.contains(proxy.id)) proxyCache.add(proxy);
    }
}
