package xin.manong.darwin.common.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author frankcl
 * @date 2023-04-04 15:51:00
 */
public class RuleGroupSuite {

    @Test
    public void testCheckOK() {
        RuleGroup ruleGroup = new RuleGroup();
        ruleGroup.name = "test";
        Assert.assertTrue(ruleGroup.check());
    }

    @Test
    public void testCheckError() {
        RuleGroup ruleGroup = new RuleGroup();
        Assert.assertFalse(ruleGroup.check());
    }
}
