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
import xin.manong.darwin.common.model.AppUser;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.AppUserService;
import xin.manong.darwin.service.request.AppUserSearchRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-10-20 13:41:36
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class AppUserServiceImplTest {

    @Resource
    protected AppUserService appUserService;

    @Test
    @Transactional
    @Rollback
    public void testAppUserOperations() {
        List<Integer> ids = new ArrayList<>();
        {
            AppUser appUser = new AppUser();
            appUser.appId = 1;
            appUser.userId = "user1";
            appUser.nickName = "frankcl";
            Assert.assertTrue(appUserService.add(appUser));
            ids.add(appUser.id);
        }
        {
            AppUser appUser = new AppUser();
            appUser.appId = 2;
            appUser.userId = "user2";
            appUser.nickName = "jack";
            Assert.assertTrue(appUserService.add(appUser));
            ids.add(appUser.id);
        }
        Assert.assertTrue(appUserService.hasAppPermission("user1", 1));
        Assert.assertTrue(appUserService.hasAppPermission("user2", 2));
        Assert.assertFalse(appUserService.hasAppPermission("user2", 1));
        {
            AppUserSearchRequest searchRequest = new AppUserSearchRequest();
            searchRequest.appId = 1;
            Pager<AppUser> pager = appUserService.search(searchRequest);
            Assert.assertEquals(1, pager.total.intValue());
            Assert.assertEquals("user1", pager.records.get(0).userId);
        }
        for (Integer id : ids) Assert.assertTrue(appUserService.delete(id));
    }
}
