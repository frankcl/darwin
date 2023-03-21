package xin.manong.darwin.common.model;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;

/**
 * @author frankcl
 * @date 2023-03-20 15:48:02
 */
public class RuleSuite {

    @Test
    public void testCheckOK() {
        Rule rule = new Rule();
        rule.name = "测试规则";
        rule.category = Constants.RULE_CATEGORY_LINK_FOLLOW;
        rule.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        rule.script = "def A() {}";
        rule.domain = "sina.com.cn";
        rule.regex = "http://\\w+.sina.com.cn/index.html";
        Assert.assertTrue(rule.check());
    }

    @Test
    public void testCheckError() {
        Rule rule = new Rule();
        rule.name = "测试规则";
        rule.category = 5;
        rule.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        rule.script = "def A() {}";
        rule.regex = "http://\\w+.sina.com.cn/index.html";
        Assert.assertFalse(rule.check());
    }
}
