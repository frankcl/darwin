package xin.manong.darwin.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;

/**
 * @author frankcl
 * @date 2025-04-29 19:29:57
 */
public class URLNormalizerTest {

    @Test
    public void testNormalize() throws MalformedURLException {
        Assert.assertEquals("http://news.sina.com.cn/hotnews/",
                URLNormalizer.normalize("http://news.sina.com.cn/hotnews/#a_slide"));
        Assert.assertEquals("https://news.sina.com.cn/w/2025-04-29/doc-ineuuwaa1219036.shtml",
                URLNormalizer.normalize("https://news.sina.com.cn/w/2025-04-29/doc-ineuuwaa1219036.shtml#"));
        Assert.assertEquals("https://news.sina.com.cn:123/w/2025-04-29/doc-ineuuwaa1219036.shtml?a=3&b=1",
                URLNormalizer.normalize("https://news.sina.com.cn:123/w/2025-04-29/doc-ineuuwaa1219036.shtml?b=1&a=3#"));
        Assert.assertEquals("https://cache.m.iqiyi.com/tmts/1440389051834800/dc2ae689658878a6deb21bc925400b7f/?qdv=1&src=02029022240000000000&t=1671073697282&type=m3u8&vf=23df6a67f548661926f33bdb84df046b",
                URLNormalizer.normalize("https://cache.m.iqiyi.com/tmts/1440389051834800/dc2ae689658878a6deb21bc925400b7f/?type=m3u8&qdv=1&t=1671073697282&src=02029022240000000000&vf=23df6a67f548661926f33bdb84df046b"));
    }
}
