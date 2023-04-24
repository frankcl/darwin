package xin.manong.darwin.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.CronExpression;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.ApplicationTest;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.TransactionService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.weapon.base.util.RandomID;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author frankcl
 * @date 2023-04-04 14:31:51
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "service", "service-dev", "queue", "queue-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class TransactionServiceImplSuite {

    @Resource
    protected PlanService planService;
    @Resource
    protected URLService urlService;
    @Resource
    protected TransactionService transactionService;

    @Test
    @Transactional
    @Rollback
    public void testOperations() throws Exception {
        Plan plan = new Plan();
        plan.name = "测试计划";
        plan.planId = RandomID.build();
        plan.category = Constants.PLAN_CATEGORY_REPEAT;
        plan.status = Constants.PLAN_STATUS_RUNNING;
        plan.appId = 1;
        plan.avoidRepeatedFetch = true;
        plan.priority = Constants.PRIORITY_HIGH;
        plan.crontabExpression = "0 0 6-23/1 * * ?";
        plan.ruleIds = new ArrayList<>();
        plan.ruleIds.add(32);
        plan.seedURLs = new ArrayList<>();
        {
            URLRecord seedURL = new URLRecord("http://www.sina.com.cn");
            seedURL.category = Constants.CONTENT_CATEGORY_LIST;
            plan.seedURLs.add(seedURL);
        }
        {
            URLRecord seedURL = new URLRecord("http://www.sina.com.cn/123.html");
            seedURL.category = Constants.CONTENT_CATEGORY_TEXT;
            plan.seedURLs.add(seedURL);
        }
        Assert.assertTrue(planService.add(plan));

        Job job = transactionService.buildJob(plan);
        Assert.assertTrue(job != null);
        Assert.assertTrue(job.jobId != null);
        Assert.assertEquals(plan.planId, job.planId);
        Assert.assertEquals(plan.appId.intValue(), job.appId.intValue());
        Assert.assertTrue(job.name.startsWith(plan.name));
        Assert.assertEquals(Constants.PRIORITY_HIGH, job.priority.intValue());
        Assert.assertEquals(Constants.JOB_STATUS_RUNNING, job.status.intValue());
        Assert.assertTrue(job.avoidRepeatedFetch);
        Assert.assertEquals(1, job.ruleIds.size());
        Assert.assertEquals(32, job.ruleIds.get(0).intValue());

        Assert.assertEquals(2, job.seedURLs.size());
        Assert.assertEquals("http://www.sina.com.cn", job.seedURLs.get(0).url);
        Assert.assertEquals(Constants.CONTENT_CATEGORY_LIST, job.seedURLs.get(0).category.intValue());
        Assert.assertEquals(plan.appId.intValue(), job.seedURLs.get(0).appId.intValue());
        Assert.assertEquals(job.jobId, job.seedURLs.get(0).jobId);
        Assert.assertEquals(Constants.PRIORITY_HIGH, job.seedURLs.get(0).priority.intValue());
        Assert.assertEquals(Constants.CONCURRENT_LEVEL_DOMAIN, job.seedURLs.get(0).concurrentLevel.intValue());
        Assert.assertEquals(0, job.seedURLs.get(0).depth.intValue());
        Assert.assertEquals(Constants.URL_STATUS_CREATED, job.seedURLs.get(0).status.intValue());
        Assert.assertEquals(DigestUtils.md5Hex("http://www.sina.com.cn"), job.seedURLs.get(0).hash);
        Assert.assertTrue(job.seedURLs.get(0).key != null);

        Assert.assertEquals("http://www.sina.com.cn/123.html", job.seedURLs.get(1).url);
        Assert.assertEquals(Constants.CONTENT_CATEGORY_TEXT, job.seedURLs.get(1).category.intValue());
        Assert.assertEquals(plan.appId.intValue(), job.seedURLs.get(1).appId.intValue());
        Assert.assertEquals(job.jobId, job.seedURLs.get(1).jobId);
        Assert.assertEquals(Constants.PRIORITY_HIGH, job.seedURLs.get(1).priority.intValue());
        Assert.assertEquals(Constants.CONCURRENT_LEVEL_DOMAIN, job.seedURLs.get(1).concurrentLevel.intValue());
        Assert.assertEquals(0, job.seedURLs.get(1).depth.intValue());
        Assert.assertEquals(Constants.URL_STATUS_CREATED, job.seedURLs.get(1).status.intValue());
        Assert.assertEquals(DigestUtils.md5Hex("http://www.sina.com.cn/123.html"), job.seedURLs.get(1).hash);
        Assert.assertTrue(job.seedURLs.get(1).key != null);

        URLSearchRequest request = new URLSearchRequest();
        request.status = Constants.URL_STATUS_CREATED;
        request.jobId = job.jobId;
        request.current = 1;
        request.size = 10;
        Pager<URLRecord> pager = urlService.search(request);
        Assert.assertEquals(2, pager.total.intValue());
        Assert.assertEquals(2, pager.records.size());

        Plan getPlan = planService.get(plan.planId);
        Assert.assertEquals(new CronExpression(plan.crontabExpression).getNextValidTimeAfter(new Date()).getTime(),
                getPlan.nextTime.longValue());
    }
}
