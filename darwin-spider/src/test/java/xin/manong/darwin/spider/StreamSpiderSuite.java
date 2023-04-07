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
        String url = "http://cache.m.iqiyi.com/mus/5328486914190101/f15b95b57bb31de443b7b34d82a69f96/afbe8fd3d73448c9/0/20230126/12/7b/4ba005eb2461eb51dba2a3df86a91ba7.m3u8?qd_originate=tmts_py&tvid=3339115613385800&bossStatus=0&qd_vip=0&px=&src=3_31_312&prv=&previewType=&previewTime=&from=&qd_time=1680771253435&qd_p=314b5d08&qd_asc=0f9cd0cb131dc56efc34cdb2ea62d961&qypid=3339115613385800_04022000001000000000_2&qd_k=2a681ed442d31c757f9331da3310fb32&isdol=0&code=2&ff=f4v&iswb=0&qd_s=otv&vf=ddb0f2a7fa3e82b00c05c044f4e208b6&np_tag=nginx_part_tag";
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
