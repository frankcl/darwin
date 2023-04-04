package xin.manong.darwin.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.model.App;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.AppService;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-04-04 14:04:49
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class AppServiceImplSuite {

    @Resource
    protected AppService appService;

    @Test
    @Transactional
    @Rollback
    public void testAppOperations() {
        App app = new App();
        app.name = "测试应用";
        Assert.assertTrue(appService.add(app));
        Assert.assertTrue(app.id != null && app.id > 0L);

        App updateApp = new App();
        updateApp.id = app.id;
        updateApp.name = "测试应用plus";
        Assert.assertTrue(appService.update(updateApp));

        App getApp = appService.get(app.id);
        Assert.assertTrue(getApp != null);
        Assert.assertEquals("测试应用plus", getApp.name);

        Pager<App> pager = appService.search("测试", 1, 10);
        Assert.assertEquals(1, pager.total.intValue());
        Assert.assertEquals(1, pager.records.size());
        Assert.assertEquals(app.id.longValue(), pager.records.get(0).id.longValue());

        Assert.assertTrue(appService.delete(app.id));
    }
}
