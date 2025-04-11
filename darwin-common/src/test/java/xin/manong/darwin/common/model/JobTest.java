package xin.manong.darwin.common.model;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;

/**
 * @author frankcl
 * @date 2023-04-04 16:04:13
 */
public class JobTest {

    @Test
    public void testCheckOK() {
        Job job = new Job();
        job.jobId = "xxx";
        job.planId = "zzz";
        job.name = "test";
        job.status = true;
        job.allowRepeat = true;
        job.appId = 1;
        job.priority = Constants.PRIORITY_HIGH;
        Assert.assertTrue(job.check());
    }

    @Test
    public void testCheckError() {
        Job job = new Job();
        job.jobId = "xxx";
        job.planId = "zzz";
        job.name = "test";
        job.status = true;
        job.allowRepeat = true;
        job.priority = Constants.PRIORITY_HIGH;
        Assert.assertFalse(job.check());
    }
}
