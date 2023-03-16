package xin.manong.darwin.parse.groovy;

import org.junit.Test;

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
            "        System.out.println(\"Hello world!!!\")\n" +
            "        return new ParseResponse();\n" +
            "    }\n" +
            "}";


    @Test
    public void testGroovyScript() {
        GroovyScript groovyScript = new GroovyScript("abc", script);
        groovyScript.execute(null);
        groovyScript.close();
    }
}
