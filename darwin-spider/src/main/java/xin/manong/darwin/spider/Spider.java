package xin.manong.darwin.spider;

import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.notify.JobCompleteNotifier;
import xin.manong.darwin.service.notify.URLCompleteNotifier;
import xin.manong.darwin.service.request.URLSearchRequest;
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

/**
 * 爬虫抽象实现
 *
 * @author frankcl
 * @date 2023-03-24 16:18:17
 */
public abstract class Spider {

    private static final Logger logger = LoggerFactory.getLogger(Spider.class);

    protected static final int HTTP_CODE_OK = 200;
    protected static final int HTTP_CODE_NOT_ACCEPTABLE = 406;

    protected static final String MIME_TYPE_TEXT = "text";
    protected static final String MIME_TYPE_VIDEO = "video";
    protected static final String MIME_TYPE_APPLICATION = "application";
    protected static final String SUB_MIME_TYPE_PDF = "pdf";
    protected static final String SUB_MIME_TYPE_MP4 = "mp4";
    protected static final String SUB_MIME_TYPE_JSON = "json";
    protected static final String SUB_MIME_TYPE_HTML = "html";

    protected static final String HEADER_USER_AGENT = "User-Agent";
    protected static final String HEADER_REFERER = "Referer";
    protected static final String HEADER_HOST = "Host";

    protected String category;
    @Resource
    protected SpiderConfig config;
    @Resource(name = "recordAspectLogger")
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
    protected URLCompleteNotifier urlCompleteNotifier;
    @Resource
    protected JobCompleteNotifier jobCompleteNotifier;
    @Resource(name = "spiderLongProxySelector")
    protected SpiderProxySelector spiderLongProxySelector;
    @Resource(name = "spiderShortProxySelector")
    protected SpiderProxySelector spiderShortProxySelector;
    private HttpClient httpClient;
    private HttpClient proxyLongHttpClient;
    private HttpClient proxyShortHttpClient;

    public Spider(String category) {
        this.category = category;
    }

