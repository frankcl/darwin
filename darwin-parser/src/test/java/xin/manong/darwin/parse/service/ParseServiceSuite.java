package xin.manong.darwin.parse.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.parser.ParseRequest;
import xin.manong.darwin.common.parser.ParseResponse;
import xin.manong.darwin.service.iface.RuleService;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-04-04 18:00:24
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "parse", "parse-dev", "service", "service-dev", "queue", "queue-dev" })
@SpringBootTest(classes = ApplicationTest.class)
public class ParseServiceSuite {

    private String scriptText = "import xin.manong.darwin.common.parser.ParseRequest;\n" +
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

    @Resource
    protected RuleService ruleService;
    @Resource
    protected ParseService parseService;

    @Test
    public void testParseWithScriptText() {
        URLRecord record = new URLRecord("http://www.sina.com.cn/");
        record.appId = 1;
        record.jobId = "xxx";
        record.category = Constants.CONTENT_CATEGORY_LIST;
        ParseRequest request = new ParseRequest.Builder().record(record).content("<p>Hello world</p>").build();
        ParseResponse response = parseService.parse(Constants.SCRIPT_TYPE_GROOVY, scriptText, request);
        Assert.assertTrue(response != null && response.status);
        Assert.assertTrue(response.structureMap != null && !response.structureMap.isEmpty());
        Assert.assertTrue(response.structureMap.containsKey("k1"));
        Assert.assertEquals(1L, (long) response.structureMap.get("k1"));
    }

    @Test
    @Rollback
    @Transactional
    public void testParseWithRule() {
        Rule rule = new Rule();
        rule.ruleGroup = 1L;
        rule.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        rule.script = scriptText;
        rule.category = Constants.RULE_CATEGORY_STRUCTURE;
        rule.name = "测试规则";
        rule.regex = "http://www\\.sina\\.com\\.cn/.*";
        rule.domain = "sina.com.cn";
        Assert.assertTrue(ruleService.add(rule));

        try {
            URLRecord record = new URLRecord("http://www.sina.com.cn/");
            record.appId = 1;
            record.jobId = "xxx";
            record.category = Constants.CONTENT_CATEGORY_LIST;
            ParseRequest request = new ParseRequest.Builder().record(record).content("<p>Hello world</p>").build();
            ParseResponse response = parseService.parse(rule.id, request);
            Assert.assertTrue(response != null && response.status);
            Assert.assertTrue(response.structureMap != null && !response.structureMap.isEmpty());
            Assert.assertTrue(response.structureMap.containsKey("k1"));
            Assert.assertEquals(1L, (long) response.structureMap.get("k1"));

            response = parseService.parse(rule, request);
            Assert.assertTrue(response != null && response.status);
            Assert.assertTrue(response.structureMap != null && !response.structureMap.isEmpty());
            Assert.assertTrue(response.structureMap.containsKey("k1"));
            Assert.assertEquals(1L, (long) response.structureMap.get("k1"));
        } finally {
            ruleService.delete(rule.id);
        }
    }
}
