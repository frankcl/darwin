package xin.manong.darwin.common.model;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;

import java.util.ArrayList;

/**
 * @author frankcl
 * @date 2023-04-04 16:04:13
 */
public class JobSuite {

    @Test
    public void testCheckOK() {
        Job job = new Job();
        job.jobId = "xxx";
        job.planId = "zzz";
        job.name = "test";
        job.status = Constants.JOB_STATUS_RUNNING;
        job.avoidRepeatedFetch = true;
        job.appId = 1;
        job.priority = Constants.PRIORITY_HIGH;
        job.ruleIds = new ArrayList<>();
        job.ruleIds.add(123);
        job.seedURLs = new ArrayList<>();
        job.seedURLs.add(new URLRecord("http://www.sina.com.cn"));
        Assert.assertTrue(job.check());
    }

    @Test
    public void testCheckError() {
        Job job = new Job();
        job.jobId = "xxx";
        job.planId = "zzz";
        job.name = "test";
        job.status = Constants.JOB_STATUS_RUNNING;
        job.avoidRepeatedFetch = true;
        job.appId = 1;
        job.priority = Constants.PRIORITY_HIGH;
        job.ruleIds = new ArrayList<>();
        job.ruleIds.add(123);
        job.seedURLs = new ArrayList<>();
        Assert.assertFalse(job.check());
    }
}
