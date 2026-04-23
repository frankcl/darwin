package xin.manong.darwin.spider.playwright;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author frankcl
 * @date 2026-04-21 16:57:29
 */
public class FeignBrowserTest {

    private FeignBrowser feignBrowser;

    @Before
    public void setUp() {
        feignBrowser = new FeignBrowser(FingerprintProfile.MAC, "/Users/frankcl/Downloads/chrome-mac-arm64/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing");
    }

    @After
    public void tearDown() throws Exception {
        feignBrowser.close();
    }

    @Test
    public void testFetch() throws Exception {
        String url = "https://www.firstsilicon.co.kr/pro10/business/download?cate_id=0111&pd_id=3604&pf_id=0";
        FetchRequest.Builder builder = FetchRequest.builder().requestURL(url);
        try (FetchResponse response = feignBrowser.fetch(builder.build())) {
            Assert.assertTrue(response.isStatus());
            Assert.assertEquals(200, response.getHttpCode());
            Assert.assertNotNull(response.getResponseBody());
        }
    }
}
