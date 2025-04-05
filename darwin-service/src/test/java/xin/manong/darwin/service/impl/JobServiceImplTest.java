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
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.request.JobSearchRequest;
import xin.manong.weapon.base.util.RandomID;

/**
 * @author frankcl
 * @date 2023-03-15 15:18:57
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class JobServiceImplTest {

    @Resource
    protected JobService jobService;

    @Test
    @Transactional
    @Rollback
    public void testJobOperations() {
        URLRecord record = new URLRecord("http://www.sina.com.cn/");
        Job job = new Job();
        job.name = "测试任务";
        job.appId = 1;
        job.jobId = RandomID.build();
        job.planId = RandomID.build();
        job.priority = Constants.PRIORITY_HIGH;
        Assert.assertTrue(job.check());
        Assert.assertTrue(jobService.add(job));

        Job jobInDB = jobService.get(job.jobId);
        Assert.assertNotNull(jobInDB);
        Assert.assertEquals("测试任务", jobInDB.name);
        Assert.assertEquals(1, jobInDB.appId.intValue());
        Assert.assertEquals(job.planId, jobInDB.planId);
        Assert.assertEquals(job.priority.intValue(), jobInDB.priority.intValue());

        Job updateJob = new Job();
        updateJob.jobId = job.jobId;
        updateJob.priority = Constants.PRIORITY_LOW;
        Assert.assertTrue(jobService.update(updateJob));

        jobInDB = jobService.get(job.jobId);
        Assert.assertNotNull(jobInDB);
        Assert.assertEquals("测试任务", jobInDB.name);
        Assert.assertEquals(job.planId, jobInDB.planId);
        Assert.assertEquals(Constants.PRIORITY_LOW, jobInDB.priority.intValue());

        jobInDB = jobService.getCache(job.jobId);
        Assert.assertNotNull(jobInDB);

        JobSearchRequest searchRequest = new JobSearchRequest();
        searchRequest.status = Constants.JOB_STATUS_RUNNING;
        searchRequest.planId = job.planId;
        searchRequest.current = 1;
        searchRequest.size = 10;
        Pager<Job> pager = jobService.search(searchRequest);
        Assert.assertEquals(1L, pager.current.longValue());
        Assert.assertEquals(1L, pager.total.longValue());
        Assert.assertEquals(1, pager.records.size());

        Assert.assertTrue(jobService.delete(job.jobId));
    }
}
