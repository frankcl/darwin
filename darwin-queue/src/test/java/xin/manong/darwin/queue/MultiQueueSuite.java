package xin.manong.darwin.queue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLCategory;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.spring.boot.annotation.EnableRedisClient;

import javax.annotation.Resource;
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
        {
            URLRecord record = new URLRecord("http://www.sina.com.cn");
            record.category = URLCategory.TEXT;
            record.jobId = "abc";
            record.priority = Constants.PRIORITY_LOW;
            Assert.assertEquals(MultiQueueStatus.OK, multiQueue.push(record));
        }
        {
            URLRecord record = new URLRecord("http://www.sina.com.cn/index.html");
            record.category = URLCategory.TEXT;
            record.jobId = "abc";
            record.priority = Constants.PRIORITY_HIGH;
            Assert.assertEquals(MultiQueueStatus.OK, multiQueue.push(record));
        }
        {
            URLRecord record = new URLRecord("http://www.sohu.com/index.html");
            record.category = URLCategory.TEXT;
            record.jobId = "def";
            record.priority = Constants.PRIORITY_HIGH;
            Assert.assertEquals(MultiQueueStatus.OK, multiQueue.push(record));
        }
        Assert.assertFalse(multiQueue.refuseService());
        List<URLRecord> records = multiQueue.pop("www.sina.com.cn", 3);
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("abc", records.get(0).jobId);
        Assert.assertEquals("http://www.sina.com.cn/index.html", records.get(0).url);
        Assert.assertEquals(Constants.PRIORITY_HIGH, records.get(0).priority.intValue());
        Assert.assertEquals(URLCategory.TEXT, records.get(0).category);
        multiQueue.removeFromJobMap(records.get(0));
        Assert.assertFalse(multiQueue.isEmptyJobMap(records.get(0).jobId));

        Assert.assertEquals("abc", records.get(1).jobId);
        Assert.assertEquals("http://www.sina.com.cn", records.get(1).url);
        Assert.assertEquals(Constants.PRIORITY_LOW, records.get(1).priority.intValue());
        Assert.assertEquals(URLCategory.TEXT, records.get(1).category);
        multiQueue.removeFromJobMap(records.get(1));
        Assert.assertTrue(multiQueue.isEmptyJobMap(records.get(1).jobId));
        multiQueue.deleteJobMap(records.get(1).jobId);

        records = multiQueue.pop("www.sohu.com", 2);
        Assert.assertEquals(1, records.size());
        Assert.assertEquals("def", records.get(0).jobId);
        Assert.assertEquals("http://www.sohu.com/index.html", records.get(0).url);
        Assert.assertEquals(Constants.PRIORITY_HIGH, records.get(0).priority.intValue());
        Assert.assertEquals(URLCategory.TEXT, records.get(0).category);
        multiQueue.removeFromJobMap(records.get(0));
        Assert.assertTrue(multiQueue.isEmptyJobMap(records.get(0).jobId));
        multiQueue.deleteJobMap(records.get(0).jobId);

        multiQueue.removeHost("www.sina.com.cn");
        multiQueue.removeHost("www.sohu.com");
    }
}
