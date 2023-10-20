package xin.manong.darwin.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.model.AppUser;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.AppUserService;
import xin.manong.darwin.service.request.AppUserSearchRequest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-10-20 13:41:36
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class AppUserServiceImplSuite {

    @Resource
    protected AppUserService appUserService;

    @Test
    @Transactional
    @Rollback
    public void testAppUserOperations() {
        List<Long> ids = new ArrayList<>();
        {
            AppUser appUser = new AppUser();
            appUser.appId = 1L;
            appUser.userId = "user1";
            appUser.userRealName = "frankcl";
            Assert.assertTrue(appUserService.add(appUser));
            ids.add(appUser.id);
        }
        {
            AppUser appUser = new AppUser();
            appUser.appId = 2L;
            appUser.userId = "user2";
            appUser.userRealName = "jack";
            Assert.assertTrue(appUserService.add(appUser));
            ids.add(appUser.id);
        }
        Assert.assertTrue(appUserService.hasAppPermission("user1", 1L));
        Assert.assertTrue(appUserService.hasAppPermission("user2", 2L));
        Assert.assertFalse(appUserService.hasAppPermission("user2", 1L));
        {
            AppUserSearchRequest searchRequest = new AppUserSearchRequest();
            searchRequest.appId = 1L;
            Pager<AppUser> pager = appUserService.search(searchRequest);
            Assert.assertEquals(1, pager.total.intValue());
            Assert.assertEquals("user1", pager.records.get(0).userId);
        }
        for (Long id : ids) Assert.assertTrue(appUserService.delete(id));
    }
}
