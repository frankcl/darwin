package xin.manong.darwin.parse.script;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.parse.script.groovy.GroovyScript;
import xin.manong.darwin.parse.script.js.JavaScript;

/**
 * @author frankcl
 * @date 2023-04-04 17:52:42
 */
public class ScriptFactorySuite {

    @Test
    public void testMakeWithScript() {
        String scriptText = "import xin.manong.darwin.common.parser.ParseRequest;\n" +
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
        Script script = ScriptFactory.make(Constants.SCRIPT_TYPE_GROOVY, scriptText);
        Assert.assertTrue(script != null && script instanceof GroovyScript);
    }

    @Test
    public void testMakeWithRule() {
        Rule rule = new Rule();
        rule.scriptType = Constants.SCRIPT_TYPE_JAVASCRIPT;
        rule.script = "function parse(request) {\n" +
                "var userDefinedMap = {};\n" +
                "userDefinedMap.url = request.record.url;\n" +
                "var link = buildLink(\"http://www.sohu.com/\", 3);\n" +
                "var followLinks = new Array();\n" +
                "followLinks[0] = link;" +
                "return buildFollowLinkResponse(followLinks);\n" +
                "}";
        Script script = ScriptFactory.make(rule);
        Assert.assertTrue(script != null && script instanceof JavaScript);
    }
}
