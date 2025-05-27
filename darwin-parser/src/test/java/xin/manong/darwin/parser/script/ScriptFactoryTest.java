package xin.manong.darwin.parser.script;

import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.parser.ApplicationTest;
import xin.manong.darwin.parser.script.groovy.GroovyScript;

/**
 * @author frankcl
 * @date 2023-04-04 17:52:42
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "parser", "parser-dev", "service", "service-dev", "queue", "queue-dev", "log" })
@SpringBootTest(classes = ApplicationTest.class)
public class ScriptFactoryTest {

    @Resource
    private ScriptFactory scriptFactory;

    @Test
    public void testMake() throws Exception {
        String scriptCode = ApplicationTest.readScript("/script/groovy_script");
        try (Script script = scriptFactory.make(Constants.SCRIPT_TYPE_GROOVY, scriptCode)) {
            Assert.assertTrue(script instanceof GroovyScript);
        }
    }
}
