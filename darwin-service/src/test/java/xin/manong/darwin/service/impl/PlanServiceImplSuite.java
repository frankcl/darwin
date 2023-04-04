package xin.manong.darwin.service.impl;

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
import xin.manong.weapon.base.util.RandomID;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * @author frankcl
 * @date 2023-03-15 15:18:57
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class PlanServiceImplSuite {

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
        plan.category = Constants.PLAN_CATEGORY_REPEAT;
        plan.status = Constants.PLAN_STATUS_RUNNING;
        plan.crontabExpression = "0 0 6-23 * * ?";
        plan.ruleIds = new ArrayList<>();
        plan.ruleIds.add(0);
        plan.seedURLs = new ArrayList<>();
        plan.seedURLs.add(record);
        Assert.assertTrue(plan.check());
        Assert.assertTrue(planService.add(plan));

        Plan planInDB = planService.get(plan.planId);
        Assert.assertTrue(planInDB != null);
        Assert.assertEquals("测试计划", planInDB.name);
        Assert.assertEquals(0, planInDB.appId.intValue());
        Assert.assertEquals(plan.planId, planInDB.planId);
        Assert.assertEquals("测试应用", planInDB.appName);
        Assert.assertEquals("0 0 6-23 * * ?", planInDB.crontabExpression);
        Assert.assertEquals(Constants.PLAN_CATEGORY_REPEAT, planInDB.category.intValue());
        Assert.assertEquals(Constants.PLAN_STATUS_RUNNING, planInDB.status.intValue());
        Assert.assertEquals(1, planInDB.ruleIds.size());
        Assert.assertEquals(0, planInDB.ruleIds.get(0).intValue());
        Assert.assertEquals(1, planInDB.seedURLs.size());
        Assert.assertEquals(record.key, planInDB.seedURLs.get(0).key);
        Assert.assertEquals(record.url, planInDB.seedURLs.get(0).url);
        Assert.assertEquals(record.createTime.longValue(), planInDB.seedURLs.get(0).createTime.longValue());

        Plan updatePlan = new Plan();
        updatePlan.planId = plan.planId;
        updatePlan.status = Constants.PLAN_STATUS_STOPPED;
        updatePlan.seedURLs = new ArrayList<>();
        updatePlan.seedURLs.add(new URLRecord("http://www.sohu.com/"));
        updatePlan.seedURLs.add(new URLRecord("http://www.163.net/"));
        Assert.assertTrue(planService.update(updatePlan));

        planInDB = planService.get(plan.planId);
        Assert.assertTrue(planInDB != null);
        Assert.assertEquals("测试计划", planInDB.name);
        Assert.assertEquals(0, planInDB.appId.intValue());
        Assert.assertEquals(plan.planId, planInDB.planId);
        Assert.assertEquals("测试应用", planInDB.appName);
        Assert.assertEquals("0 0 6-23 * * ?", planInDB.crontabExpression);
        Assert.assertEquals(Constants.PLAN_CATEGORY_REPEAT, planInDB.category.intValue());
        Assert.assertEquals(Constants.PLAN_STATUS_STOPPED, planInDB.status.intValue());
        Assert.assertEquals(1, planInDB.ruleIds.size());
        Assert.assertEquals(0, planInDB.ruleIds.get(0).intValue());
        Assert.assertEquals(2, planInDB.seedURLs.size());
        Assert.assertEquals("http://www.sohu.com/", planInDB.seedURLs.get(0).url);
        Assert.assertEquals("http://www.163.net/", planInDB.seedURLs.get(1).url);

        Pager<Plan> pager = planService.search(null, 1, 10);
        Assert.assertEquals(1L, pager.current.longValue());
        Assert.assertEquals(1L, pager.total.longValue());
        Assert.assertEquals(1, pager.records.size());

        Assert.assertTrue(planService.delete(plan.planId));
    }
}
