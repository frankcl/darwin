package xin.manong.darwin.spider;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.OSSService;
import xin.manong.darwin.spider.core.Router;
import xin.manong.darwin.spider.core.SpiderConfig;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.util.RandomID;

/**
 * @author frankcl
 * @date 2023-03-30 15:50:07
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "spider", "spider-dev", "service", "service-dev", "parse", "parse-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class ResourceSpiderTest {

    @Resource
    private SpiderConfig spiderConfig;
    @Resource
    private OSSService ossService;
    @Resource
    private Router router;

    @Test
    public void testFetchSuccess() {
        String url = "https://default-crawler-file.oss-cn-hangzhou.aliyuncs.com/crawler_video/video/2021/11/11/4d5e356b35138269fb4492b52f834727.mp4";
        URLRecord record = new URLRecord(url);
        record.jobId = RandomID.build();
        record.appId = 0;
        record.concurrencyUnit = "default-crawler-file.oss-cn-hangzhou.aliyuncs.com";
        record.concurrencyLevel = Constants.CONCURRENCY_LEVEL_HOST;
        Context context = new Context();
        router.route(record, context);
        String key = String.format("%s/%s/%s.mp4", spiderConfig.ossDirectory, "video", record.key);
        Assert.assertEquals(Constants.URL_STATUS_FETCH_SUCCESS, record.status.intValue());
        Assert.assertEquals(Constants.CONTENT_TYPE_VIDEO, record.contentType.intValue());
        Assert.assertEquals(ossService.buildURL(key), record.fetchContentURL);
        Assert.assertTrue(record.fetchTime != null && record.fetchTime > 0L);
        Assert.assertFalse(StringUtils.isEmpty(record.fetchContentURL));
        Assert.assertTrue(ossService.existsByKey(key));
        ossService.deleteByKey(key);
    }

    @Test
    public void testFetchFail() {
        String url = "https://default-crawler-file.oss-cn-hangzhou.aliyuncs.com/crawler_video/video/not_found.mp4";
        URLRecord record = new URLRecord(url);
        record.jobId = RandomID.build();
        record.appId = 0;
        record.concurrencyUnit = "default-crawler-file.oss-cn-hangzhou.aliyuncs.com";
        record.concurrencyLevel = Constants.CONCURRENCY_LEVEL_HOST;
        Context context = new Context();
        router.route(record, context);
        Assert.assertNull(record.contentType);
        Assert.assertEquals(Constants.URL_STATUS_FETCH_FAIL, record.status.intValue());
        Assert.assertTrue(record.fetchTime != null && record.fetchTime > 0L);
        Assert.assertTrue(StringUtils.isEmpty(record.fetchContentURL));
    }
}
