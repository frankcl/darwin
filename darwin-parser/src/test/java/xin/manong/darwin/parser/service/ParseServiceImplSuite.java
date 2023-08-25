package xin.manong.darwin.parser.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.parser.ApplicationTest;
import xin.manong.darwin.parser.sdk.ParseResponse;
import xin.manong.darwin.parser.service.request.HTMLScriptRequest;
import xin.manong.darwin.parser.service.request.HTMLScriptRequestBuilder;
import xin.manong.darwin.parser.service.response.CompileResponse;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-04-04 18:00:24
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "parse", "parse-dev" })
@SpringBootTest(classes = ApplicationTest.class)
public class ParseServiceImplSuite {

    @Resource
    protected ParseService parseService;

    @Test
    public void testCompile() throws Exception {
        String scriptCode = ApplicationTest.readScript("/script/groovy_script");
        CompileResponse response = parseService.compile(Constants.SCRIPT_TYPE_GROOVY, scriptCode);
        Assert.assertTrue(response.status);
    }

    @Test
    public void testParse() throws Exception {
        String scriptCode = ApplicationTest.readScript("/script/groovy_script");
        HTMLScriptRequest request = new HTMLScriptRequestBuilder().url("http://www.sina.com.cn/").
                html("<p>Hello world</p>").scriptType(Constants.SCRIPT_TYPE_GROOVY).scriptCode(scriptCode).build();
        ParseResponse response = parseService.parse(request);
        Assert.assertTrue(response != null && response.status);
        Assert.assertTrue(response.fieldMap != null && !response.fieldMap.isEmpty());
        Assert.assertTrue(response.fieldMap.containsKey("k1"));
        Assert.assertEquals(1L, (long) response.fieldMap.get("k1"));
    }
}
