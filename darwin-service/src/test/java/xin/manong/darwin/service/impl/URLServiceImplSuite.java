package xin.manong.darwin.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
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
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(value = { "service", "service-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class URLServiceImplSuite {

    @Resource
    protected URLService urlService;

    @Test
    public void testPlanOperations() {
        List<String> list = new ArrayList<>();
        list.add("abc");
        list.add("xyz");
        URLRecord record = new URLRecord("http://www.sina.com.cn/");
        record.jobId = "test_job_id";
        record.status = Constants.URL_STATUS_CREATED;
        record.category = Constants.CONTENT_CATEGORY_TEXT;
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

        Assert.assertTrue(urlService.delete(record.key));
    }
}
