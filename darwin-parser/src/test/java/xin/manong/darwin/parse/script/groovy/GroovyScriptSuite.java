package xin.manong.darwin.parse.script.groovy;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;

/**
 * @author frankcl
 * @date 2023-03-16 16:27:05
 */
public class GroovyScriptSuite {

    private String script = "import xin.manong.darwin.common.parser.ParseRequest;\n" +
            "import xin.manong.darwin.common.parser.ParseResponse;\n" +
            "import xin.manong.darwin.parse.parser.Parser;\n" +
            "\n" +
            "class CustomParser extends Parser {\n" +
            "    \n" +
            "    @Override\n" +
            "    public ParseResponse parse(ParseRequest request) {\n" +
            "        if (request != null && request.record != null) System.out.println(request.record.url);\n" +
            "        Map<String, Object> structureMap = new HashMap<>();\n" +
            "        structureMap.put(\"k1\", 1L);\n" +
            "        return ParseResponse.buildStructureResponse(structureMap, null);\n" +
            "    }\n" +
            "}";


    @Test
    public void testGroovyScript() {
        URLRecord record = new URLRecord("http://www.sina.com.cn/");
        GroovyScript groovyScript = new GroovyScript(1L, script);
        ParseRequest request = new ParseRequest.Builder().content("<p>Hello world!!!</p>").record(record).build();
        ParseResponse response = groovyScript.execute(request);
        Assert.assertTrue(response.status);
        Assert.assertEquals(1, response.structureMap.size());
        Assert.assertTrue(response.structureMap.containsKey("k1"));
        Assert.assertEquals(1L, (long) response.structureMap.get("k1"));
        groovyScript.close();
    }
}