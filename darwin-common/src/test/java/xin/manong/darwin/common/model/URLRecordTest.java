package xin.manong.darwin.common.model;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;

/**
 * @author frankcl
 * @date 2023-04-04 15:59:11
 */
public class URLRecordTest {

    @Test
    public void testCheckOK() {
        URLRecord record = new URLRecord("http://www.sina.com.cn");
        record.priority = Constants.PRIORITY_HIGH;
        record.appId = 1;
        record.jobId = "xxx";
        record.planId = "xxx";
        Assert.assertTrue(record.check());
    }

    @Test
    public void testCheckError() {
        URLRecord record = new URLRecord("http://www.sina.com.cn");
        record.priority = Constants.PRIORITY_HIGH;
        record.jobId = "xxx";
        Assert.assertFalse(record.check());
    }
}
