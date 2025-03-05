package xin.manong.darwin.service.impl;

import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.CronExpression;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.request.PlanSearchRequest;
import xin.manong.weapon.base.util.RandomID;

import java.util.ArrayList;
import java.util.Date;

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
    @Resource
    protected RuleService ruleService;

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
        plan.status = Constants.PLAN_STATUS_RUNNING;
        plan.crontabExpression = "0 0 6-23 * * ?";
        plan.seedURLs = new ArrayList<>();
        plan.seedURLs.add(record);
        Assert.assertTrue(plan.check());
        Assert.assertTrue(planService.add(plan));

        Rule rule = new Rule();
        rule.name = "测试规则";
        rule.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        rule.script = "function";
        rule.regex = "http://www.sina.com.cn/\\d+.html";
        rule.domain = "sina.com.cn";
        rule.planId = plan.planId;
        rule.appId = 1;
        rule.check();
        Assert.assertTrue(ruleService.add(rule));
        Assert.assertTrue(rule.id != null && rule.id > 0L);

        Plan planInDB = planService.get(plan.planId);
        Assert.assertNotNull(planInDB);
        Assert.assertEquals("测试计划", planInDB.name);
        Assert.assertEquals(0, planInDB.appId.intValue());
        Assert.assertEquals(plan.planId, planInDB.planId);
        Assert.assertEquals("测试应用", planInDB.appName);
        Assert.assertEquals("0 0 6-23 * * ?", planInDB.crontabExpression);
        Assert.assertEquals(Constants.PLAN_CATEGORY_PERIOD, planInDB.category.intValue());
        Assert.assertEquals(Constants.PLAN_STATUS_RUNNING, planInDB.status.intValue());
        Assert.assertEquals(1, planInDB.ruleIds.size());
        Assert.assertEquals(rule.id.intValue(), planInDB.ruleIds.get(0).intValue());
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
        Assert.assertNotNull(planInDB);
        Assert.assertEquals("测试计划", planInDB.name);
        Assert.assertEquals(0, planInDB.appId.intValue());
        Assert.assertEquals(plan.planId, planInDB.planId);
        Assert.assertEquals("测试应用", planInDB.appName);
        Assert.assertEquals("0 0 6-23 * * ?", planInDB.crontabExpression);
        Assert.assertEquals(Constants.PLAN_CATEGORY_PERIOD, planInDB.category.intValue());
        Assert.assertEquals(Constants.PLAN_STATUS_STOPPED, planInDB.status.intValue());
        Assert.assertEquals(1, planInDB.ruleIds.size());
        Assert.assertEquals(rule.id.intValue(), planInDB.ruleIds.get(0).intValue());
        Assert.assertEquals(2, planInDB.seedURLs.size());
        Assert.assertEquals("http://www.sohu.com/", planInDB.seedURLs.get(0).url);
        Assert.assertEquals("http://www.163.net/", planInDB.seedURLs.get(1).url);

        PlanSearchRequest searchRequest = new PlanSearchRequest();
        searchRequest.name = "测试计划";
        Pager<Plan> pager = planService.search(searchRequest);
        Assert.assertEquals(1L, pager.current.longValue());
        Assert.assertEquals(1L, pager.total.longValue());
        Assert.assertEquals(1, pager.records.size());

        Assert.assertTrue(planService.delete(plan.planId));
    }

    @Test
    @Transactional
    @Rollback
    public void testExecute() throws Exception {
        Plan plan = new Plan();
        plan.name = "测试计划";
        plan.planId = RandomID.build();
        plan.category = Constants.PLAN_CATEGORY_PERIOD;
        plan.status = Constants.PLAN_STATUS_RUNNING;
        plan.appId = 1;
        plan.avoidRepeatedFetch = true;
        plan.priority = Constants.PRIORITY_HIGH;
        plan.crontabExpression = "0 0 6-23/1 * * ?";
        plan.ruleIds = new ArrayList<>();
        plan.ruleIds.add(32);
        plan.seedURLs = new ArrayList<>();
        {
            URLRecord seedURL = new URLRecord("http://www.sina.com.cn");
            seedURL.category = Constants.CONTENT_CATEGORY_LIST;
            plan.seedURLs.add(seedURL);
        }
        {
            URLRecord seedURL = new URLRecord("http://www.sina.com.cn/123.html");
            seedURL.category = Constants.CONTENT_CATEGORY_LIST;
            plan.seedURLs.add(seedURL);
        }
        Assert.assertTrue(planService.add(plan));

        Assert.assertTrue(planService.execute(plan));

        Plan getPlan = planService.get(plan.planId);
        Assert.assertEquals(new CronExpression(plan.crontabExpression).getNextValidTimeAfter(new Date()).getTime(),
                getPlan.nextTime.longValue());
    }
}
