package xin.manong.darwin.parser.script;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.parser.ApplicationTest;
import xin.manong.darwin.parser.script.groovy.GroovyScript;

/**
 * @author frankcl
 * @date 2023-04-04 17:52:42
 */
public class ScriptFactoryTest {

    @Test
    public void testMake() throws Exception {
        String scriptCode = ApplicationTest.readScript("/script/groovy_script");
        Script script = ScriptFactory.make(Constants.SCRIPT_TYPE_GROOVY, scriptCode);
        Assert.assertTrue(script instanceof GroovyScript);
    }
}
