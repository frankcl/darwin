package xin.manong.darwin.common.util;

import org.junit.Assert;
import org.junit.Test;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.base.common.Context;

/**
 * @author frankcl
 * @date 2023-04-04 16:27:16
 */
public class DarwinUtilSuite {

    @Test
    public void testPutRecordContext() {
        Context context = new Context();
        URLRecord record = new URLRecord("http://www.sina.com.cn");
        record.parentURL = "http://www.people.com.cn";
        record.appId = 1;
        record.jobId = "xxx";
        record.category = Constants.CONTENT_CATEGORY_LIST;
        record.concurrentLevel = Constants.CONCURRENT_LEVEL_HOST;
        record.priority = Constants.PRIORITY_HIGH;
        DarwinUtil.putContext(context, record);

        Assert.assertTrue(context.contains(Constants.JOB_ID));
        Assert.assertTrue(context.contains(Constants.APP_ID));
        Assert.assertTrue(context.contains(Constants.KEY));
        Assert.assertTrue(context.contains(Constants.HASH));
        Assert.assertTrue(context.contains(Constants.URL));
        Assert.assertTrue(context.contains(Constants.PARENT_URL));
        Assert.assertTrue(context.contains(Constants.STATUS));
        Assert.assertTrue(context.contains(Constants.CATEGORY));
        Assert.assertTrue(context.contains(Constants.CONCURRENT_LEVEL));
        Assert.assertTrue(context.contains(Constants.DARWIN_RECORD_TYPE));

        Assert.assertEquals(Constants.RECORD_TYPE_URL, context.get(Constants.DARWIN_RECORD_TYPE));
        Assert.assertEquals(record.url, context.get(Constants.URL));
        Assert.assertEquals(record.key, context.get(Constants.KEY));
        Assert.assertEquals(record.hash, context.get(Constants.HASH));
        Assert.assertEquals(record.parentURL, context.get(Constants.PARENT_URL));
        Assert.assertEquals("xxx", context.get(Constants.JOB_ID));
        Assert.assertEquals(Constants.SUPPORT_URL_STATUSES.get(Constants.URL_STATUS_CREATED), context.get(Constants.STATUS));
        Assert.assertEquals(Constants.SUPPORT_CONTENT_CATEGORIES.get(Constants.CONTENT_CATEGORY_LIST), context.get(Constants.CATEGORY));
        Assert.assertEquals(Constants.SUPPORT_CONCURRENT_LEVELS.get(Constants.CONCURRENT_LEVEL_HOST), context.get(Constants.CONCURRENT_LEVEL));
        Assert.assertEquals(1, (int) context.get(Constants.APP_ID));
        Assert.assertEquals(Constants.PRIORITY_HIGH, (int) context.get(Constants.PRIORITY));
    }

    @Test
    public void testPutJobContext() {
        Context context = new Context();
        Job job = new Job();
        job.appId = 1;
        job.jobId = "xxx";
        job.planId = "zzz";
        job.name = "test job";
        job.status = Constants.JOB_STATUS_RUNNING;
        job.priority = Constants.PRIORITY_HIGH;
        DarwinUtil.putContext(context, job);

        Assert.assertTrue(context.contains(Constants.JOB_ID));
        Assert.assertTrue(context.contains(Constants.APP_ID));
        Assert.assertTrue(context.contains(Constants.PLAN_ID));
        Assert.assertTrue(context.contains(Constants.NAME));
        Assert.assertTrue(context.contains(Constants.STATUS));
        Assert.assertTrue(context.contains(Constants.PRIORITY));
        Assert.assertTrue(context.contains(Constants.DARWIN_RECORD_TYPE));

        Assert.assertEquals(Constants.RECORD_TYPE_JOB, context.get(Constants.DARWIN_RECORD_TYPE));
        Assert.assertEquals("xxx", context.get(Constants.JOB_ID));
        Assert.assertEquals("zzz", context.get(Constants.PLAN_ID));
        Assert.assertEquals("test job", context.get(Constants.NAME));
        Assert.assertEquals(Constants.SUPPORT_JOB_STATUSES.get(Constants.JOB_STATUS_RUNNING), context.get(Constants.STATUS));
        Assert.assertEquals(1, (int) context.get(Constants.APP_ID));
        Assert.assertEquals(Constants.PRIORITY_HIGH, (int) context.get(Constants.PRIORITY));
    }

    @Test
    public void testPutPlanContext() {
        Context context = new Context();
        Plan plan = new Plan();
        plan.planId = "xxx";
        plan.name = "test plan";
        plan.status = Constants.PLAN_STATUS_RUNNING;
        plan.appId = 1;
        plan.priority = Constants.PRIORITY_HIGH;
        plan.category = Constants.PLAN_CATEGORY_REPEAT;
        plan.avoidRepeatedFetch = true;
        plan.crontabExpression = "0 0 6-23/1 * * ?";
        DarwinUtil.putContext(context, plan);

        Assert.assertTrue(context.contains(Constants.APP_ID));
        Assert.assertTrue(context.contains(Constants.PLAN_ID));
        Assert.assertTrue(context.contains(Constants.NAME));
        Assert.assertTrue(context.contains(Constants.STATUS));
        Assert.assertTrue(context.contains(Constants.PRIORITY));
        Assert.assertTrue(context.contains(Constants.CATEGORY));
        Assert.assertTrue(context.contains(Constants.AVOID_REPEATED_FETCH));
        Assert.assertTrue(context.contains(Constants.CRONTAB_EXPRESSION));
        Assert.assertTrue(context.contains(Constants.DARWIN_RECORD_TYPE));

        Assert.assertEquals(Constants.RECORD_TYPE_PLAN, context.get(Constants.DARWIN_RECORD_TYPE));
        Assert.assertEquals("xxx", context.get(Constants.PLAN_ID));
        Assert.assertEquals("test plan", context.get(Constants.NAME));
        Assert.assertEquals(true, context.get(Constants.AVOID_REPEATED_FETCH));
        Assert.assertEquals(Constants.SUPPORT_PLAN_STATUSES.get(Constants.PLAN_STATUS_RUNNING), context.get(Constants.STATUS));
        Assert.assertEquals(Constants.SUPPORT_PLAN_CATEGORIES.get(Constants.PLAN_CATEGORY_REPEAT), context.get(Constants.CATEGORY));
        Assert.assertEquals(1, (int) context.get(Constants.APP_ID));
        Assert.assertEquals(Constants.PRIORITY_HIGH, (int) context.get(Constants.PRIORITY));
        Assert.assertEquals("0 0 6-23/1 * * ?", context.get(Constants.CRONTAB_EXPRESSION));
    }
}
