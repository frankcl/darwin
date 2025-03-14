package xin.manong.darwin.parser.service;

import jakarta.annotation.Resource;
import okhttp3.Response;
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
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;

import java.io.IOException;

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
    private final HttpClient httpClient = new HttpClient();

    private String fetch(String url) throws IOException {
        HttpRequest httpRequest = HttpRequest.buildGetRequest(url, null);
        try (Response response = httpClient.execute(httpRequest)) {
            if (response == null || !response.isSuccessful() ||
                    response.code() != 200) return null;
            assert response.body() != null;
            return response.body().string();
        }
    }

    @Test
    public void testScopeExtract() throws Exception {
        String url = "http://www.sina.com.cn";
        ScriptParseRequest request = new ScriptParseRequest();
        request.url = url;
        request.html = fetch(url);
        request.linkScope = Constants.LINK_SCOPE_HOST;
        ParseResponse response = linkExtractService.extract(request);
        Assert.assertTrue(response != null && response.status);
    }

}
