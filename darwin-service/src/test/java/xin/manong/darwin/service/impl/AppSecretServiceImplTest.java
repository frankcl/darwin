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
import xin.manong.darwin.common.model.AppSecret;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.AppSecretService;
import xin.manong.darwin.service.request.AppSecretSearchRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author frankcl
 * @date 2025-10-16 16:03:26
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class AppSecretServiceImplTest {

    @Resource
    private AppSecretService service;

    @Test
    @Transactional
    @Rollback
    public void testAppSecret() {
        List<Integer> newAppSecretIds = new ArrayList<>();
        {
            AppSecret appSecret = new AppSecret();
            appSecret.appId = 1;
            appSecret.name = "test";
            appSecret.accessKey = "ak";
            appSecret.secretKey = "sk";
            Assert.assertTrue(service.add(appSecret));
            newAppSecretIds.add(appSecret.id);
        }
        {
            AppSecret appSecret = new AppSecret();
            appSecret.appId = 1;
            appSecret.name = "test1";
            appSecret.accessKey = "ak";
            appSecret.secretKey = "sk";
            try {
                service.add(appSecret);
                Assert.fail("unexpected");
            } catch (Exception e) {
                appSecret.accessKey = "ak1";
                appSecret.secretKey = "sk1";
                Assert.assertTrue(service.add(appSecret));
                newAppSecretIds.add(appSecret.id);
            }
        }

        AppSecretSearchRequest searchRequest = new AppSecretSearchRequest();
        searchRequest.appId = 1;
        Pager<AppSecret> pager = service.search(searchRequest);
        Assert.assertNotNull(pager);
        Assert.assertNotNull(pager.records);
        Assert.assertEquals(2, pager.records.size());
        for (Integer id : newAppSecretIds) Assert.assertTrue(service.delete(id));
    }
}
