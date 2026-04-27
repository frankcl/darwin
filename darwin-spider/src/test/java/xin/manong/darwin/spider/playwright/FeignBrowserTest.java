package xin.manong.darwin.spider.playwright;

import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author frankcl
 * @date 2026-04-21 16:57:29
 */
public class FeignBrowserTest {

    private static FeignBrowser feignBrowser;

    @BeforeClass
    public static void setUp() {
        feignBrowser = new FeignBrowser(FingerprintProfile.MAC);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        feignBrowser.close();
    }

    @Test
    public void testDownload() throws Exception {
        String url = "https://www.firstsilicon.co.kr/pro10/business/download?cate_id=0111&pd_id=3604&pf_id=0";
        Map<String, String> headers = Map.of("Cookie", "dt=dt; CUPID=8fa3f1c9a71d0617d1985c850ff73daf; PHPSESSID=m20ccrgevhklj1f9r4dea60bme; 2a0d2363701f23f8a75028924a3af643=MTQ2LjE5MC4xNTEuNjI%3D");
        FetchRequest.Builder builder = FetchRequest.builder().requestURL(url).headers(headers);
        try (FetchResponse response = feignBrowser.fetch(builder.build())) {
            Assert.assertTrue(response.isStatus());
            Assert.assertEquals(200, response.getHttpCode());
            Assert.assertNotNull(response.getResponseBody());
        }
    }

    @Test
    public void testFetchGet() throws Exception {
        String url = "https://www.silergy.com/list/288";
        FetchRequest.Builder builder = FetchRequest.builder().requestURL(url);
        try (FetchResponse response = feignBrowser.fetch(builder.build())) {
            Assert.assertTrue(response.isStatus());
            Assert.assertEquals(200, response.getHttpCode());
            Assert.assertNotNull(response.getResponseBody());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            response.getResponseBody().transferTo(outputStream);
            System.out.println(outputStream.toString(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testFetchPost() throws Exception {
        String url = "https://www.tukuppt.com/api/audio";
        Map<String, Object> requestBody = Map.of("value", "9020804");
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Referer", "https://www.tukuppt.com/peiyue/m109/",
                "Host", "www.tukuppt.com");
        FetchRequest.Builder builder = FetchRequest.builder().requestURL(url).
                method(FetchRequest.METHOD_POST).requestBody(requestBody).headers(headers);
        try (FetchResponse response = feignBrowser.fetch(builder.build())) {
            Assert.assertTrue(response.isStatus());
            Assert.assertEquals(200, response.getHttpCode());
            Assert.assertNotNull(response.getResponseBody());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            response.getResponseBody().transferTo(outputStream);
            System.out.println(outputStream.toString(StandardCharsets.UTF_8));
        }
    }
}
