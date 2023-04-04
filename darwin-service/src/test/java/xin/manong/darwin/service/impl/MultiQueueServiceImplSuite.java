package xin.manong.darwin.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.computer.ConcurrentUnitComputer;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.MultiQueueService;
import xin.manong.darwin.service.iface.URLService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-04-04 15:00:58
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class MultiQueueServiceImplSuite {

    @Resource
    protected MultiQueue multiQueue;
    @Resource
    protected URLService urlService;
    @Resource
    protected MultiQueueService multiQueueService;

    @Test
    @Transactional
    @Rollback
    public void testPushQueue() {
        List<URLRecord> records = new ArrayList<>();

        URLRecord record1 = new URLRecord("http://www.sina.com.cn");
        record1.category = Constants.CONTENT_CATEGORY_LIST;
        record1.appId = 1;
        record1.jobId = "xxx";
        record1.priority = Constants.PRIORITY_HIGH;
        Assert.assertTrue(record1.check());
        Assert.assertTrue(urlService.add(record1));
        records.add(record1);

        URLRecord record2 = new URLRecord("http://www.sohu.com");
        record2.category = Constants.CONTENT_CATEGORY_LIST;
        record2.appId = 1;
        record2.jobId = "xxx";
        record2.priority = Constants.PRIORITY_NORMAL;
        Assert.assertTrue(record2.check());
        Assert.assertTrue(urlService.add(record2));
        records.add(record2);

        records = multiQueueService.pushQueue(records);
        Assert.assertEquals(2, records.size());
        Assert.assertEquals(Constants.URL_STATUS_QUEUING, records.get(0).status.intValue());
        Assert.assertEquals(Constants.URL_STATUS_QUEUING, records.get(1).status.intValue());

        URLRecord getRecord1 = urlService.get(record1.key);
        Assert.assertTrue(getRecord1 != null);
        Assert.assertTrue(getRecord1.inQueueTime != null);
        Assert.assertEquals(Constants.URL_STATUS_QUEUING, getRecord1.status.intValue());

        URLRecord getRecord2 = urlService.get(record2.key);
        Assert.assertTrue(getRecord2 != null);
        Assert.assertTrue(getRecord2.inQueueTime != null);
        Assert.assertEquals(Constants.URL_STATUS_QUEUING, getRecord2.status.intValue());

        {
            String concurrentUnit = ConcurrentUnitComputer.compute(record1);
            List<URLRecord> queueRecords = multiQueue.pop(concurrentUnit, 10);
            Assert.assertEquals(1, queueRecords.size());
            Assert.assertEquals("http://www.sina.com.cn", queueRecords.get(0).url);
            multiQueue.removeFromJobMap(record1);
            multiQueue.concurrentUnitsInQueue().remove(concurrentUnit);
            multiQueue.jobsInQueue().remove(record1.jobId);
        }
        {
            String concurrentUnit = ConcurrentUnitComputer.compute(record2);
            List<URLRecord> queueRecords = multiQueue.pop(concurrentUnit, 10);
            Assert.assertEquals(1, queueRecords.size());
            Assert.assertEquals("http://www.sohu.com", queueRecords.get(0).url);
            multiQueue.removeFromJobMap(record2);
            multiQueue.concurrentUnitsInQueue().remove(concurrentUnit);
            multiQueue.jobsInQueue().remove(record2.jobId);
        }
    }
}
