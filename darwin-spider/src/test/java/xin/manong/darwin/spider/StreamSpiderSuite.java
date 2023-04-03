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
 * @date 2023-03-31 14:36:24
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "dev", "service", "service-dev", "queue", "queue-dev" })
@SpringBootTest(classes = ApplicationTest.class)
public class StreamSpiderSuite {

    @Resource
    protected SpiderConfig config;
    @Resource
    protected OSSClient ossClient;
    @Resource
    protected StreamSpider spider;

    @Test
    public void testFetchSuccess() {
        String url = "http://kbs-dokdo.gscdn.com/dokdo_300/_definst_/dokdo_300.stream/playlist.m3u8";
        URLRecord record = new URLRecord(url);
        record.category = Constants.CONTENT_CATEGORY_STREAM;
        record.jobId = RandomID.build();
        record.appId = 0;
        Context context = new Context();
        spider.process(record, context);
        String key = String.format("%s/%s/%s.mp4", config.contentDirectory, "stream", record.key);
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
    public void testFetchLiveStream() {
        String url = "http://kbs-dokdo.gscdn.com/dokdo_300/_definst_/dokdo_300.stream/playlist.m3u8";
        URLRecord record = new URLRecord(url);
        record.category = Constants.CONTENT_CATEGORY_STREAM;
        record.jobId = RandomID.build();
        record.appId = 0;
        Context context = new Context();
        spider.process(record, context);
        Assert.assertEquals(Constants.URL_STATUS_FAIL, record.status.intValue());
        Assert.assertTrue(record.fetchTime != null && record.fetchTime > 0L);
        Assert.assertTrue(StringUtils.isEmpty(record.fetchContentURL));
    }
}
