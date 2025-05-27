package xin.manong.darwin.parser.script.js;

import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.darwin.parser.ApplicationTest;
import xin.manong.darwin.parser.config.ParseConfig;
import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseRequestBuilder;
import xin.manong.darwin.parser.sdk.ParseResponse;

/**
 * @author frankcl
 * @date 2023-03-17 14:56:19
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "parser", "parser-dev", "service", "service-dev", "queue", "queue-dev", "log" })
@SpringBootTest(classes = ApplicationTest.class)
public class JavaScriptTest {

    @Resource
    private ParseConfig parseConfig;

    @Test
    public void testBuildJavaScript() throws Exception {
        String url = "http://www.sina.com.cn/";
        String scriptCode = ApplicationTest.readScript("/script/java_script");
        JavaScript javaScript = new JavaScript(scriptCode, parseConfig.requireCwd);
        ParseRequest request = new ParseRequestBuilder().text("<!DOCTYPE html><html><meta><body><p>Hello world!!!</p></body></html>").url(url).build();
        ParseResponse response = javaScript.execute(request);
        System.out.println(response.stdout);
        Assert.assertTrue(response.status);
        Assert.assertNull(response.fieldMap);
        Assert.assertEquals("debug test\n", response.stdout);
        Assert.assertEquals(1, response.children.size());
        Assert.assertEquals("http://www.sohu.com/", response.children.get(0).url);
        Assert.assertTrue(response.customMap != null && response.customMap.containsKey("url"));
        Assert.assertEquals("http://www.sina.com.cn/", response.customMap.get("url"));
        javaScript.close();
    }
}
