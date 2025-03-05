package xin.manong.darwin.spider;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
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
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.util.RandomID;

import java.util.ArrayList;

/**
 * @author frankcl
 * @date 2023-03-31 14:36:24
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "dev", "service", "service-dev", "parse", "parse-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class HTMLSpiderTest {

    @Resource
    protected SpiderConfig config;
    @Resource
    protected RuleService ruleService;
    @Resource
    protected JobService jobService;
    @Resource
    protected PlanService planService;
    @Resource
    protected OSSClient ossClient;
    @Resource
    protected HTMLSpider spider;

    private void sweep(Plan plan, Job job) {
        Assert.assertTrue(jobService.delete(job.jobId));
        Assert.assertTrue(planService.delete(plan.planId));
    }

    private Plan preparePlan() {
        Plan plan = new Plan();
        plan.name = "测试计划";
        plan.planId = RandomID.build();
        plan.appId = 0;
        plan.appName = "测试应用";
        plan.category = Constants.PLAN_CATEGORY_PERIOD;
        plan.status = Constants.PLAN_STATUS_RUNNING;
        plan.crontabExpression = "0 0 6-23 * * ?";
        plan.seedURLs = new ArrayList<>();
        plan.seedURLs.add(new URLRecord("http://www.sina.com.cn/"));
        Assert.assertTrue(plan.check());
        Assert.assertTrue(planService.add(plan));
        return plan;
    }

    private Job prepareJobAndHTMLRule(Plan plan) throws Exception {
        String scriptCode = ApplicationTest.readScript("/html_parse_script");
        Rule rule = new Rule();
        rule.domain = "people.com.cn";
        rule.name = "人民网结构化规则";
        rule.regex = "http://politics.people.com.cn/n1/\\d{4}/\\d{4}/c\\d+?-\\d+?\\.html";
        rule.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        rule.script = scriptCode;
        rule.appId = 1;
        rule.planId = plan.planId;
        Assert.assertTrue(ruleService.add(rule));

        Job job = new Job();
        job.jobId = "aaa";
        job.name = "测试任务";
        job.priority = Constants.PRIORITY_NORMAL;
        job.planId = plan.planId;
        job.appId = 1;
        job.status = Constants.JOB_STATUS_RUNNING;
        job.avoidRepeatedFetch = true;
        job.ruleIds = new ArrayList<>();
        job.ruleIds.add(rule.id);
        Assert.assertTrue(jobService.add(job));
        return job;
    }

    private Job prepareJobAndJSONRule(Plan plan) throws Exception {
        String scriptCode = ApplicationTest.readScript("/json_parse_script");
        Rule rule = new Rule();
        rule.domain = "sina.com.cn";
        rule.name = "JSON解析规则";
        rule.regex = "https://www.sina.com.cn/api/hotword.json";
        rule.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        rule.script = scriptCode;
        rule.appId = 1;
        rule.planId = plan.planId;
        Assert.assertTrue(ruleService.add(rule));

        Job job = new Job();
        job.jobId = "aaa";
        job.name = "测试任务";
        job.priority = Constants.PRIORITY_NORMAL;
        job.planId = plan.planId;
        job.appId = 1;
        job.status = Constants.JOB_STATUS_RUNNING;
        job.avoidRepeatedFetch = true;
        job.ruleIds = new ArrayList<>();
        job.ruleIds.add(rule.id);
        Assert.assertTrue(jobService.add(job));
        return job;
    }

    @Test
    @Rollback
    @Transactional
    public void testFetchHTML() throws Exception {
        Plan plan = preparePlan();
        Job job = prepareJobAndHTMLRule(plan);
        try {
            String url = "http://politics.people.com.cn/n1/2023/0406/c1001-32658085.html";
            URLRecord record = new URLRecord(url);
            record.category = Constants.CONTENT_CATEGORY_CONTENT;
            record.fetchMethod = Constants.FETCH_METHOD_LONG_PROXY;
            record.jobId = "aaa";
            record.planId = plan.planId;
            record.appId = 1;
            Context context = new Context();
            spider.process(record, context);
            String key = String.format("%s/%s/%s.html", config.contentDirectory, "html", record.key);
            OSSMeta ossMeta = new OSSMeta();
            ossMeta.region = config.contentRegion;
            ossMeta.bucket = config.contentBucket;
            ossMeta.key = key;
            Assert.assertEquals(Constants.URL_STATUS_SUCCESS, record.status.intValue());
            Assert.assertEquals(OSSClient.buildURL(ossMeta), record.fetchContentURL);
            Assert.assertTrue(record.fetchTime != null && record.fetchTime > 0L);
            Assert.assertTrue(record.fieldMap != null && !record.fieldMap.isEmpty());
            Assert.assertTrue(record.fieldMap.containsKey("title"));
            ossMeta = OSSClient.parseURL(record.fetchContentURL);
            Assert.assertTrue(ossClient.exist(ossMeta.bucket, ossMeta.key));
            ossClient.deleteObject(ossMeta.bucket, ossMeta.key);
        } finally {
            sweep(plan, job);
        }
    }

    @Test
    @Rollback
    @Transactional
    public void testFetchJSON() throws Exception {
        Plan plan = preparePlan();
        Job job = prepareJobAndJSONRule(plan);
        try {
            String url = "https://www.sina.com.cn/api/hotword.json";
            URLRecord record = new URLRecord(url);
            record.category = Constants.CONTENT_CATEGORY_CONTENT;
            record.jobId = "aaa";
            record.planId = plan.planId;
            record.appId = 1;
            Context context = new Context();
            spider.process(record, context);
            String key = String.format("%s/%s/%s.json", config.contentDirectory, "html", record.key);
            OSSMeta ossMeta = new OSSMeta();
            ossMeta.region = config.contentRegion;
            ossMeta.bucket = config.contentBucket;
            ossMeta.key = key;
            Assert.assertEquals(Constants.URL_STATUS_SUCCESS, record.status.intValue());
            Assert.assertEquals(OSSClient.buildURL(ossMeta), record.fetchContentURL);
            Assert.assertTrue(record.fetchTime != null && record.fetchTime > 0L);
            Assert.assertTrue(record.fieldMap != null && !record.fieldMap.isEmpty());
            Assert.assertTrue(record.fieldMap.containsKey("result_size"));
            Assert.assertEquals(10, (int) record.fieldMap.get("result_size"));
            ossMeta = OSSClient.parseURL(record.fetchContentURL);
            Assert.assertTrue(ossClient.exist(ossMeta.bucket, ossMeta.key));
            ossClient.deleteObject(ossMeta.bucket, ossMeta.key);
        } finally {
            sweep(plan, job);
        }
    }

    @Test
    @Rollback
    @Transactional
    public void testFetchFail() throws Exception {
        Plan plan = preparePlan();
        Job job = prepareJobAndHTMLRule(plan);
        try {
            String url = "http://politics.people.com.cn/n1/2023/0406/c1001-32658085111.html";
            URLRecord record = new URLRecord(url);
            record.category = Constants.CONTENT_CATEGORY_CONTENT;
            record.jobId = "aaa";
            record.planId = plan.planId;
            record.appId = 1;
            Context context = new Context();
            spider.process(record, context);
            Assert.assertEquals(Constants.URL_STATUS_FETCH_FAIL, record.status.intValue());
            Assert.assertTrue(record.fetchTime != null && record.fetchTime > 0L);
            Assert.assertTrue(StringUtils.isEmpty(record.fetchContentURL));
        } finally {
            sweep(plan, job);
        }
    }
}
