package xin.manong.darwin.parser.service;

import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.parser.ApplicationTest;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.request.CompileRequest;
import xin.manong.darwin.parser.service.request.ScriptParseRequest;
import xin.manong.darwin.parser.service.request.ScriptParseRequestBuilder;
import xin.manong.darwin.parser.service.response.CompileResult;

/**
 * @author frankcl
 * @date 2023-04-04 18:00:24
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "parser" })
@SpringBootTest(classes = ApplicationTest.class)
public class ParseServiceImplTest {

    @Resource
    protected ParseService parseService;

    @Test
    public void testCompile() throws Exception {
        String scriptCode = ApplicationTest.readScript("/script/groovy_script");
        CompileRequest request = new CompileRequest();
        request.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        request.script = scriptCode;
        CompileResult result = parseService.compile(request);
        Assert.assertTrue(result.status);
    }

    @Test
    public void testParse() throws Exception {
        String scriptCode = ApplicationTest.readScript("/script/groovy_script");
        ScriptParseRequest request = new ScriptParseRequestBuilder().url("http://www.sina.com.cn/").
                text("<p>Hello world</p>").scriptType(Constants.SCRIPT_TYPE_GROOVY).scriptCode(scriptCode).build();
        ParseResponse response = parseService.parse(request);
        Assert.assertTrue(response != null && response.status);
        Assert.assertTrue(response.fieldMap != null && !response.fieldMap.isEmpty());
        Assert.assertTrue(response.fieldMap.containsKey("k1"));
        Assert.assertEquals(1L, (long) response.fieldMap.get("k1"));
    }
}
