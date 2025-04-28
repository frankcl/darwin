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
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.request.RuleSearchRequest;

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

    @Test
    @Transactional
    @Rollback
    public void testRuleOperations() {
        Rule rule = new Rule();
        rule.name = "测试规则";
        rule.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        rule.script = "function";
        rule.regex = "http://www.sina.com.cn/\\d+.html";
        rule.planId = "xxx";
        rule.changeLog = "test";
        rule.check();
        Assert.assertTrue(rule.match("http://www.sina.com.cn/123.html"));

        Assert.assertTrue(ruleService.add(rule));
        Assert.assertTrue(rule.id != null && rule.id > 0L);

        {
            Pager<RuleHistory> pager = ruleService.getHistoryList(rule.id, 1, 10);
            Assert.assertTrue(pager != null && pager.records.isEmpty());
        }

        Rule updateRule = new Rule();
        updateRule.id = rule.id;
        updateRule.name = "test rule";
        updateRule.script = "function() {}";
        updateRule.changeLog = "test";
        Assert.assertTrue(ruleService.update(updateRule));

        Rule getRule = ruleService.get(rule.id);
        Assert.assertNotNull(getRule);
        Assert.assertEquals("test rule", getRule.name);
        Assert.assertEquals(Constants.SCRIPT_TYPE_GROOVY, getRule.scriptType.intValue());
        Assert.assertEquals("function() {}", getRule.script);
        Assert.assertEquals("http://www.sina.com.cn/\\d+.html", getRule.regex);

        {
            Pager<RuleHistory> pager = ruleService.getHistoryList(rule.id, 1, 10);
            Assert.assertTrue(pager != null && pager.records.size() == 1);
            RuleHistory ruleHistory = pager.records.get(0);
            Assert.assertEquals(Constants.SCRIPT_TYPE_GROOVY, ruleHistory.scriptType.intValue());
            Assert.assertEquals("function", ruleHistory.script);
            Assert.assertEquals("http://www.sina.com.cn/\\d+.html", ruleHistory.regex);
        }

        RuleSearchRequest request = new RuleSearchRequest();
        request.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        request.name = "test rule";
        request.pageNum = 1;
        request.pageSize = 10;
        Pager<Rule> pager = ruleService.search(request);
        Assert.assertEquals(1, pager.total.intValue());
        Assert.assertEquals(1, pager.records.size());
        Assert.assertEquals(rule.id.intValue(), pager.records.get(0).id.intValue());

        Assert.assertTrue(ruleService.delete(rule.id));
        Assert.assertEquals(0, (long) ruleService.getHistoryList(rule.id, 1, 10).total);
    }
}
