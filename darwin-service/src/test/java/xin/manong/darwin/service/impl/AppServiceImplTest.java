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
import xin.manong.darwin.common.model.App;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.AppService;
import xin.manong.darwin.service.request.AppSearchRequest;

/**
 * @author frankcl
 * @date 2023-04-04 14:04:49
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class AppServiceImplTest {

    @Resource
    protected AppService appService;

    @Test
    @Transactional
    @Rollback
    public void testAppOperations() {
        App app = new App();
        app.name = "测试应用1";
        app.creatorId = "xxx";
        app.creator = "frankcl";
        Assert.assertTrue(appService.add(app));
        Assert.assertTrue(app.id != null && app.id > 0L);

        App updateApp = new App();
        updateApp.id = app.id;
        updateApp.name = "测试应用plus";
        Assert.assertTrue(appService.update(updateApp));

        App getApp = appService.get(app.id);
        Assert.assertNotNull(getApp);
        Assert.assertEquals("测试应用plus", getApp.name);

        AppSearchRequest searchRequest = new AppSearchRequest();
        searchRequest.name = "测试应用plus";
        searchRequest.pageNum = 1;
        searchRequest.pageSize = 10;
        Pager<App> pager = appService.search(searchRequest);
        Assert.assertEquals(1, pager.total.intValue());
        Assert.assertEquals(1, pager.records.size());
        Assert.assertEquals(app.id, pager.records.get(0).id);

        Assert.assertTrue(appService.delete(app.id));
    }
}