    /**
     * 写入抓取内容到OSS
     *
     * @param record URL记录
     * @param inputStream 内容字节流
     * @param context 上下文
     * @return 成功返回true，否则返回false
     */
    protected boolean writeStream(URLRecord record, InputStream inputStream, Context context) {
        Long startTime = System.currentTimeMillis();
        try {
            String key = String.format("%s/%s/%s", config.contentDirectory, category, record.key);
            String suffix = buildResourceFileSuffix(record);
            if (!StringUtils.isEmpty(suffix)) key = String.format("%s.%s", key, suffix);
            if (!ossClient.putObject(config.contentBucket, key, inputStream)) {
                record.status = Constants.URL_STATUS_IO_ERROR;
                logger.error("write fetch content failed for url[{}]", record.url);
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取内容写入OSS失败");
                return false;
            }
            OSSMeta ossMeta = new OSSMeta();
            ossMeta.region = config.contentRegion;
            ossMeta.bucket = config.contentBucket;
            ossMeta.key = key;
            record.fetchContentURL = OSSClient.buildURL(ossMeta);
            return true;
        } finally {
            context.put(Constants.DARWIN_WRITE_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 获取HTTPClient
     *
     * @param fetchMethod 抓取方式
     * @return HttpClient
     */
    protected HttpClient getHttpClient(Integer fetchMethod) {
        if ((fetchMethod == null || (fetchMethod != Constants.FETCH_METHOD_LONG_PROXY &&
                fetchMethod != Constants.FETCH_METHOD_SHORT_PROXY)) && httpClient != null) return httpClient;
        if (fetchMethod != null && fetchMethod == Constants.FETCH_METHOD_LONG_PROXY &&
                proxyLongHttpClient != null) return proxyLongHttpClient;
        if (fetchMethod != null && fetchMethod == Constants.FETCH_METHOD_SHORT_PROXY &&
                proxyShortHttpClient != null) return proxyShortHttpClient;
        HttpClientConfig httpClientConfig = new HttpClientConfig();
        httpClientConfig.connectTimeoutSeconds = config.connectTimeoutSeconds;
        httpClientConfig.readTimeoutSeconds = config.readTimeoutSeconds;
        httpClientConfig.keepAliveMinutes = config.keepAliveMinutes;
        httpClientConfig.maxIdleConnections = config.maxIdleConnections;
        httpClientConfig.retryCnt = config.retryCnt;
        synchronized (this) {
            if (fetchMethod == null || (fetchMethod != Constants.FETCH_METHOD_LONG_PROXY &&
                    fetchMethod != Constants.FETCH_METHOD_SHORT_PROXY)) {
                if (httpClient != null) return httpClient;
                httpClient = new HttpClient(httpClientConfig);
                return httpClient;
            }
            if (fetchMethod != null && fetchMethod == Constants.FETCH_METHOD_LONG_PROXY) {
                if (proxyLongHttpClient != null) return proxyLongHttpClient;
                proxyLongHttpClient = new HttpClient(httpClientConfig, spiderLongProxySelector);
                return proxyLongHttpClient;
            }
            if (fetchMethod != null && fetchMethod == Constants.FETCH_METHOD_SHORT_PROXY) {
                if (proxyShortHttpClient != null) return proxyShortHttpClient;
                proxyShortHttpClient = new HttpClient(httpClientConfig, spiderShortProxySelector);
                return proxyShortHttpClient;
            }
            return null;
        }
    }

    /**
     * 获取以前抓取资源，避免重复抓取
     * 满足以下条件可以使用以前抓取资源
     * 1. 任务设置避免重复抓取
     * 2. URL在一天内抓取过
     * 3. 抓取资源OSS数据存在
     *
     * @param record URL记录
     * @param context 上下文
     * @return 成功返回抓取资源，否则返回null
     */
    protected SpiderResource getSpiderResource(URLRecord record, Context context) {
        if (context == null || !context.contains(Constants.AVOID_REPEATED_FETCH) ||
                !((boolean) context.get(Constants.AVOID_REPEATED_FETCH))) return null;
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.status = Constants.URL_STATUS_SUCCESS;
        searchRequest.url = record.url;
        searchRequest.fetchTime = new RangeValue<>();
        searchRequest.fetchTime.start = System.currentTimeMillis() - config.reuseExpiredTimeMs;
        searchRequest.fetchTime.includeLower = true;
        searchRequest.current = 1;
        searchRequest.size = 1;
        Pager<URLRecord> pager = urlService.search(searchRequest);
        if (pager == null || pager.records == null || pager.records.isEmpty()) return null;
        URLRecord prevRecord = pager.records.get(0);
        return SpiderResource.buildFrom(prevRecord, ossClient);
    }

    /**
     * 根据资源mimeType构建资源文件后缀
     *
     * @param record URL记录
     * @return 成功返回资源文件后缀，否则返回null
     */
    protected String buildResourceFileSuffix(URLRecord record) {
        if (!Constants.SUPPORT_MIME_TYPES.contains(record.mimeType)) return null;
        if (StringUtils.isEmpty(record.subMimeType)) return null;
        if (record.mimeType.equalsIgnoreCase(MIME_TYPE_TEXT) &&
                !record.subMimeType.equalsIgnoreCase(SUB_MIME_TYPE_HTML)) return null;
        if (record.mimeType.equalsIgnoreCase(MIME_TYPE_APPLICATION) &&
                !record.subMimeType.equalsIgnoreCase(SUB_MIME_TYPE_PDF) &&
                !record.subMimeType.equalsIgnoreCase(SUB_MIME_TYPE_JSON)) return null;
        return record.subMimeType.toLowerCase();
    }

    /**
     * 抓取URL
     *
     * @param record URL记录
     * @param context 上下文
     * @return 爬虫抓取资源
     */
    protected SpiderResource fetch(URLRecord record, Context context) {
        try {
            HttpClient httpClient = getHttpClient(record.fetchMethod);
            HttpRequest httpRequest = new HttpRequest.Builder().requestURL(record.url).method(RequestMethod.GET).build();
            if (!StringUtils.isEmpty(config.userAgent)) httpRequest.headers.put(HEADER_USER_AGENT, config.userAgent);
            if (!StringUtils.isEmpty(record.parentURL)) httpRequest.headers.put(HEADER_REFERER, record.parentURL);
            String host = CommonUtil.getHost(record.url);
            if (!StringUtils.isEmpty(host) && !CommonUtil.isIP(host)) httpRequest.headers.put(HEADER_HOST, host);
            if (record.headers != null && !record.headers.isEmpty()) httpRequest.headers.putAll(record.headers);
            Response httpResponse = httpClient.execute(httpRequest);
            if (httpResponse == null || !httpResponse.isSuccessful()) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "执行HTTP请求失败");
                logger.error("execute http request failed for url[{}]", record.url);
            }
            return SpiderResource.buildFrom(record.url, httpResponse);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "执行HTTP请求异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("exception occurred when fetching url[{}]", record.url);
            logger.error(e.getMessage(), e);
            return SpiderResource.buildFrom(record.url, null);
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
        Job job = null;
        try {
            job = jobService.getCache(record.jobId);
            if (job != null && job.avoidRepeatedFetch) context.put(Constants.AVOID_REPEATED_FETCH, true);
            handle(record, context);
        } catch (Throwable t) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取数据异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(t));
            logger.error("fetch record error for url[{}]", record.url);
            logger.error(t.getMessage(), t);
        } finally {
            urlCompleteNotifier.onComplete(record, context);
            if (multiQueue.isEmptyJobRecordMap(record.jobId)) {
                multiQueue.deleteJobRecordMap(record.jobId);
                jobCompleteNotifier.onComplete(job, new Context());
            }
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
