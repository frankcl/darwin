package xin.manong.darwin.spider.core;

import jakarta.annotation.Resource;
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
import xin.manong.darwin.spider.input.HTTPInput;
import xin.manong.darwin.spider.input.Input;
import xin.manong.darwin.spider.input.OSSInput;
import xin.manong.darwin.spider.output.OSSOutput;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.http.*;
import xin.manong.weapon.base.log.JSONLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    protected static final String CHARSET_UTF8 = "UTF-8";
    protected static final String MIME_TYPE_TEXT = "text";
    protected static final String MIME_TYPE_VIDEO = "video";
    protected static final String MIME_TYPE_APPLICATION = "application";
    protected static final String SUB_MIME_TYPE_PDF = "pdf";
    protected static final String SUB_MIME_TYPE_MP4 = "mp4";
    protected static final String SUB_MIME_TYPE_JSON = "json";
    protected static final String SUB_MIME_TYPE_HTML = "html";

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
     * 构建抓取数据输入
     *
     * @param record URL数据
     * @param context 上下文
     * @return 抓取数据输入
     */
    protected Input buildInput(URLRecord record, Context context) {
        URLRecord prevRecord = search(record, context);
        if (checkFetchContentURL(prevRecord)) {
            record.status = prevRecord.status;
            record.httpCode = prevRecord.httpCode;
            record.redirectURL = prevRecord.redirectURL;
            record.mimeType = prevRecord.mimeType;
            record.subMimeType = prevRecord.subMimeType;
            record.charset = StandardCharsets.UTF_8;
            return new OSSInput(prevRecord.fetchContentURL, ossClient);
        }
        return new HTTPInput(record, getHttpClient(record.fetchMethod), config);
    }

    /**
     * 输入源写入OSS
     *
     * @param record URL数据
     * @param input 输入数据源
     * @param context 上下文
     * @throws IOException I/O异常
     */
    protected void write(URLRecord record, Input input, Context context) throws IOException {
        long startTime = System.currentTimeMillis();
        try {
            try (input) {
                input.open();
                OSSMeta ossMeta = buildOSSMeta(record);
                OSSOutput output = new OSSOutput(ossMeta, ossClient);
                input.transport(output);
                record.fetchContentURL = OSSClient.buildURL(ossMeta);
            }
        } finally {
            context.put(Constants.DARWIN_WRITE_TIME, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 推测字符集，顺序如下
     * 1. 使用HTTP response header中设定字符集
     * 2. 使用HTML header中设定字符集
     * 3. 猜测HTML字符集
     * 4. 以上都失败，使用UTF-8字符集
     *
     * @param byteArray HTML字节数组
     * @param record URL数据
     * @return 字符集
     */
    protected String speculateCharset(byte[] byteArray, URLRecord record) {
        if (record.charset != null) return record.charset.name();
        String charset = HTMLCharsetParser.parse(byteArray);
        if (StringUtils.isEmpty(charset)) charset = CharsetSpeculator.speculate(byteArray, 0, 1024);
        if (StringUtils.isNotEmpty(charset)) return charset;
        logger.warn("speculate charset failed, using UTF-8 charset for url: {}", record.url);
        return CHARSET_UTF8;
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
     * 搜索一定时间内抓取数据
     *
     * @param record URL数据
     * @param context 上下文
     * @return 如果存在一定时间内抓取数据则返回，否则返回null
     */
    private URLRecord search(URLRecord record, Context context) {
        if (context == null) return null;
        if (context.contains(Constants.ALLOW_REPEAT) && !(boolean) context.get(Constants.ALLOW_REPEAT)) return null;
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
        return pager == null || pager.records == null || pager.records.isEmpty() ? null : pager.records.get(0);
    }

    /**
     * 检测抓取结果是否可用
     *
     * @param record URL数据
     * @return 可用返回true，否则返回false
     */
    private boolean checkFetchContentURL(URLRecord record) {
        if (record == null || StringUtils.isEmpty(record.fetchContentURL)) return false;
        OSSMeta ossMeta = OSSClient.parseURL(record.fetchContentURL);
        return ossMeta != null && ossClient.exist(ossMeta.bucket, ossMeta.key);
    }

    /**
     * 构建OSS元数据
     *
     * @param record URL数据
     * @return OSS元数据
     */
    private OSSMeta buildOSSMeta(URLRecord record) {
        String suffix = generateSuffixUsingMimeType(record);
        String key = String.format("%s/%s/%s", config.contentDirectory, category, record.key);
        if (!StringUtils.isEmpty(suffix)) key = String.format("%s.%s", key, suffix);
        return new OSSMeta(config.contentRegion, config.contentBucket, key);
    }

    /**
     * 根据资源mimeType构建资源文件后缀
     *
     * @param record URL记录
     * @return 成功返回资源文件后缀，否则返回null
     */
    private String generateSuffixUsingMimeType(URLRecord record) {
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
            if (job != null && job.allowRepeat != null) context.put(Constants.ALLOW_REPEAT, job.allowRepeat);
            record.fetchTime = System.currentTimeMillis();
            handle(record, context);
            record.status = Constants.URL_STATUS_SUCCESS;
        } catch (Throwable t) {
            record.status = Constants.URL_STATUS_FETCH_FAIL;
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取数据异常");
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(t));
            logger.error("fetch failed for url: {}", record.url);
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
