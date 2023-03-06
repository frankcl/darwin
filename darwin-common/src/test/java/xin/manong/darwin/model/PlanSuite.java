package xin.manong.darwin.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.PlanCategory;
import xin.manong.darwin.common.model.URLRecord;

import java.util.ArrayList;

/**
 * @author frankcl
 * @date 2023-03-06 18:22:07
 */
public class PlanSuite {

    @Test
    public void testBuildJob() {
        Plan plan = new Plan();
        plan.appId = 1;
        plan.appName = "test";
        plan.planId = "test_id";
        plan.name = "test_job";
        plan.category = PlanCategory.ONCE;
        plan.priority = Constants.JOB_PRIORITY_HIGH;
        plan.seedURLs = new ArrayList<>();
        plan.seedURLs.add(new URLRecord("http://www.sina.com.cn/"));
        Assert.assertTrue(plan.check());

        Job job = plan.buildJob();
        Assert.assertFalse(StringUtils.isEmpty(job.jobId));
        Assert.assertFalse(StringUtils.isEmpty(job.name));
        Assert.assertTrue(job.name.startsWith("test_job_"));
        Assert.assertEquals("test_id", job.planId);
        Assert.assertEquals(1, job.seedURLs.size());
        Assert.assertEquals("http://www.sina.com.cn/", job.seedURLs.get(0).url);
        Assert.assertEquals(Constants.JOB_PRIORITY_HIGH, job.seedURLs.get(0).priority.intValue());
    }
}
