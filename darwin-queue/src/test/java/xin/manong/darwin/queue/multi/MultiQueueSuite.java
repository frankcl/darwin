package xin.manong.darwin.queue.multi;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.ApplicationTest;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;

import javax.annotation.Resource;
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
public class MultiQueueSuite {

    @Resource
    private MultiQueue multiQueue;

    @Test
    public void testMultiQueue() {
        List<URLRecord> records = new ArrayList<>();
        {
            URLRecord record = new URLRecord("http://www.sina.com.cn");
            record.category = Constants.CONTENT_CATEGORY_TEXT;
            record.jobId = "abc";
            record.planId = "abc";
            record.appId = 0;
            record.priority = Constants.PRIORITY_LOW;
            record.concurrentLevel = Constants.CONCURRENT_LEVEL_HOST;
            records.add(record);
        }
        {
            URLRecord record = new URLRecord("http://www.sina.com.cn/index.html");
            record.category = Constants.CONTENT_CATEGORY_TEXT;
            record.jobId = "abc";
            record.planId = "abc";
            record.appId = 0;
            record.priority = Constants.PRIORITY_HIGH;
            record.concurrentLevel = Constants.CONCURRENT_LEVEL_HOST;
            records.add(record);
        }
        List<MultiQueueStatus> statusList = multiQueue.push(records);
        Assert.assertEquals(2, statusList.size());
        Assert.assertEquals(MultiQueueStatus.OK, statusList.get(0));
        Assert.assertEquals(MultiQueueStatus.OK, statusList.get(1));
        {
            URLRecord record = new URLRecord("http://www.sohu.com/index.html");
            record.category = Constants.CONTENT_CATEGORY_TEXT;
            record.jobId = "def";
            record.planId = "def";
            record.appId = 0;
            record.priority = Constants.PRIORITY_HIGH;
            record.concurrentLevel = Constants.CONCURRENT_LEVEL_DOMAIN;
            Assert.assertEquals(MultiQueueStatus.OK, multiQueue.push(record));
        }
        Assert.assertFalse(multiQueue.refuseService());
        records = multiQueue.pop("www.sina.com.cn", 3);
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("abc", records.get(0).jobId);
        Assert.assertEquals("http://www.sina.com.cn/index.html", records.get(0).url);
        Assert.assertEquals(Constants.PRIORITY_HIGH, records.get(0).priority.intValue());
        Assert.assertEquals(Constants.CONTENT_CATEGORY_TEXT, records.get(0).category.intValue());
        Assert.assertEquals(Constants.CONCURRENT_LEVEL_HOST, records.get(0).concurrentLevel.intValue());
        multiQueue.removeFromJobRecordMap(records.get(0));
        Assert.assertFalse(multiQueue.isEmptyJobRecordMap(records.get(0).jobId));

        Assert.assertEquals("abc", records.get(1).jobId);
        Assert.assertEquals("http://www.sina.com.cn", records.get(1).url);
        Assert.assertEquals(Constants.PRIORITY_LOW, records.get(1).priority.intValue());
        Assert.assertEquals(Constants.CONTENT_CATEGORY_TEXT, records.get(1).category.intValue());
        Assert.assertEquals(Constants.CONCURRENT_LEVEL_HOST, records.get(1).concurrentLevel.intValue());
        multiQueue.removeFromJobRecordMap(records.get(1));
        Assert.assertTrue(multiQueue.isEmptyJobRecordMap(records.get(1).jobId));
        multiQueue.deleteJobRecordMap(records.get(1).jobId);

        records = multiQueue.pop("sohu.com", 2);
        Assert.assertEquals(1, records.size());
        Assert.assertEquals("def", records.get(0).jobId);
        Assert.assertEquals("http://www.sohu.com/index.html", records.get(0).url);
        Assert.assertEquals(Constants.PRIORITY_HIGH, records.get(0).priority.intValue());
        Assert.assertEquals(Constants.CONTENT_CATEGORY_TEXT, records.get(0).category.intValue());
        Assert.assertEquals(Constants.CONCURRENT_LEVEL_DOMAIN, records.get(0).concurrentLevel.intValue());
        multiQueue.removeFromJobRecordMap(records.get(0));
        Assert.assertTrue(multiQueue.isEmptyJobRecordMap(records.get(0).jobId));
        multiQueue.deleteJobRecordMap(records.get(0).jobId);

        multiQueue.removeConcurrentUnit("www.sina.com.cn");
        multiQueue.removeConcurrentUnit("sohu.com");
    }
}
