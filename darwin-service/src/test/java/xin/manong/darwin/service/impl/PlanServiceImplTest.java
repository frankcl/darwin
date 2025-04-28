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
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.request.PlanSearchRequest;
import xin.manong.weapon.base.util.RandomID;

/**
 * @author frankcl
 * @date 2023-03-15 15:18:57
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class PlanServiceImplTest {

    @Resource
    protected PlanService planService;

    @Test
    @Transactional
    @Rollback
    public void testPlanOperations() {
        URLRecord record = new URLRecord("http://www.sina.com.cn/");
        Plan plan = new Plan();
        plan.name = "测试计划";
        plan.planId = RandomID.build();
        plan.appId = 0;
        plan.appName = "测试应用";
        plan.category = Constants.PLAN_CATEGORY_PERIOD;
        plan.status = false;
        plan.crontabExpression = "0 0 6-23 * * ?";
        Assert.assertTrue(plan.check());
        Assert.assertTrue(planService.add(plan));

        Plan planInDB = planService.get(plan.planId);
        Assert.assertNotNull(planInDB);
        Assert.assertEquals("测试计划", planInDB.name);
        Assert.assertEquals(0, planInDB.appId.intValue());
        Assert.assertEquals(plan.planId, planInDB.planId);
        Assert.assertEquals("测试应用", planInDB.appName);
        Assert.assertEquals("0 0 6-23 * * ?", planInDB.crontabExpression);
        Assert.assertEquals(Constants.PLAN_CATEGORY_PERIOD, planInDB.category.intValue());
        Assert.assertEquals(false, planInDB.status);

        Plan updatePlan = new Plan();
        updatePlan.planId = plan.planId;
        updatePlan.status = true;
        Assert.assertTrue(planService.update(updatePlan));

        planInDB = planService.get(plan.planId);
        Assert.assertNotNull(planInDB);
        Assert.assertEquals("测试计划", planInDB.name);
        Assert.assertEquals(0, planInDB.appId.intValue());
        Assert.assertEquals(plan.planId, planInDB.planId);
        Assert.assertEquals("测试应用", planInDB.appName);
        Assert.assertEquals("0 0 6-23 * * ?", planInDB.crontabExpression);
        Assert.assertEquals(Constants.PLAN_CATEGORY_PERIOD, planInDB.category.intValue());
        Assert.assertEquals(true, planInDB.status);

        PlanSearchRequest searchRequest = new PlanSearchRequest();
        searchRequest.name = "测试计划";
        Pager<Plan> pager = planService.search(searchRequest);
        Assert.assertEquals(1L, pager.pageNum.longValue());
        Assert.assertEquals(1L, pager.total.longValue());
        Assert.assertEquals(1, pager.records.size());

        Assert.assertTrue(planService.delete(plan.planId));
    }
}
