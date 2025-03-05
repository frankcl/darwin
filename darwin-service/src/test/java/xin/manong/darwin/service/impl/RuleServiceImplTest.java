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
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.request.RuleSearchRequest;
import xin.manong.weapon.base.util.RandomID;

import java.util.ArrayList;

/**
 * @author frankcl
 * @date 2023-04-04 14:14:39
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class RuleServiceImplTest {

    @Resource
    protected RuleService ruleService;
    @Resource
    protected PlanService planService;

    @Test
    @Transactional
    @Rollback
    public void testRuleOperations() {
        Plan plan = new Plan();
        plan.name = "测试计划";
        plan.planId = RandomID.build();
        plan.appId = 0;
        plan.appName = "测试应用";
        plan.category = Constants.PLAN_CATEGORY_PERIOD;
        plan.status = Constants.PLAN_STATUS_RUNNING;
        plan.crontabExpression = "0 0 6-23 * * ?";
        plan.seedURLs = new ArrayList<>();
        plan.seedURLs.add(new URLRecord("http://www.sina.com.cn/"));
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

        {
            Pager<RuleHistory> pager = ruleService.listHistory(rule.id, 1, 10);
            Assert.assertTrue(pager != null && pager.records.size() == 1);
            RuleHistory ruleHistory = pager.records.get(0);
            Assert.assertEquals(Constants.SCRIPT_TYPE_GROOVY, ruleHistory.scriptType.intValue());
            Assert.assertEquals("function", ruleHistory.script);
            Assert.assertEquals("http://www.sina.com.cn/\\d+.html", ruleHistory.regex);
            Assert.assertEquals("sina.com.cn", ruleHistory.domain);
        }

        Rule updateRule = new Rule();
        updateRule.id = rule.id;
        updateRule.name = "test rule";
        updateRule.script = "function() {}";
        Assert.assertTrue(ruleService.update(updateRule));

        Rule getRule = ruleService.get(rule.id);
        Assert.assertNotNull(getRule);
        Assert.assertEquals("test rule", getRule.name);
        Assert.assertEquals(Constants.SCRIPT_TYPE_GROOVY, getRule.scriptType.intValue());
        Assert.assertEquals("function() {}", getRule.script);
        Assert.assertEquals("http://www.sina.com.cn/\\d+.html", getRule.regex);
        Assert.assertEquals("sina.com.cn", getRule.domain);

        {
            Pager<RuleHistory> pager = ruleService.listHistory(rule.id, 1, 10);
            Assert.assertTrue(pager != null && pager.records.size() == 2);
            RuleHistory ruleHistory = pager.records.get(0);
            Assert.assertEquals(Constants.SCRIPT_TYPE_GROOVY, ruleHistory.scriptType.intValue());
            Assert.assertEquals("function() {}", ruleHistory.script);
            Assert.assertEquals("http://www.sina.com.cn/\\d+.html", ruleHistory.regex);
            Assert.assertEquals("sina.com.cn", ruleHistory.domain);
        }

        RuleSearchRequest request = new RuleSearchRequest();
        request.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        request.domain = "sina.com.cn";
        request.current = 1;
        request.size = 10;
        Pager<Rule> pager = ruleService.search(request);
        Assert.assertEquals(1, pager.total.intValue());
        Assert.assertEquals(1, pager.records.size());
        Assert.assertEquals(rule.id.intValue(), pager.records.get(0).id.intValue());

        URLRecord record = new URLRecord("http://www.sina.com.cn/123.html");
        Assert.assertTrue(ruleService.match(record, rule));

        Assert.assertTrue(ruleService.delete(rule.id));
        Assert.assertEquals(0, (long) ruleService.listHistory(rule.id, 1, 10).total);
    }
}
