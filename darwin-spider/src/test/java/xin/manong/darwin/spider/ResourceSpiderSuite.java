package xin.manong.darwin.spider;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.util.RandomID;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-03-30 15:50:07
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "dev", "service", "service-dev", "parse", "parse-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = ApplicationTest.class)
public class ResourceSpiderSuite {

    @Resource
    protected SpiderConfig config;
    @Resource
    protected OSSClient ossClient;
    @Resource
    protected ResourceSpider spider;

    @Test
    public void testFetchSuccess() {
        String url = "https://default-crawler-file.oss-cn-hangzhou.aliyuncs.com/crawler_video/video/2021/11/11/4d5e356b35138269fb4492b52f834727.mp4";
        URLRecord record = new URLRecord(url);
        record.category = Constants.CONTENT_CATEGORY_RESOURCE;
        record.jobId = RandomID.build();
        record.appId = 0;
        Context context = new Context();
        spider.process(record, context);
        String key = String.format("%s/%s/%s.mp4", config.contentDirectory, "resource", record.key);
        OSSMeta ossMeta = new OSSMeta();
        ossMeta.region = config.contentRegion;
        ossMeta.bucket = config.contentBucket;
        ossMeta.key = key;
        Assert.assertEquals(Constants.URL_STATUS_SUCCESS, record.status.intValue());
        Assert.assertEquals(OSSClient.buildURL(ossMeta), record.fetchContentURL);
        Assert.assertTrue(record.fetchTime != null && record.fetchTime > 0L);
        Assert.assertTrue(!StringUtils.isEmpty(record.fetchContentURL));
        ossMeta = OSSClient.parseURL(record.fetchContentURL);
        Assert.assertTrue(ossClient.exist(ossMeta.bucket, ossMeta.key));
        ossClient.deleteObject(ossMeta.bucket, ossMeta.key);
    }

    @Test
    public void testFetchFail() {
        String url = "https://default-crawler-file.oss-cn-hangzhou.aliyuncs.com/crawler_video/video/not_found.mp4";
        URLRecord record = new URLRecord(url);
        record.category = Constants.CONTENT_CATEGORY_RESOURCE;
        record.jobId = RandomID.build();
        record.appId = 0;
        Context context = new Context();
        spider.process(record, context);
        Assert.assertEquals(Constants.URL_STATUS_FAIL, record.status.intValue());
        Assert.assertTrue(record.fetchTime != null && record.fetchTime > 0L);
        Assert.assertTrue(StringUtils.isEmpty(record.fetchContentURL));
    }
}
