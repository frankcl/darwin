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
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.request.RuleSearchRequest;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-04-04 14:14:39
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class RuleServiceImplSuite {

    @Resource
    protected RuleService ruleService;

    @Test
    @Transactional
    @Rollback
    public void testRuleGroupOperations() {
        Rule rule = new Rule();
        rule.name = "测试规则";
        rule.ruleGroup = 1;
        rule.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        rule.script = "function";
        rule.regex = "http://www.sina.com.cn/\\d+.html";
        Assert.assertTrue(rule.check());

        Assert.assertTrue(ruleService.add(rule));
        Assert.assertTrue(rule.id != null && rule.id > 0L);

        Rule updateRule = new Rule();
        updateRule.id = rule.id;
        updateRule.name = "test rule";
        updateRule.script = "function() {}";
        Assert.assertTrue(ruleService.update(updateRule));

        Rule getRule = ruleService.get(rule.id);
        Assert.assertTrue(getRule != null);
        Assert.assertEquals("test rule", getRule.name);
        Assert.assertEquals(1L, getRule.ruleGroup.longValue());
        Assert.assertEquals(Constants.SCRIPT_TYPE_GROOVY, getRule.scriptType.intValue());
        Assert.assertEquals("function() {}", getRule.script);
        Assert.assertEquals("http://www.sina.com.cn/\\d+.html", getRule.regex);
        Assert.assertEquals("sina.com.cn", getRule.domain);

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
    }
}
