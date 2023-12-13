package xin.manong.darwin.service.impl;

import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.model.Proxy;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 代理缓存
 *
 * @author frankcl
 * @date 2023-12-11 17:27:10
 */
public class ProxyCache {

    private Random random;
    private ReentrantReadWriteLock readWriteLock;
    private List<Proxy> proxies;
    private Map<Integer, Proxy> proxyMap;

    public ProxyCache() {
        random = new Random();
        readWriteLock = new ReentrantReadWriteLock();
        proxies = new ArrayList<>();
        proxyMap = new HashMap<>();
    }

    /**
     * 缓存代理数量
     *
     * @return 代理数量
     */
    public int size() {
        return proxies == null ? 0 : proxies.size();
    }

    /**
     * 是否包含代理
     *
     * @param id 代理ID
     * @return 包含返回true，否则返回false
     */
    public boolean contains(int id) {
        return proxyMap.containsKey(id);
    }

    /**
     * 随机获取代理
     *
     * @return 代理
     */
    public Proxy randomGet() {
        if (proxies.isEmpty()) return null;
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            if (proxies.isEmpty()) return null;
            int next = random.nextInt(proxies.size());
            return proxies.get(next);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 移除代理
     *
     * @param id 代理ID
     */
    public void remove(int id) {
        if (!proxyMap.containsKey(id)) return;
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            if (!proxyMap.containsKey(id)) return;
            Proxy proxy = proxyMap.remove(id);
            int index = proxies.indexOf(proxy);
            if (index != -1) proxies.remove(index);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 更新代理
     *
     * @param proxy 代理
     */
    public void update(Proxy proxy) {
        Proxy cachedProxy = proxyMap.get(proxy.id);
        if (cachedProxy == null) return;
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            if (!StringUtils.isEmpty(proxy.address)) cachedProxy.address = proxy.address;
            if (!StringUtils.isEmpty(proxy.username)) cachedProxy.username = proxy.username;
            if (!StringUtils.isEmpty(proxy.password)) cachedProxy.password = proxy.password;
            if (proxy.expiredTime != null) cachedProxy.expiredTime = proxy.expiredTime;
            if (proxy.category != null) cachedProxy.category = proxy.category;
            if (proxy.port != null) cachedProxy.port = proxy.port;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 添加代理
     *
     * @param proxy 代理
     */
    public void add(Proxy proxy) {
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            proxyMap.put(proxy.id, proxy);
            Iterator<Proxy> iterator = proxies.iterator();
            while (iterator.hasNext()) {
                Proxy cachedProxy = iterator.next();
                if (cachedProxy.id != proxy.id) continue;
                iterator.remove();
                break;
            }
            proxies.add(proxy);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 重建代理缓存
     *
     * @param proxies 代理列表
     */
    public void rebuild(List<Proxy> proxies) {
        List<Proxy> newProxies = new ArrayList<>(proxies);
        Map<Integer, Proxy> newProxyMap = new HashMap<>();
        for (Proxy proxy : proxies) newProxyMap.put(proxy.id, proxy);
        this.proxies = newProxies;
        this.proxyMap = newProxyMap;
    }
}
