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
import xin.manong.darwin.parser.service.request.ScriptParseRequest;

/**
 * @author frankcl
 * @date 2023-11-15 15:43:21
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "parse", "parse-dev" })
@SpringBootTest(classes = ApplicationTest.class)
public class LinkExtractServiceImplTest {

    @Resource
    private LinkExtractService linkExtractService;

    @Test
    public void testScopeExtract() throws Exception {
        String url = "http://www.sina.com.cn";
        ScriptParseRequest request = new ScriptParseRequest();
        request.url = url;
        request.html = "<html><body></body</html>";
        request.linkScope = Constants.LINK_SCOPE_HOST;
        ParseResponse response = linkExtractService.extract(request);
        Assert.assertTrue(response != null && response.status);
    }

}
