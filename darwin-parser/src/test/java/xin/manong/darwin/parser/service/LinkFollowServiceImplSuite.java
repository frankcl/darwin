package xin.manong.darwin.parser.service;

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
import xin.manong.darwin.parser.service.request.HTMLParseRequest;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author frankcl
 * @date 2023-11-15 15:43:21
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "parse", "parse-dev" })
@SpringBootTest(classes = ApplicationTest.class)
public class LinkFollowServiceImplSuite {

    @Resource
    private LinkFollowService linkFollowService;
    private HttpClient httpClient = new HttpClient();

    private String fetch(String url) throws IOException {
        HttpRequest httpRequest = HttpRequest.buildGetRequest(url, null);
        Response response = httpClient.execute(httpRequest);
        try {
            if (response == null || !response.isSuccessful() ||
                    response.code() != 200) return null;
            return response.body().string();
        } finally {
            if (response != null) response.close();
        }
    }

    @Test
    public void testLinkFollow() throws Exception {
        String url = "http://www.sina.com.cn";
        HTMLParseRequest request = new HTMLParseRequest();
        request.url = url;
        request.html = fetch(url);
        request.scope = Constants.LINK_SCOPE_HOST;
        ParseResponse response = linkFollowService.parse(request);
        Assert.assertTrue(response != null && response.status);
    }

}
