package xin.manong.darwin.parse.script.js;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.parser.LinkURL;
import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;

/**
 * @author frankcl
 * @date 2023-03-17 14:56:19
 */
public class JavaScriptSuite {

    private String script = "function parse(request) {\n" +
            "var userDefinedMap = {};\n" +
            "userDefinedMap.url = request.linkURL.url;\n" +
            "var linkURL = buildLinkURL(\"http://www.sohu.com/\");\n" +
            "var followLinks = new Array();\n" +
            "followLinks[0] = linkURL;" +
            "return buildFollowLinkResponse(followLinks, userDefinedMap);\n" +
            "}";


    @Test
    public void testJavaScript() {
        LinkURL linkURL = new LinkURL("http://www.sina.com.cn/");
        JavaScript javaScript = new JavaScript(1L, script);
        ParseRequest request = new ParseRequest.Builder().content("<p>Hello world!!!</p>").linkURL(linkURL).build();
        ParseResponse response = javaScript.execute(request);
        Assert.assertTrue(response.status);
        Assert.assertTrue(response.structureMap == null);
        Assert.assertEquals(1, response.userDefinedMap.size());
        Assert.assertTrue(response.userDefinedMap.containsKey("url"));
        Assert.assertEquals("http://www.sina.com.cn/", response.userDefinedMap.get("url"));
        Assert.assertEquals(1, response.followLinks.size());
        Assert.assertEquals("http://www.sohu.com/", response.followLinks.get(0).url);
        javaScript.close();
    }
}
