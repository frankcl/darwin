package xin.manong.darwin.spider;

import jakarta.annotation.Resource;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.notify.JobCompleteNotifier;
import xin.manong.darwin.service.notify.URLCompleteNotifier;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.http.*;
import xin.manong.weapon.base.log.JSONLogger;
import xin.manong.weapon.base.util.CommonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    @Resource(name = "urlAspectLogger")
    protected JSONLogger aspectLogger;
    @Resource
    protected OSSClient ossClient;
    @Resource
    protected URLService urlService;
    @Resource
    protected JobService jobService;
    @Resource
    protected URLCompleteNotifier urlCompleteNotifier;
    @Resource
    protected JobCompleteNotifier jobCompleteNotifier;
    @Resource(name = "spiderLongProxySelector")
    protected SpiderProxySelector spiderLongProxySelector;
    @Resource(name = "spiderShortProxySelector")
    protected SpiderProxySelector spiderShortProxySelector;
    private final HttpProxyAuthenticator authenticator;
    private final Map<Integer, HttpClient> httpClientMap;

    public Spider(String category) {
        this.category = category;
        this.authenticator = new HttpProxyAuthenticator();
        this.httpClientMap = new ConcurrentHashMap<>();
    }

    /**
     * 写入抓取内容到OSS
     *
     * @param record URL记录
     * @param inputStream 内容字节流
     * @param context 上下文
     */
    protected void write(URLRecord record, InputStream inputStream, Context context) throws IOException {
        long startTime = System.currentTimeMillis();
        try {
            String key = String.format("%s/%s/%s", config.contentDirectory, category, record.key);
            String suffix = generateSuffixUsingMimeType(record);
            if (!StringUtils.isEmpty(suffix)) key = String.format("%s.%s", key, suffix);
            if (!ossClient.putObject(config.contentBucket, key, inputStream)) {
                record.status = Constants.URL_STATUS_IO_ERROR;
                logger.error("write fetched content failed for URL[{}]", record.url);
                throw new IOException("抓取内容写入OSS失败");
            }
            OSSMeta ossMeta = new OSSMeta();
            ossMeta.region = config.contentRegion;
            ossMeta.bucket = config.contentBucket;
            ossMeta.key = key;
            record.fetchContentURL = OSSClient.buildURL(ossMeta);
        } finally {
            context.put(Constants.DARWIN_WRITE_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 执行HTTP请求
     *
     * @param record URL数据
     * @return HTTP响应
     */
    protected Response httpRequest(URLRecord record) throws Exception {
        try {
            HttpClient httpClient = getHttpClient(record.fetchMethod);
            HttpRequest httpRequest = new HttpRequest.Builder().requestURL(record.url).method(RequestMethod.GET).build();
            if (!StringUtils.isEmpty(config.userAgent)) httpRequest.headers.put(HEADER_USER_AGENT, config.userAgent);
            if (!StringUtils.isEmpty(record.parentURL)) httpRequest.headers.put(HEADER_REFERER, record.parentURL);
            String host = CommonUtil.getHost(record.url);
            if (!StringUtils.isEmpty(host) && !CommonUtil.isIP(host)) httpRequest.headers.put(HEADER_HOST, host);
            if (record.headers != null && !record.headers.isEmpty()) httpRequest.headers.putAll(record.headers);
            if (record.timeout != null && record.timeout > 0) {
                httpRequest.connectTimeoutMs = record.timeout;
                httpRequest.readTimeoutMs = record.timeout;
            }
            return httpClient.execute(httpRequest);
        } catch (Exception e) {
            logger.error("exception occurred when fetching url[{}]", record.url);
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 根据抓取方式获取HTTPClient
     * 1. 长效代理HttpClient
     * 2. 短效代理HttpClient
     * 3. 本地IP HttpClient
     *
     * @param fetchMethod 抓取方式
     * @return HttpClient
     */
    protected HttpClient getHttpClient(Integer fetchMethod) {
        int category = fetchMethod == null ? Constants.FETCH_METHOD_COMMON : fetchMethod;
        if (httpClientMap.containsKey(category)) return httpClientMap.get(category);
        synchronized (this) {
            if (httpClientMap.containsKey(category)) return httpClientMap.get(category);
            HttpClientConfig httpClientConfig = new HttpClientConfig();
            httpClientConfig.connectTimeoutSeconds = config.connectTimeoutSeconds;
            httpClientConfig.readTimeoutSeconds = config.readTimeoutSeconds;
            httpClientConfig.keepAliveMinutes = config.keepAliveMinutes;
            httpClientConfig.maxIdleConnections = config.maxIdleConnections;
            httpClientConfig.retryCnt = config.retryCnt;
            HttpClient httpClient;
            if (category == Constants.FETCH_METHOD_LONG_PROXY) {
                httpClient = new HttpClient(httpClientConfig, spiderLongProxySelector, authenticator);
            } else if (category == Constants.FETCH_METHOD_SHORT_PROXY) {
                httpClient = new HttpClient(httpClientConfig, spiderShortProxySelector, authenticator);
            } else {
                httpClient = new HttpClient(httpClientConfig);
            }
            httpClientMap.put(category, httpClient);
            return httpClient;
        }
    }

    /**
     * 获取以前抓取记录输入流，避免重复抓取
     * 满足以下条件可以使用以前抓取资源
     * 1. 任务设置避免重复抓取
     * 2. URL在一天内抓取过
     * 3. 抓取资源OSS数据存在
     *
     * @param record URL记录
     * @param context 上下文
     * @return 成功返回输入流，否则返回null
     */
    protected InputStream getPrevInputStream(URLRecord record, Context context) {
        if (context == null || !context.contains(Constants.AVOID_REPEATED_FETCH) ||
                !((boolean) context.get(Constants.AVOID_REPEATED_FETCH))) return null;
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_SUCCESS);
        searchRequest.url = record.url;
        searchRequest.fetchTimeRange = new RangeValue<>();
        searchRequest.fetchTimeRange.start = System.currentTimeMillis() - config.reuseExpiredTimeMs;
        searchRequest.fetchTimeRange.includeLower = true;
        searchRequest.current = 1;
        searchRequest.size = 1;
        Pager<URLRecord> pager = urlService.search(searchRequest);
        if (pager == null || pager.records == null || pager.records.isEmpty()) return null;
        URLRecord prevRecord = pager.records.get(0);
        if (StringUtils.isEmpty(prevRecord.fetchContentURL)) return null;
        OSSMeta ossMeta = OSSClient.parseURL(prevRecord.fetchContentURL);
        if (ossMeta == null || !ossClient.exist(ossMeta.bucket, ossMeta.key)) return null;
        record.status = prevRecord.status;
        record.httpCode = prevRecord.httpCode;
        record.redirectURL = prevRecord.redirectURL;
        record.mimeType = prevRecord.mimeType;
        record.subMimeType = prevRecord.subMimeType;
        return ossClient.getObjectStream(ossMeta.bucket, ossMeta.key);
    }

    /**
     * 从HTTP响应中获取输入流
     *
     * @param httpResponse HTTP响应
     * @param record URL数据
     * @return 输入流
     * @throws Exception 异常
     */
    protected InputStream getHTTPInputStream(Response httpResponse, URLRecord record) throws Exception {
        if (httpResponse == null) throw new IOException("获取HTTP响应失败");
        if (httpResponse.isSuccessful()) record.status = Constants.URL_STATUS_SUCCESS;
        record.httpCode = httpResponse.code();
        String targetURL = httpResponse.request().url().url().toString();
        if (!StringUtils.isEmpty(targetURL) && !targetURL.equals(record.url)) record.redirectURL = targetURL;
        ResponseBody responseBody = httpResponse.body();
        assert responseBody != null;
        MediaType mediaType = responseBody.contentType();
        if (mediaType != null) {
            record.mimeType = mediaType.type();
            record.subMimeType = mediaType.subtype();
            record.charset = mediaType.charset();
        }
        return responseBody.byteStream();
    }

    /**
     * 根据资源mimeType构建资源文件后缀
     *
     * @param record URL记录
     * @return 成功返回资源文件后缀，否则返回null
     */
    protected String generateSuffixUsingMimeType(URLRecord record) {
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
     * 处理抓取数据
     *
     * @param record URL记录
     * @param context 上下文
     */
    public void process(URLRecord record, Context context) {
        long startTime = System.currentTimeMillis();
        try {
            Job job = jobService.getCache(record.jobId);
            if (job != null && job.avoidRepeatedFetch) context.put(Constants.AVOID_REPEATED_FETCH, true);
            record.status = Constants.URL_STATUS_FETCH_FAIL;
            record.fetchTime = System.currentTimeMillis();
            handle(record, context);
            record.status = Constants.URL_STATUS_SUCCESS;
        } catch (Throwable t) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取数据异常");
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(t));
            logger.error("fetch record error for url[{}]", record.url);
            logger.error(t.getMessage(), t);
        } finally {
            context.put(Constants.DARWIN_PROCESS_TIME, System.currentTimeMillis() - startTime);
            urlCompleteNotifier.onComplete(record, context);
            if (jobService.finish(record.jobId)) jobCompleteNotifier.onComplete(record.jobId, new Context());
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
