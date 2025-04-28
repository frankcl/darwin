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
import xin.manong.darwin.service.iface.OSSService;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.spider.core.Router;
import xin.manong.darwin.spider.core.SpiderConfig;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.util.RandomID;

/**
 * @author frankcl
 * @date 2023-03-31 14:36:24
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "spider", "spider-dev", "service", "service-dev", "parse", "parse-dev", "queue", "queue-dev", "log", "log-dev" })
@SpringBootTest(classes = { ApplicationTest.class })
public class TextSpiderTest {

    @Resource
    private SpiderConfig spiderConfig;
    @Resource
    private RuleService ruleService;
    @Resource
    private JobService jobService;
    @Resource
    private PlanService planService;
    @Resource
    private OSSService ossService;
    @Resource
    private Router router;

    private void sweep(Plan plan, Job job) {
        Assert.assertTrue(jobService.delete(job.jobId));
        Assert.assertTrue(planService.delete(plan.planId));
        Assert.assertTrue(ruleService.deleteRules(plan.planId));
    }

    private Plan preparePlan() {
        Plan plan = new Plan();
        plan.name = "测试计划";
        plan.planId = RandomID.build();
        plan.appId = 0;
        plan.appName = "测试应用";
        plan.category = Constants.PLAN_CATEGORY_PERIOD;
        plan.status = true;
        plan.crontabExpression = "0 0 6-23 * * ?";
        Assert.assertTrue(plan.check());
        Assert.assertTrue(planService.add(plan));
        return plan;
    }

    private void prepareHTMLRule(Plan plan) throws Exception {
        String scriptCode = ApplicationTest.readScript("/html_parse_script");
        Rule rule = new Rule();
        rule.name = "人民网结构化规则";
        rule.regex = "http://politics.people.com.cn/n1/\\d{4}/\\d{4}/c\\d+?-\\d+?\\.html";
        rule.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        rule.script = scriptCode;
        rule.planId = plan.planId;
        Assert.assertTrue(ruleService.add(rule));
    }

    private void prepareJSONRule(Plan plan) throws Exception {
        String scriptCode = ApplicationTest.readScript("/json_parse_script");
        Rule rule = new Rule();
        rule.name = "JSON解析规则";
        rule.regex = "https://www.sina.com.cn/api/hotword.json";
        rule.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        rule.script = scriptCode;
        rule.planId = plan.planId;
        Assert.assertTrue(ruleService.add(rule));
    }

    private Job prepareJob(Plan plan) throws Exception {
        Job job = new Job();
        job.jobId = "aaa";
        job.name = "测试任务";
        job.priority = Constants.PRIORITY_NORMAL;
        job.planId = plan.planId;
        job.appId = 1;
        job.status = true;
        job.allowRepeat = false;
        Assert.assertTrue(jobService.add(job));
        return job;
    }

    @Test
    @Rollback
    @Transactional
    public void testFetchHTML() throws Exception {
        Plan plan = preparePlan();
        prepareHTMLRule(plan);
        Job job = prepareJob(plan);
        try {
            String url = "http://politics.people.com.cn/n1/2023/0406/c1001-32658085.html";
            URLRecord record = new URLRecord(url);
            record.fetchMethod = Constants.FETCH_METHOD_LONG_PROXY;
            record.jobId = "aaa";
            record.planId = plan.planId;
            record.appId = 1;
            Context context = new Context();
            router.route(record, context);
            String key = String.format("%s/%s/%s.html", spiderConfig.ossDirectory, "text", record.key);
            Assert.assertEquals(Constants.URL_STATUS_FETCH_SUCCESS, record.status.intValue());
            Assert.assertEquals(Constants.CONTENT_CATEGORY_PAGE, record.category.intValue());
            Assert.assertEquals(ossService.buildURL(key), record.fetchContentURL);
            Assert.assertTrue(record.fetchTime != null && record.fetchTime > 0L);
            Assert.assertTrue(record.fieldMap != null && !record.fieldMap.isEmpty());
            Assert.assertTrue(record.fieldMap.containsKey("title"));
            Assert.assertTrue(ossService.existsByURL(record.fetchContentURL));
            ossService.deleteByURL(record.fetchContentURL);
        } finally {
            sweep(plan, job);
        }
    }

    @Test
    @Rollback
    @Transactional
    public void testFetchJSON() throws Exception {
        Plan plan = preparePlan();
        prepareJSONRule(plan);
        Job job = prepareJob(plan);
        try {
            String url = "https://www.sina.com.cn/api/hotword.json";
            URLRecord record = new URLRecord(url);
            record.jobId = "aaa";
            record.planId = plan.planId;
            record.appId = 1;
            Context context = new Context();
            router.route(record, context);
            String key = String.format("%s/%s/%s.json", spiderConfig.ossDirectory, "text", record.key);
            Assert.assertEquals(Constants.URL_STATUS_FETCH_SUCCESS, record.status.intValue());
            Assert.assertEquals(Constants.CONTENT_CATEGORY_PAGE, record.category.intValue());
            Assert.assertEquals(ossService.buildURL(key), record.fetchContentURL);
            Assert.assertTrue(record.fetchTime != null && record.fetchTime > 0L);
            Assert.assertTrue(record.fieldMap != null && !record.fieldMap.isEmpty());
            Assert.assertTrue(record.fieldMap.containsKey("result_size"));
            Assert.assertEquals(10, (int) record.fieldMap.get("result_size"));
            Assert.assertTrue(ossService.existsByKey(key));
            ossService.deleteByKey(key);
        } finally {
            sweep(plan, job);
        }
    }

    @Test
    @Rollback
    @Transactional
    public void testFetchFail() throws Exception {
        Plan plan = preparePlan();
        prepareHTMLRule(plan);
        Job job = prepareJob(plan);
        try {
            String url = "http://politics.people.com.cn/n1/2023/0406/c1001-32658085111.html";
            URLRecord record = new URLRecord(url);
            record.jobId = "aaa";
            record.planId = plan.planId;
            record.appId = 1;
            Context context = new Context();
            router.route(record, context);
            Assert.assertNull(record.category);
            Assert.assertEquals(Constants.URL_STATUS_FETCH_FAIL, record.status.intValue());
            Assert.assertTrue(record.fetchTime != null && record.fetchTime > 0L);
            Assert.assertTrue(StringUtils.isEmpty(record.fetchContentURL));
        } finally {
            sweep(plan, job);
        }
    }
}
