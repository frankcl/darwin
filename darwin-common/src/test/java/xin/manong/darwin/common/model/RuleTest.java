package xin.manong.darwin.common.model;

import jakarta.ws.rs.BadRequestException;
import org.junit.Test;
import xin.manong.darwin.common.Constants;

/**
 * @author frankcl
 * @date 2023-03-20 15:48:02
 */
public class RuleTest {

    @Test
    public void testCheckOK() {
        Rule rule = new Rule();
        rule.name = "测试规则";
        rule.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        rule.script = "def A() {}";
        rule.regex = "http://\\w+.sina.com.cn/index.html";
        rule.planId = "123";
        rule.check();
    }

    @Test(expected = BadRequestException.class)
    public void testCheckError() {
        Rule rule = new Rule();
        rule.name = "测试规则";
        rule.scriptType = null;
        rule.script = "def A() {}";
        rule.regex = "http://\\w+.sina.com.cn/index.html";
        rule.check();
    }
}
