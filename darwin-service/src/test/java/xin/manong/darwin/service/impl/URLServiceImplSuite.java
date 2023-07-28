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
import xin.manong.darwin.common.model.FetchRecord;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.URLService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-03-15 15:18:57
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class URLServiceImplSuite {

    @Resource
    protected URLService urlService;
    @Resource
    protected MultiQueue multiQueue;

    @Test
    @Transactional
    @Rollback
    public void testURLOperations() {
        List<String> list = new ArrayList<>();
        list.add("abc");
        list.add("xyz");
        URLRecord record = new URLRecord("http://www.sina.com.cn/");
        record.jobId = "test_job_id";
        record.appId = 1;
        record.status = Constants.URL_STATUS_CREATED;
        record.category = Constants.CONTENT_CATEGORY_TEXT;
        record.fetchTime = System.currentTimeMillis();
        record.userDefinedMap = new HashMap<>();
        record.userDefinedMap.put("k1", "v1");
        record.userDefinedMap.put("k2", 123L);
        record.userDefinedMap.put("k3", list);
        Assert.assertTrue(record.check());
        Assert.assertTrue(urlService.add(record));

        URLRecord recordInDB = urlService.get(record.key);
        Assert.assertTrue(recordInDB != null);
        Assert.assertEquals(record.key, recordInDB.key);
        Assert.assertEquals(record.url, recordInDB.url);
        Assert.assertEquals(record.hash, recordInDB.hash);
        Assert.assertEquals("test_job_id", recordInDB.jobId);
        Assert.assertEquals(1, recordInDB.getAppId().intValue());
        Assert.assertEquals(Constants.URL_STATUS_CREATED, recordInDB.status.intValue());
        Assert.assertEquals(Constants.CONTENT_CATEGORY_TEXT, recordInDB.category.intValue());
        Assert.assertEquals(Constants.PRIORITY_NORMAL, recordInDB.priority.intValue());
        Assert.assertEquals(record.createTime.longValue(), recordInDB.createTime.longValue());
        Assert.assertEquals(3, recordInDB.userDefinedMap.size());
        Assert.assertTrue(recordInDB.userDefinedMap.containsKey("k1"));
        Assert.assertTrue(recordInDB.userDefinedMap.containsKey("k2"));
        Assert.assertTrue(recordInDB.userDefinedMap.containsKey("k3"));
        Assert.assertEquals("v1", recordInDB.userDefinedMap.get("k1"));
        Assert.assertEquals(123, (int) recordInDB.userDefinedMap.get("k2"));
        List<String> l = (List<String>) recordInDB.userDefinedMap.get("k3");
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("abc", l.get(0));
        Assert.assertEquals("xyz", l.get(1));

        Assert.assertTrue(null == urlService.getCache("http://www.unknonwn.com/"));
        Assert.assertTrue(null == urlService.getCache("http://www.sina.com.cn/"));

        Assert.assertTrue(urlService.updateStatus(record.key, Constants.URL_STATUS_SUCCESS));

        URLRecord updateRecord = new URLRecord();
        updateRecord.status = null;
        updateRecord.createTime = null;
        updateRecord.inQueueTime = 123L;
        updateRecord.outQueueTime = 1123L;
        updateRecord.key = record.key;
        Assert.assertTrue(urlService.updateQueueTime(updateRecord));

        FetchRecord fetchRecord = new FetchRecord();
        fetchRecord.key = record.key;
        fetchRecord.status = null;
        fetchRecord.parentURL = "http://www.sohu.com";
        fetchRecord.fetchContentURL = "http://www.sohu.com/123.html";
        Assert.assertTrue(urlService.updateResult(fetchRecord));

        recordInDB = urlService.getCache("http://www.sina.com.cn/");
        Assert.assertTrue(recordInDB != null);
        Assert.assertEquals(record.key, recordInDB.key);
        Assert.assertEquals(record.url, recordInDB.url);
        Assert.assertEquals(record.hash, recordInDB.hash);
        Assert.assertEquals(Constants.URL_STATUS_SUCCESS, recordInDB.status.intValue());
        Assert.assertEquals(123L, recordInDB.inQueueTime.longValue());
        Assert.assertEquals(1123L, recordInDB.outQueueTime.longValue());
        Assert.assertTrue(null == recordInDB.parentURL);
        Assert.assertEquals("http://www.sohu.com/123.html", recordInDB.fetchContentURL);

        Assert.assertTrue(urlService.delete(record.key));
    }
}
