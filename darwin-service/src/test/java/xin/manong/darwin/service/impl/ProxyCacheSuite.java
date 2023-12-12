package xin.manong.darwin.service.impl;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.model.Proxy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-12-12 11:52:22
 */
public class ProxyCacheSuite {

    @Test
    public void testCacheOperations() {
        ProxyCache proxyCache = new ProxyCache();
        {
            Proxy proxy = new Proxy();
            proxy.id = 1;
            proxy.address = "1.1.1.1";
            proxy.port = 1234;
            proxyCache.add(proxy);
        }
        {
            Proxy proxy = new Proxy();
            proxy.id = 2;
            proxy.address = "1.1.1.2";
            proxy.port = 1234;
            proxyCache.add(proxy);
        }
        Assert.assertEquals(2, proxyCache.size());
        {
            Proxy proxy = proxyCache.randomGet();
            Assert.assertTrue(proxy != null);
        }
        {
            proxyCache.remove(1);
            Proxy proxy = proxyCache.randomGet();
            Assert.assertTrue(proxy != null);
            Assert.assertEquals(2, proxy.id.intValue());
            Assert.assertEquals(1, proxyCache.size());
        }
        {
            Proxy proxy = new Proxy();
            proxy.id = 2;
            proxy.address = "1.1.1.3";
            proxy.username = "abc";
            proxyCache.update(proxy);
            proxy = proxyCache.randomGet();
            Assert.assertTrue(proxy != null);
            Assert.assertEquals(2, proxy.id.intValue());
            Assert.assertEquals("1.1.1.3", proxy.address);
            Assert.assertEquals(1234, proxy.port.intValue());
            Assert.assertEquals("abc", proxy.username);
        }
        {
            List<Proxy> proxies = new ArrayList<>();
            Proxy proxy = new Proxy();
            proxy.id = 3;
            proxy.address = "1.1.1.10";
            proxy.port = 1234;
            proxies.add(proxy);
            proxyCache.rebuild(proxies);
            Assert.assertEquals(1, proxyCache.size());
            proxy = proxyCache.randomGet();
            Assert.assertTrue(proxy != null);
            Assert.assertEquals(3, proxy.id.intValue());
        }
    }
}
