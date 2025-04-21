package xin.manong.darwin.queue;

import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-03-08 14:58:18
 */
@EnableRedisClient
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(value = { "queue", "queue-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class ConcurrencyQueueTest {

    @Resource
    private ConcurrencyQueue concurrencyQueue;

    private URLRecord buildRecord(String url, int priority, int concurrentLevel) {
        URLRecord record = URLRecord.buildListLink(url);
        record.jobId = "abc";
        record.planId = "abc";
        record.appId = 0;
        record.priority = priority;
        record.concurrentLevel = concurrentLevel;
        return record;
    }

    @Test
    public void testConcurrencyQueue() {
        List<URLRecord> records = new ArrayList<>();
        records.add(buildRecord("http://www.sina.com.cn", Constants.PRIORITY_LOW, Constants.CONCURRENT_LEVEL_HOST));
        records.add(buildRecord("http://www.sina.com.cn/index.html", Constants.PRIORITY_HIGH, Constants.CONCURRENT_LEVEL_HOST));
        List<PushResult> pushResults = concurrencyQueue.push(records);
        Assert.assertEquals(2, pushResults.size());
        Assert.assertEquals(PushResult.SUCCESS, pushResults.get(0));
        Assert.assertEquals(PushResult.SUCCESS, pushResults.get(1));
        URLRecord record = buildRecord("http://www.sohu.com/index.html",
                Constants.PRIORITY_HIGH, Constants.CONCURRENT_LEVEL_DOMAIN);
        Assert.assertEquals(PushResult.SUCCESS, concurrencyQueue.push(record));

        Assert.assertTrue(concurrencyQueue.canPush());

        records = concurrencyQueue.pop("www.sina.com.cn", 3);
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("abc", records.get(0).jobId);
        Assert.assertEquals("http://www.sina.com.cn/index.html", records.get(0).url);
        Assert.assertEquals(Constants.PRIORITY_HIGH, records.get(0).priority.intValue());
        Assert.assertEquals(Constants.CONTENT_CATEGORY_LIST, records.get(0).category.intValue());
        Assert.assertEquals(Constants.CONCURRENT_LEVEL_HOST, records.get(0).concurrentLevel.intValue());

        Assert.assertEquals("abc", records.get(1).jobId);
        Assert.assertEquals("http://www.sina.com.cn", records.get(1).url);
        Assert.assertEquals(Constants.PRIORITY_LOW, records.get(1).priority.intValue());
        Assert.assertEquals(Constants.CONTENT_CATEGORY_LIST, records.get(1).category.intValue());
        Assert.assertEquals(Constants.CONCURRENT_LEVEL_HOST, records.get(1).concurrentLevel.intValue());

        records = concurrencyQueue.pop("sohu.com", 2);
        Assert.assertEquals(1, records.size());
        Assert.assertEquals("abc", records.get(0).jobId);
        Assert.assertEquals("http://www.sohu.com/index.html", records.get(0).url);
        Assert.assertEquals(Constants.PRIORITY_HIGH, records.get(0).priority.intValue());
        Assert.assertEquals(Constants.CONTENT_CATEGORY_LIST, records.get(0).category.intValue());
        Assert.assertEquals(Constants.CONCURRENT_LEVEL_DOMAIN, records.get(0).concurrentLevel.intValue());

        Assert.assertTrue(concurrencyQueue.removeConcurrentUnit("www.sina.com.cn"));
        Assert.assertTrue(concurrencyQueue.removeConcurrentUnit("sohu.com"));
    }
}
