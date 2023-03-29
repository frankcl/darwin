package xin.manong.darwin.spider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.computer.ConcurrentUnitComputer;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.parse.service.ParseService;
import xin.manong.darwin.queue.concurrent.ConcurrentManager;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.iface.*;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.weapon.aliyun.ons.ONSProducer;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.http.HttpClient;
import xin.manong.weapon.base.http.HttpClientConfig;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestMethod;
import xin.manong.weapon.base.log.JSONLogger;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 爬虫抽象实现
 *
 * @author frankcl
 * @date 2023-03-24 16:18:17
 */
public abstract class Spider {

    private static final Logger logger = LoggerFactory.getLogger(Spider.class);

    private Long expiredTimeMs = 86400 * 1000L;
    protected String category;
    @Resource
    protected SpiderConfig config;
    @Resource(name = "spiderAspectLogger")
    protected JSONLogger aspectLogger;
    @Resource
    protected OSSClient ossClient;
    @Resource
    protected URLService urlService;
    @Resource
    protected JobService jobService;
    @Resource
    protected RuleService ruleService;
    @Resource
    protected ParseService parseService;
    @Resource
    protected MultiQueue multiQueue;
    @Resource
    protected MultiQueueService multiQueueService;
    @Resource
    protected ConcurrentManager concurrentManager;
    @Resource
    protected ONSProducer producer;
    protected HttpClient httpClient;

    public Spider(String category) {
        this.category = category;
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        httpClientConfig.connectTimeoutSeconds = 5;
        httpClientConfig.readTimeoutSeconds = 10;
        httpClientConfig.keepAliveMinutes = 3;
        httpClientConfig.maxIdleConnections = 100;
        httpClientConfig.retryCnt = 3;
        httpClient = new HttpClient(httpClientConfig);
    }

    /**
     * 写入抓取内容到OSS
     *
     * @param record URL记录
     * @param bytes 内容字节数组
     * @return 成功返回true，否则返回false
     */
    protected boolean writeContent(URLRecord record, byte[] bytes) {
        String key = String.format("%s/%s/%s", config.contentDirectory, category, record.hash);
        if (!ossClient.putObject(config.contentBucket, key, bytes)) return false;
        OSSMeta ossMeta = new OSSMeta();
        ossMeta.region = config.contentRegion;
        ossMeta.bucket = config.contentBucket;
        ossMeta.key = key;
        record.fetchContentURL = OSSClient.buildURL(ossMeta);
        return true;
    }

    /**
     * 写入抓取内容到OSS
     *
     * @param record URL记录
     * @param inputStream 内容字节流
     * @return 成功返回true，否则返回false
     */
    protected boolean writeContent(URLRecord record, InputStream inputStream) {
        String key = String.format("%s/%s/%s", config.contentDirectory, category, record.hash);
        if (!ossClient.putObject(config.contentBucket, key, inputStream)) return false;
        OSSMeta ossMeta = new OSSMeta();
        ossMeta.region = config.contentRegion;
        ossMeta.bucket = config.contentBucket;
        ossMeta.key = key;
        record.fetchContentURL = OSSClient.buildURL(ossMeta);
        return true;
    }

