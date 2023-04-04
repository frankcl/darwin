package xin.manong.darwin.common.model;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;

/**
 * @author frankcl
 * @date 2023-04-04 15:59:11
 */
public class URLRecordSuite {

    @Test
    public void testCheckOK() {
        URLRecord record = new URLRecord("http://www.sina.com.cn");
        record.category = Constants.CONTENT_CATEGORY_LIST;
        record.concurrentLevel = Constants.CONCURRENT_LEVEL_DOMAIN;
        record.priority = Constants.PRIORITY_HIGH;
        record.appId = 1;
        record.jobId = "xxx";
        Assert.assertTrue(record.check());
    }

    @Test
    public void testCheckError() {
        URLRecord record = new URLRecord("http://www.sina.com.cn");
        record.category = Constants.CONTENT_CATEGORY_LIST;
        record.concurrentLevel = Constants.CONCURRENT_LEVEL_DOMAIN;
        record.priority = Constants.PRIORITY_HIGH;
        record.jobId = "xxx";
        Assert.assertFalse(record.check());
    }
}
