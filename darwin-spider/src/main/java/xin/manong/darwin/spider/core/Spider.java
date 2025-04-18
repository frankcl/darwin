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
import xin.manong.darwin.service.iface.OSSService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.notify.JobCompleteNotifier;
import xin.manong.darwin.service.notify.URLCompleteNotifier;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.darwin.service.util.HTMLUtil;
import xin.manong.darwin.spider.input.HTTPInput;
import xin.manong.darwin.spider.input.Input;
import xin.manong.darwin.spider.input.OSSInput;
import xin.manong.darwin.spider.output.OSSOutput;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.log.JSONLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
    protected static final String MIME_TYPE_VIDEO = "video";
    protected static final String SUB_MIME_TYPE_MP4 = "mp4";

    protected String category;
    @Resource
    protected SpiderConfig config;
    @Resource(name = "urlAspectLogger")
    protected JSONLogger aspectLogger;
    @Resource
    protected OSSService ossService;
    @Resource
    protected URLService urlService;
    @Resource
    protected JobService jobService;
    @Resource
    protected HttpClientFactory httpClientFactory;
    @Resource
    protected URLCompleteNotifier urlCompleteNotifier;
    @Resource
    protected JobCompleteNotifier jobCompleteNotifier;

    public Spider(String category) {
        this.category = category;
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
            return new OSSInput(prevRecord.fetchContentURL, ossService);
        }
        return new HTTPInput(record, httpClientFactory.getHttpClient(record), config);
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
                String ossKey = buildOSSKey(record);
                OSSOutput output = new OSSOutput(buildOSSKey(record), ossService);
                input.transport(output);
                record.fetchContentURL = ossService.buildURL(ossKey);
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
        searchRequest.fetchTimeRange.start = System.currentTimeMillis() - config.maxRepeatFetchTimeIntervalMs;
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
        return ossService.existsByURL(record.fetchContentURL);
    }

    /**
     * 构建OSS key
     *
     * @param record URL数据
     * @return OSS key
     */
    private String buildOSSKey(URLRecord record) {
        String suffix = HTMLUtil.generateSuffixUsingMimeType(record);
        String key = String.format("%s/%s/%s", config.ossDirectory, category, record.key);
        if (StringUtils.isEmpty(suffix)) return key;
        return String.format("%s.%s", key, suffix);
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
            logger.info("fetch success for url: {}", record.url);
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
