package xin.manong.darwin.parser.script.groovy;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.parser.ApplicationTest;
import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseRequestBuilder;
import xin.manong.darwin.parser.sdk.ParseResponse;

/**
 * @author frankcl
 * @date 2023-03-16 16:27:05
 */
public class GroovyScriptTest {

    @Test
    public void testBuildGroovyScript() throws Exception {
        String url = "http://www.sina.com.cn/";
        String scriptCode = ApplicationTest.readScript("/script/groovy_script");
        GroovyScript groovyScript = new GroovyScript(scriptCode);
        ParseRequest request = new ParseRequestBuilder().text("<p>Hello world!!!</p>").url(url).build();
        ParseResponse response = groovyScript.execute(request);
        Assert.assertTrue(response.status);
        Assert.assertEquals(1, response.fieldMap.size());
        Assert.assertTrue(response.fieldMap.containsKey("k1"));
        Assert.assertEquals(1L, (long) response.fieldMap.get("k1"));
        groovyScript.close();
    }
}