    /**
     * 抓取URL
     *
     * @param record URL记录
     * @param context 上下文
     * @return 抓取响应，失败返回null
     */
    protected Response fetch(URLRecord record, Context context) {
        try {
            HttpRequest httpRequest = new HttpRequest.Builder().requestURL(record.url).method(RequestMethod.GET).build();
            if (record.headers != null && !record.headers.isEmpty()) httpRequest.headers = record.headers;
            Response httpResponse = httpClient.execute(httpRequest);
            if (httpResponse != null) context.put(Constants.HTTP_CODE, httpResponse.code());
            if (httpResponse == null || !httpResponse.isSuccessful()) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取失败");
                logger.error("execute http request failed for url[{}]", record.url);
            }
            return httpResponse;
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("fetch content error for url[{}]", record.url);
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            record.fetchTime = System.currentTimeMillis();
        }
    }

    /**
     * 根据URL记录获取匹配规则
     *
     * @param record URL记录
     * @param context 上下文
     * @return 匹配规则，无匹配返回null
     */
    protected Rule getMatchRule(URLRecord record, Context context) {
        Job job = jobService.getCache(record.jobId);
        if (job == null) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "爬虫任务不存在");
            logger.error("job[{}] is not found for url[{}]", record.jobId, record.url);
            return null;
        }
        List<Rule> rules = new ArrayList<>();
        for (Integer ruleId : job.ruleIds) {
            Rule rule = ruleService.getCache(ruleId.longValue());
            if (rule == null || !ruleService.match(record, rule)) continue;
            rules.add(rule);
        }
        if (rules.size() != 1) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, String.format("匹配规则数量[%d]不符合预期", rules.size()));
            logger.error("match rule num[{}] is unexpected", rules.size());
            return null;
        }
        return rules.get(0);
    }

    /**
     * 使用重复抓取结果：满足以下条件使用之前抓取结果
     * 1. 任务设置避免重复抓取
     * 2. URL在一天内抓取过
     * 3. OSS数据存在
     *
     * @param record URL记录
     * @param context 上下文
     */
    private void useRepeatedFetchContent(URLRecord record, Context context) {
        Job job = jobService.getCache(record.jobId);
        if (job == null || !job.avoidRepeatedFetch) return;
        context.put(Constants.AVOID_REPEATED_FETCH, true);
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.status = Constants.URL_STATUS_SUCCESS;
        searchRequest.url = record.url;
        searchRequest.fetchTime = new RangeValue<>();
        searchRequest.fetchTime.start = System.currentTimeMillis() - expiredTimeMs;
        searchRequest.fetchTime.includeLower = true;
        Pager<URLRecord> pager = urlService.search(searchRequest, 1, 1);
        if (pager == null || pager.records == null || pager.records.isEmpty()) return;
        URLRecord prevRecord = pager.records.get(0);
        if (StringUtils.isEmpty(prevRecord.fetchContentURL)) return;
        OSSMeta ossMeta = OSSClient.parseURL(prevRecord.fetchContentURL);
        if (!ossClient.exist(ossMeta.bucket, ossMeta.key)) return;
        InputStream inputStream = ossClient.getObjectStream(ossMeta.bucket, ossMeta.key);
        if (inputStream != null) context.put(Constants.DARWIN_INPUT_STREAM, inputStream);
    }

    /**
     * 处理结束任务
     *
     * @param jobId 任务ID
     * @param appId 应用ID
     */
    private void processFinishJob(String jobId, int appId) {
        Context context = new Context();
        Job updateJob = new Job();
        try {
            updateJob.avoidRepeatedFetch = null;
            updateJob.jobId = jobId;
            updateJob.status = Constants.JOB_STATUS_FINISHED;
            if (!jobService.update(updateJob)) logger.warn("update finish status failed for job[{}]", updateJob.jobId);
            Job job = jobService.getCache(jobId);
            updateJob.planId = job.planId;
            updateJob.name = job.name;
            String jobString = JSON.toJSONString(updateJob, SerializerFeature.DisableCircularReferenceDetect);
            Message message = new Message(config.jobTopic, String.format("%d", appId), updateJob.jobId,
                    jobString.getBytes(Charset.forName("UTF-8")));
            SendResult sendResult = producer.send(message);
            if (sendResult == null || StringUtils.isEmpty(sendResult.getMessageId())) {
                logger.warn("push finish message failed for job[{}]", jobId);
                return;
            }
            context.put(Constants.DARWIN_MESSAGE_ID, sendResult.getMessageId());
            context.put(Constants.DARWIN_MESSAGE_KEY, updateJob.jobId);
        } catch (Exception e) {
            logger.error("process finished job[{}] failed", jobId);
            logger.error(e.getMessage(), e);
        } finally {
            context.put(Constants.APP_ID, appId);
            DarwinUtil.putContext(context, updateJob);
            if (aspectLogger != null) aspectLogger.commit(context.getFeatureMap());
        }
    }

    /**
     * 处理结束URL记录
     *
     * @param record URL记录
     * @param context 上下文
     */
    private void processFinishRecord(URLRecord record, Context context) {
        try {
            String recordString = JSON.toJSONString(record, SerializerFeature.DisableCircularReferenceDetect);
            Message message = new Message(config.urlTopic, String.format("%d", record.appId), record.key,
                    recordString.getBytes(Charset.forName("UTF-8")));
            SendResult sendResult = producer.send(message);
            if (sendResult == null || StringUtils.isEmpty(sendResult.getMessageId())) {
                logger.warn("push record finish message failed");
                return;
            }
            context.put(Constants.DARWIN_MESSAGE_ID, sendResult.getMessageId());
            context.put(Constants.DARWIN_MESSAGE_KEY, record.key);
        } catch (Exception e) {
            logger.error("push record finish message failed");
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 处理抓取数据
     *
     * @param record URL记录
     * @param context 上下文
     */
    public void process(URLRecord record, Context context) {
        Long startTime = System.currentTimeMillis();
        try {
            useRepeatedFetchContent(record, context);
            handle(record, context);
        } catch (Throwable t) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取处理数据异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(t));
            logger.error("process record error for url[{}]", record.url);
            logger.error(t.getMessage(), t);
        } finally {
            context.remove(Constants.DARWIN_INPUT_STREAM);
            if (!urlService.updateWithFetchRecord(record)) {
                logger.warn("update fetch content failed for url[{}]", record.url);
            }
            multiQueue.removeFromJobMap(record);
            String concurrentUnit = ConcurrentUnitComputer.compute(record);
            concurrentManager.decreaseConnections(concurrentUnit, 1);
            concurrentManager.removeConnectionRecord(concurrentUnit, record.key);
            processFinishRecord(record, context);
            if (multiQueue.isEmptyJobMap(record.jobId)) processFinishJob(record.jobId, record.appId);
            DarwinUtil.putContext(context, record);
            context.put(Constants.DARWIN_PROCESS_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 爬取并处理数据
     *
     * @param record 数据
     * @param context 上下文
     */
    protected abstract void handle(URLRecord record, Context context) throws Exception;
}
