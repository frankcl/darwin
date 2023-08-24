package xin.manong.darwin.parse.script.js;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;

/**
 * @author frankcl
 * @date 2023-03-17 14:56:19
 */
public class JavaScriptSuite {

    private String script = "function parse(request) {\n" +
            "var userDefinedMap = {};\n" +
            "userDefinedMap.url = request.record.url;\n" +
            "var link = buildURLRecord(\"http://www.sohu.com/\", 3);\n" +
            "var followLinks = new Array();\n" +
            "followLinks[0] = link;" +
            "return buildFollowURLsResponse(followLinks);\n" +
            "}";


    @Test
    public void testJavaScript() {
        URLRecord record = new URLRecord("http://www.sina.com.cn/");
        JavaScript javaScript = new JavaScript(1L, script);
        ParseRequest request = new ParseRequest.Builder().content("<p>Hello world!!!</p>").record(record).build();
        ParseResponse response = javaScript.execute(request);
        Assert.assertTrue(response.status);
        Assert.assertTrue(response.structureMap == null);
        Assert.assertEquals(1, response.followURLs.size());
        Assert.assertEquals("http://www.sohu.com/", response.followURLs.get(0).url);
        javaScript.close();
    }
}
