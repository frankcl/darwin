package xin.manong.darwin.service.impl;

import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.URLService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-03-15 15:18:57
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class URLServiceImplTest {

    @Resource
    protected URLService urlService;

    @SuppressWarnings("unchecked")
    @Test
    @Transactional
    @Rollback
    public void testURLOperations() {
        List<String> list = new ArrayList<>();
        list.add("abc");
        list.add("xyz");
        URLRecord record = new URLRecord("http://www.sina.com.cn/");
        record.jobId = "test_job_id";
        record.planId = "test_plan_id";
        record.appId = 1;
        record.status = Constants.URL_STATUS_QUEUING;
        record.contentType = Constants.CONTENT_TYPE_PAGE;
        record.fetchTime = System.currentTimeMillis();
        record.customMap = new HashMap<>();
        record.customMap.put("k1", "v1");
        record.customMap.put("k2", 123L);
        record.customMap.put("k3", list);
        Assert.assertTrue(record.check());
        Assert.assertTrue(urlService.add(record));

        URLRecord recordInDB = urlService.get(record.key);
        Assert.assertNotNull(recordInDB);
        Assert.assertEquals(record.key, recordInDB.key);
        Assert.assertEquals(record.url, recordInDB.url);
        Assert.assertEquals(record.hash, recordInDB.hash);
        Assert.assertEquals("test_job_id", recordInDB.jobId);
        Assert.assertEquals("test_plan_id", recordInDB.planId);
        Assert.assertEquals(1, recordInDB.getAppId().intValue());
        Assert.assertEquals(Constants.URL_STATUS_QUEUING, recordInDB.status.intValue());
        Assert.assertEquals(Constants.CONTENT_TYPE_PAGE, recordInDB.contentType.intValue());
        Assert.assertEquals(Constants.PRIORITY_NORMAL, recordInDB.priority.intValue());
        Assert.assertEquals(record.createTime.longValue(), recordInDB.createTime.longValue());
        Assert.assertEquals(3, recordInDB.customMap.size());
        Assert.assertTrue(recordInDB.customMap.containsKey("k1"));
        Assert.assertTrue(recordInDB.customMap.containsKey("k2"));
        Assert.assertTrue(recordInDB.customMap.containsKey("k3"));
        Assert.assertEquals("v1", recordInDB.customMap.get("k1"));
        Assert.assertEquals(123, (int) recordInDB.customMap.get("k2"));
        List<String> l = (List<String>) recordInDB.customMap.get("k3");
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("abc", l.get(0));
        Assert.assertEquals("xyz", l.get(1));

        Assert.assertTrue(urlService.updateStatus(record.key, Constants.URL_STATUS_FETCH_SUCCESS));

        URLRecord updateRecord = new URLRecord();
        updateRecord.status = null;
        updateRecord.createTime = null;
        updateRecord.pushTime = 123L;
        updateRecord.popTime = 1123L;
        updateRecord.key = record.key;
        Assert.assertTrue(urlService.updateQueueTime(updateRecord));

        URLRecord fetchRecord = new URLRecord();
        fetchRecord.key = record.key;
        fetchRecord.status = null;
        fetchRecord.parentURL = "http://www.sohu.com";
        fetchRecord.fetchContentURL = "http://www.sohu.com/123.html";
        fetchRecord.fieldMap.put("AAA", 123);
        Assert.assertTrue(urlService.updateContent(fetchRecord));

        recordInDB = urlService.get(record.key);
        Assert.assertEquals(Constants.URL_STATUS_FETCH_SUCCESS, recordInDB.status.intValue());
        Assert.assertEquals(123L, recordInDB.pushTime.longValue());
        Assert.assertEquals(1123L, recordInDB.popTime.longValue());
        Assert.assertEquals("http://www.sohu.com", recordInDB.parentURL);
        Assert.assertEquals("http://www.sohu.com/123.html", recordInDB.fetchContentURL);
        Assert.assertEquals(1, recordInDB.fieldMap.size());
        Assert.assertTrue(recordInDB.fieldMap.containsKey("AAA"));
        Assert.assertEquals(123, (int) recordInDB.fieldMap.get("AAA"));

        Assert.assertTrue(urlService.delete(record.key));
    }
}
