package xin.manong.darwin.spider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.computer.ConcurrentUnitComputer;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.queue.concurrent.ConcurrentManager;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.URLService;
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
import xin.manong.weapon.base.util.CommonUtil;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * 爬虫抽象实现
 *
 * @author frankcl
 * @date 2023-03-24 16:18:17
 */
public abstract class Spider {

    private static final Logger logger = LoggerFactory.getLogger(Spider.class);

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
    protected MultiQueue multiQueue;
    @Resource
    protected ConcurrentManager concurrentManager;
    @Resource
    protected ONSProducer producer;
    protected HttpClient httpClient;

    public Spider(String category) {
        this.category = category;
    }

    /**
     * 写入抓取内容到OSS
     *
     * @param record URL记录
     * @param bytes 内容字节数组
     * @param context 上下文
     * @return 成功返回true，否则返回false
     */
    protected boolean writeContent(URLRecord record, byte[] bytes, Context context) {
        String key = String.format("%s/%s/%s", config.contentDirectory, category, record.key);
        String suffix = (String) context.get(Constants.RESOURCE_SUFFIX);
        if (!StringUtils.isEmpty(suffix)) key = String.format("%s.%s", key, suffix);
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
     * @param context 上下文
     * @return 成功返回true，否则返回false
     */
    protected boolean writeContent(URLRecord record, InputStream inputStream, Context context) {
        String key = String.format("%s/%s/%s", config.contentDirectory, category, record.key);
        String suffix = (String) context.get(Constants.RESOURCE_SUFFIX);
        if (!StringUtils.isEmpty(suffix)) key = String.format("%s.%s", key, suffix);
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
            buildHttpClient();
            HttpRequest httpRequest = new HttpRequest.Builder().requestURL(record.url).method(RequestMethod.GET).build();
            if (!StringUtils.isEmpty(config.userAgent)) httpRequest.headers.put("User-Agent", config.userAgent);
            if (!StringUtils.isEmpty(record.parentURL)) httpRequest.headers.put("Referer", record.parentURL);
            String host = CommonUtil.getHost(record.url);
            if (!StringUtils.isEmpty(host) && !CommonUtil.isIP(host)) httpRequest.headers.put("Host", host);
            if (record.headers != null && !record.headers.isEmpty()) httpRequest.headers.putAll(record.headers);
            Response httpResponse = httpClient.execute(httpRequest);
            if (httpResponse != null) context.put(Constants.HTTP_CODE, httpResponse.code());
            if (httpResponse == null || !httpResponse.isSuccessful()) {
                if (httpResponse != null) httpResponse.close();
                record.status = Constants.URL_STATUS_FAIL;
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取失败");
                logger.error("execute http request failed for url[{}]", record.url);
                return null;
            }
            String targetURL = httpResponse.request().url().url().toString();
            if (!StringUtils.isEmpty(targetURL) && !targetURL.equals(record.url)) record.redirectURL = targetURL;
            return httpResponse;
        } catch (Exception e) {
            record.status = Constants.URL_STATUS_FAIL;
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
     * 构建HTTPClient
     */
    protected void buildHttpClient() {
        if (httpClient != null) return;
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        httpClientConfig.connectTimeoutSeconds = config.connectTimeoutSeconds;
        httpClientConfig.readTimeoutSeconds = config.readTimeoutSeconds;
        httpClientConfig.keepAliveMinutes = config.keepAliveMinutes;
        httpClientConfig.maxIdleConnections = config.maxIdleConnections;
        httpClientConfig.retryCnt = config.retryCnt;
        httpClient = new HttpClient(httpClientConfig);
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
    private void reuseFetchContent(URLRecord record, Context context) {
        Job job = jobService.getCache(record.jobId);
        if (job == null || !job.avoidRepeatedFetch) return;
        context.put(Constants.AVOID_REPEATED_FETCH, true);
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.status = Constants.URL_STATUS_SUCCESS;
        searchRequest.url = record.url;
        searchRequest.fetchTime = new RangeValue<>();
        searchRequest.fetchTime.start = System.currentTimeMillis() - config.reuseExpiredTimeMs;
        searchRequest.fetchTime.includeLower = true;
        Pager<URLRecord> pager = urlService.search(searchRequest, 1, 1);
        if (pager == null || pager.records == null || pager.records.isEmpty()) return;
        URLRecord prevRecord = pager.records.get(0);
        if (StringUtils.isEmpty(prevRecord.fetchContentURL)) return;
        OSSMeta ossMeta = OSSClient.parseURL(prevRecord.fetchContentURL);
        if (!ossClient.exist(ossMeta.bucket, ossMeta.key)) return;
        InputStream inputStream = ossClient.getObjectStream(ossMeta.bucket, ossMeta.key);
        if (inputStream != null) {
            context.put(Constants.DARWIN_INPUT_STREAM, inputStream);
            int index = ossMeta.key.lastIndexOf(".");
            if (index != -1) context.put(Constants.RESOURCE_SUFFIX, ossMeta.key.substring(index + 1).trim());
        }
    }

    /**
     * 推送结束任务
     *
     * @param record URL记录
     */
    private void pushFinishJob(URLRecord record) {
        Context context = new Context();
        Job updateJob = new Job();
        try {
            updateJob.avoidRepeatedFetch = null;
            updateJob.jobId = record.jobId;
            updateJob.status = Constants.JOB_STATUS_FINISHED;
            if (!jobService.update(updateJob)) logger.warn("update finish status failed for job[{}]", updateJob.jobId);
            Job job = jobService.getCache(record.jobId);
            if (job != null) {
                updateJob.planId = job.planId;
                updateJob.appId = job.appId;
                updateJob.name = job.name;
            }
            String jobString = JSON.toJSONString(updateJob, SerializerFeature.DisableCircularReferenceDetect);
            Message message = new Message(config.jobTopic, String.format("%d", record.appId), updateJob.jobId,
                    jobString.getBytes(Charset.forName("UTF-8")));
            SendResult sendResult = producer.send(message);
            if (sendResult == null || StringUtils.isEmpty(sendResult.getMessageId())) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息失败");
                logger.warn("push finish message failed for job[{}]", record.jobId);
                return;
            }
            context.put(Constants.DARWIN_MESSAGE_ID, sendResult.getMessageId());
            context.put(Constants.DARWIN_MESSAGE_KEY, updateJob.jobId);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("process finished job[{}] failed", record.jobId);
            logger.error(e.getMessage(), e);
        } finally {
            DarwinUtil.putContext(context, updateJob);
            if (aspectLogger != null) aspectLogger.commit(context.getFeatureMap());
        }
    }

    /**
     * 推送结束抓取记录
     *
     * @param record URL记录
     * @param context 上下文
     */
    private void pushFinishRecord(URLRecord record, Context context) {
        try {
            String recordString = JSON.toJSONString(record, SerializerFeature.DisableCircularReferenceDetect);
            Message message = new Message(config.recordTopic, String.format("%d", record.appId), record.key,
                    recordString.getBytes(Charset.forName("UTF-8")));
            SendResult sendResult = producer.send(message);
            if (sendResult == null || StringUtils.isEmpty(sendResult.getMessageId())) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息失败");
                logger.warn("push record finish message failed for key[{}]", record.key);
                return;
            }
            context.put(Constants.DARWIN_MESSAGE_ID, sendResult.getMessageId());
            context.put(Constants.DARWIN_MESSAGE_KEY, record.key);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("push record finish message failed for key[{}]", record.key);
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 获取资源后缀
     *
     * @param response HTTP响应
     * @return 成功返回资源后缀，否则返回null
     */
    protected String getResourceSuffix(Response response) {
        if (response == null || !response.isSuccessful()) return null;
        ResponseBody responseBody = response.body();
        MediaType mediaType = responseBody.contentType();
        if (mediaType == null || mediaType.type() == null || mediaType.subtype() == null) return null;
        if (Constants.SUPPORT_MIME_TYPES.contains(mediaType.type())) {
            String subType = mediaType.subtype();
            if (StringUtils.isEmpty(subType)) return null;
            String type = mediaType.type();
            if (type.equalsIgnoreCase("text") && !subType.equalsIgnoreCase("html")) return null;
            if (type.equalsIgnoreCase("application") && !subType.equalsIgnoreCase("pdf")) return null;
            return subType.toLowerCase();
        }
        return null;
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
            reuseFetchContent(record, context);
            handle(record, context);
        } catch (Throwable t) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取处理数据异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(t));
            logger.error("process record error for url[{}]", record.url);
            logger.error(t.getMessage(), t);
        } finally {
            try {
                context.remove(Constants.RESOURCE_SUFFIX);
                InputStream inputStream = (InputStream) context.get(Constants.DARWIN_INPUT_STREAM);
                if (inputStream != null) {
                    inputStream.close();
                    context.remove(Constants.DARWIN_INPUT_STREAM);
                }
                if (!urlService.updateWithFetchRecord(record)) {
                    logger.warn("update fetch content failed for url[{}]", record.url);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            multiQueue.removeFromJobMap(record);
            String concurrentUnit = ConcurrentUnitComputer.compute(record);
            concurrentManager.decreaseConnections(concurrentUnit, 1);
            concurrentManager.removeConnectionRecord(concurrentUnit, record.key);
            pushFinishRecord(record, context);
            if (multiQueue.isEmptyJobMap(record.jobId)) pushFinishJob(record);
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
