package xin.manong.darwin.runner.proxy;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.runner.ApplicationTest;

import java.util.List;

/**
 * @author frankcl
 * @date 2026-02-06 15:13:18
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = {
        "runner", "runner-dev",
        "service", "service-dev",
        "queue", "queue-dev",
        "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class JiliuProxyGetterTest {

    @Autowired(required = false)
    private ProxyGetter getter;

    @Test
    public void getBatchGetProxy() {
        if (getter == null) return;
        List<Proxy> proxies = getter.batchGet();
        Assert.assertEquals(5, proxies.size());
    }
}
