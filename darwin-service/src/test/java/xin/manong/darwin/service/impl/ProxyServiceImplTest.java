package xin.manong.darwin.service.impl;

import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.ProxyService;
import xin.manong.darwin.service.request.ProxySearchRequest;

/**
 * @author frankcl
 * @date 2023-12-12 14:41:45
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class ProxyServiceImplTest {

    @Resource
    protected ProxyService proxyService;

    @Test
    @Transactional
    @Rollback
    public void testProxyOperations() {
        Proxy proxy = new Proxy();
        proxy.address = "0.0.0.0";
        proxy.port = 8888;
        proxy.category = Constants.PROXY_CATEGORY_LONG;
        proxy.username = "frankcl";
        proxy.password = "xxx";
        Assert.assertTrue(proxyService.add(proxy));

        Proxy updateProxy = new Proxy();
        updateProxy.id = proxy.id;
        updateProxy.username = "abc";
        Assert.assertTrue(proxyService.update(updateProxy));

        proxy = proxyService.get(proxy.id);
        Assert.assertNotNull(proxy);
        Assert.assertEquals("0.0.0.0", proxy.address);
        Assert.assertEquals(8888, proxy.port.intValue());
        Assert.assertEquals(Constants.PROXY_CATEGORY_LONG, proxy.category.intValue());
        Assert.assertEquals("abc", proxy.username);
        Assert.assertEquals("xxx", proxy.password);

        ProxySearchRequest searchRequest = new ProxySearchRequest();
        searchRequest.category = Constants.PROXY_CATEGORY_LONG;
        searchRequest.expired = false;
        Pager<Proxy> pager = proxyService.search(searchRequest);
        Assert.assertTrue(pager != null && !pager.records.isEmpty());

        Assert.assertTrue(proxyService.delete(proxy.id));
    }
}
