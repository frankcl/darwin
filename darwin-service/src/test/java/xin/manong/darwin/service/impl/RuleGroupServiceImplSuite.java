package xin.manong.darwin.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RuleGroup;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.RuleGroupService;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-04-04 14:10:46
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class RuleGroupServiceImplSuite {

    @Resource
    protected RuleGroupService ruleGroupService;

    @Test
    @Transactional
    @Rollback
    public void testRuleGroupOperations() {
        RuleGroup ruleGroup = new RuleGroup();
        ruleGroup.name = "测试规则分组";
        Assert.assertTrue(ruleGroupService.add(ruleGroup));
        Assert.assertTrue(ruleGroup.id != null && ruleGroup.id > 0L);

        RuleGroup updateRuleGroup = new RuleGroup();
        updateRuleGroup.id = ruleGroup.id;
        updateRuleGroup.name = "测试规则分组plus";
        Assert.assertTrue(ruleGroupService.update(updateRuleGroup));

        RuleGroup getRuleGroup = ruleGroupService.get(ruleGroup.id);
        Assert.assertTrue(getRuleGroup != null);
        Assert.assertEquals("测试规则分组plus", getRuleGroup.name);

        Pager<RuleGroup> pager = ruleGroupService.search("测试规则", 1, 10);
        Assert.assertEquals(1, pager.total.intValue());
        Assert.assertEquals(1, pager.records.size());
        Assert.assertEquals(ruleGroup.id.longValue(), pager.records.get(0).id.longValue());

        Assert.assertTrue(ruleGroupService.delete(ruleGroup.id));
    }
}
