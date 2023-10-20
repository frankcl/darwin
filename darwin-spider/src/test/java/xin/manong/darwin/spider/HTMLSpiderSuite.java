package xin.manong.darwin.spider;

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
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.common.Context;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * @author frankcl
 * @date 2023-03-31 14:36:24
 */
@ActiveProfiles(value = { "dev", "service", "service-dev", "parse", "parse-dev", "queue", "queue-dev", "log", "log-dev" })
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationTest.class)
public class HTMLSpiderSuite {

    @Resource
    protected SpiderConfig config;
    @Resource
    protected RuleService ruleService;
    @Resource
    protected JobService jobService;
    @Resource
    protected OSSClient ossClient;
    @Resource
    protected HTMLSpider spider;

    private void sweepJobAndRule(Job job) {
        for (Integer ruleId : job.ruleIds) Assert.assertTrue(ruleService.delete(ruleId));
        Assert.assertTrue(jobService.delete(job.jobId));
    }

    private Job prepareJobAndHTMLRule() throws Exception {
        String scriptCode = ApplicationTest.readScript("/html_parse_script");
        Rule rule = new Rule();
        rule.domain = "people.com.cn";
        rule.name = "人民网结构化规则";
        rule.regex = "http://politics.people.com.cn/n1/\\d{4}/\\d{4}/c\\d+?-\\d+?\\.html";
        rule.ruleGroup = 1;
        rule.category = Constants.RULE_CATEGORY_STRUCTURE;
        rule.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        rule.script = scriptCode;
        Assert.assertTrue(ruleService.add(rule));

        Job job = new Job();
        job.jobId = "aaa";
        job.name = "测试任务";
        job.priority = Constants.PRIORITY_NORMAL;
        job.planId = "xxx";
        job.appId = 1;
        job.status = Constants.JOB_STATUS_RUNNING;
        job.avoidRepeatedFetch = true;
        job.ruleIds = new ArrayList<>();
        job.ruleIds.add(rule.id.intValue());
        Assert.assertTrue(jobService.add(job));
        return job;
    }

    private Job prepareJobAndJSONRule() throws Exception {
        String scriptCode = ApplicationTest.readScript("/json_parse_script");
        Rule rule = new Rule();
        rule.domain = "shuwen.com";
        rule.name = "JSON解析规则";
        rule.regex = "http://external-data-service.shuwen.com/report/histogram";
        rule.ruleGroup = 1;
        rule.category = Constants.RULE_CATEGORY_STRUCTURE;
        rule.scriptType = Constants.SCRIPT_TYPE_GROOVY;
        rule.script = scriptCode;
        Assert.assertTrue(ruleService.add(rule));

        Job job = new Job();
        job.jobId = "aaa";
        job.name = "测试任务";
        job.priority = Constants.PRIORITY_NORMAL;
        job.planId = "xxx";
        job.appId = 1;
        job.status = Constants.JOB_STATUS_RUNNING;
        job.avoidRepeatedFetch = true;
        job.ruleIds = new ArrayList<>();
        job.ruleIds.add(rule.id.intValue());
        Assert.assertTrue(jobService.add(job));
        return job;
    }

    @Test
    @Rollback
    @Transactional
    public void testFetchHTML() throws Exception {
        Job job = prepareJobAndHTMLRule();
        try {
            String url = "http://politics.people.com.cn/n1/2023/0406/c1001-32658085.html";
            URLRecord record = new URLRecord(url);
            record.category = Constants.CONTENT_CATEGORY_TEXT;
            record.jobId = "aaa";
            record.planId = "xxx";
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
            sweepJobAndRule(job);
        }
    }

    @Test
    @Rollback
    @Transactional
    public void testFetchJSON() throws Exception {
        Job job = prepareJobAndJSONRule();
        try {
            String url = "http://external-data-service.shuwen.com/report/histogram";
            URLRecord record = new URLRecord(url);
            record.category = Constants.CONTENT_CATEGORY_TEXT;
            record.jobId = "aaa";
            record.planId = "xxx";
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
            Assert.assertEquals(7, (int) record.fieldMap.get("result_size"));
            ossMeta = OSSClient.parseURL(record.fetchContentURL);
            Assert.assertTrue(ossClient.exist(ossMeta.bucket, ossMeta.key));
            ossClient.deleteObject(ossMeta.bucket, ossMeta.key);
        } finally {
            sweepJobAndRule(job);
        }
    }

    @Test
    @Rollback
    @Transactional
    public void testFetchFail() throws Exception {
        Job job = prepareJobAndHTMLRule();
        try {
            String url = "http://politics.people.com.cn/n1/2023/0406/c1001-32658085111.html";
            URLRecord record = new URLRecord(url);
            record.category = Constants.CONTENT_CATEGORY_TEXT;
            record.jobId = "aaa";
            record.planId = "xxx";
            record.appId = 1;
            Context context = new Context();
            spider.process(record, context);
            Assert.assertEquals(Constants.URL_STATUS_FAIL, record.status.intValue());
            Assert.assertTrue(record.fetchTime != null && record.fetchTime > 0L);
            Assert.assertTrue(StringUtils.isEmpty(record.fetchContentURL));
        } finally {
            sweepJobAndRule(job);
        }
    }
}
