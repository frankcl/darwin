package xin.manong.darwin.parser.script.js;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.parser.ApplicationTest;
import xin.manong.darwin.parser.sdk.ParseRequest;
import xin.manong.darwin.parser.sdk.ParseRequestBuilder;
import xin.manong.darwin.parser.sdk.ParseResponse;

/**
 * @author frankcl
 * @date 2023-03-17 14:56:19
 */
public class JavaScriptTest {

    @Test
    public void testBuildJavaScript() throws Exception {
        String url = "http://www.sina.com.cn/";
        String scriptCode = ApplicationTest.readScript("/script/java_script");
        JavaScript javaScript = new JavaScript(scriptCode);
        ParseRequest request = new ParseRequestBuilder().text("<p>Hello world!!!</p>").url(url).build();
        ParseResponse response = javaScript.execute(request);
        Assert.assertTrue(response.status);
        Assert.assertNull(response.fieldMap);
        Assert.assertEquals(1, response.children.size());
        Assert.assertEquals("http://www.sohu.com/", response.children.get(0).url);
        Assert.assertTrue(response.customMap != null && response.customMap.containsKey("url"));
        Assert.assertEquals("http://www.sina.com.cn/", response.customMap.get("url"));
        javaScript.close();
    }
}
