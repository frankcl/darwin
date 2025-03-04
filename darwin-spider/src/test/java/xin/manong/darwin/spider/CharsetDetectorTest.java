package xin.manong.darwin.spider;

import okhttp3.Response;
import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.service.component.CharsetDetector;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestMethod;

/**
 * @author frankcl
 * @date 2023-04-07 16:39:53
 */
public class CharsetDetectorTest {

    @Test
    public void testDetect() throws Exception {
        String url = "http://politics.people.com.cn/n1/2023/0406/c1001-32658085.html";
        HttpClient httpClient = new HttpClient();
        HttpRequest httpRequest = new HttpRequest.Builder().requestURL(url).method(RequestMethod.GET).build();
        byte[] body;
        try (Response response = httpClient.execute(httpRequest)) {
            Assert.assertTrue(response != null && response.isSuccessful());
            Assert.assertNotNull(response.body());
            body = response.body().bytes();
        }
        Assert.assertEquals("GB18030", CharsetDetector.detect(body));
    }
}
