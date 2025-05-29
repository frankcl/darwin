package xin.manong.darwin.common.model;

import org.junit.Assert;
import org.junit.Test;

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
        job.appId = 1;
        Assert.assertTrue(job.check());
    }

    @Test
    public void testCheckError() {
        Job job = new Job();
        job.jobId = "xxx";
        job.planId = "zzz";
        job.name = "test";
        job.status = true;
        Assert.assertFalse(job.check());
    }
}
