package xin.manong.darwin.spider.core;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.MediaType;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.log.core.AspectLogSupport;
import xin.manong.darwin.queue.ConcurrencyControl;
import xin.manong.darwin.queue.ConcurrencyQueue;
import xin.manong.darwin.queue.CrawlDelayControl;
import xin.manong.darwin.queue.PushResult;
import xin.manong.darwin.service.event.JobEventListener;
import xin.manong.darwin.service.event.URLEventListener;
import xin.manong.darwin.service.iface.OSSService;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.spider.input.HTTPInput;
import xin.manong.darwin.spider.input.Input;
import xin.manong.darwin.spider.input.OSSInput;
import xin.manong.weapon.base.common.Context;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * 爬虫路由
 * 根据抓取情况及类型将数据导向不同爬虫抓取
 *
 * @author frankcl
 * @date 2025-04-27 13:09:40
 */
@Component
public class Router {

    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    @Resource
    private SpiderConfig spiderConfig;
    @Resource
    @Lazy
    private TextSpider textSpider;
    @Resource
    @Lazy
    private ResourceSpider resourceSpider;
    @Resource
    private CrawlDelayControl crawlDelayControl;
    @Resource
    private ConcurrencyControl concurrencyControl;
    @Resource
    private ConcurrencyQueue concurrencyQueue;
    @Resource
    private HttpClientFactory httpClientFactory;
    @Resource
    private PlanService planService;
    @Resource
    private URLService urlService;
    @Resource
    private OSSService ossService;
    @Resource
    private URLEventListener urlEventListener;
    @Resource
    private JobEventListener jobEventListener;
    @Resource
    private AspectLogSupport aspectLogSupport;
    private final Map<MediaType, Spider> routeTable = new HashMap<>();

    /**
     * 路由数据进行抓取
     *
     * @param record 数据
     * @param context 上下文
     */
    public void route(URLRecord record, Context context) {
        long startTime = System.currentTimeMillis();
        boolean passConcurrency = true;
        try {
            passConcurrency = concurrencyCheck(record);
            if (!passConcurrency) return;
            concurrencyControl.putConnection(record.concurrencyUnit, record.key);
            crawlDelayCheck(record);
            Plan plan = planService.get(record.planId);
            if (plan != null) {
                context.put(Constants.MAX_DEPTH, plan.maxDepth);
                context.put(Constants.ALLOW_DISPATCH_FAIL, plan.allowDispatchFail);
            }
            record.fetchTime = System.currentTimeMillis();
            URLRecord prevRecord = fetchedRecord(record);
            MediaType mediaType;
            try (Input input = openInput(record, prevRecord)) {
                Spider spider = route(record.mediaType);
                if (spider == null) throw new UnsupportedOperationException(
                        String.format("不支持的媒体类型:%s", record.mediaType));
                mediaType = spider.handle(record, input, context);
            }
            while (mediaType != MediaType.UNKNOWN) {
                Spider spider = route(mediaType);
                if (spider == null) throw new UnsupportedOperationException(
                        String.format("不支持的媒体类型:%s", mediaType));
                mediaType = spider.handle(record, context);
            }
            if (record.status != Constants.URL_STATUS_PARSE_ERROR) {
                record.status = Constants.URL_STATUS_FETCH_SUCCESS;
                logger.info("Fetch success for url: {}", record.url);
            }
        } catch (IOException e) {
            record.status = Constants.URL_STATUS_FETCH_FAIL;
            if (e instanceof SocketTimeoutException) {
                record.status = Constants.URL_STATUS_TIMEOUT;
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取超时");
            } else if (e instanceof UnknownHostException) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "域名解析失败");
            }
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("Fetch failed for url: {}", record.url);
            logger.error(e.getMessage(), e);
        } catch (Throwable t) {
            record.status = Constants.URL_STATUS_ERROR;
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "抓取数据异常");
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(t));
            logger.error("Fetch error for url: {}", record.url);
            logger.error(t.getMessage(), t);
        } finally {
            if (passConcurrency) {
                concurrencyControl.removeConnection(record.concurrencyUnit, record.key);
                if (!urlService.updateContent(record)) logger.warn("Update fetching content failed for url:{}", record.url);
                urlEventListener.onComplete(record.key, context);
                jobEventListener.onComplete(record.jobId, null);
            }
            context.put(Constants.DARWIN_PROCESS_TIME, System.currentTimeMillis() - startTime);
            aspectLogSupport.commitAspectLog(context, record);
        }
    }

    /**
     * 注册爬虫实例到路由表
     *
     * @param mediaType 媒体类型
     * @param spider 爬虫实例
     */
    public void registerSpider(MediaType mediaType, Spider spider) {
        routeTable.put(mediaType, spider);
    }

    /**
     * 根据媒体类型路由到对应spider
     *
     * @param mediaType 媒体类型
     * @return 成功返回spider，否则返回null
     */
    private Spider route(MediaType mediaType) {
        if (mediaType == null) return null;
        Spider spider = routeTable.get(mediaType);
        if (spider != null) return spider;
        if (mediaType.isText()) return textSpider;
        if (mediaType.isVideo() || mediaType.isImage() || mediaType.isAudio()) return resourceSpider;
        return null;
    }

    /**
     * 构建并打开数据输入
     * 1. 如果存量数据存在，则从OSS读取数据，返回OSSInput
     * 2. 否则通过HTTP协议从网络抓取数据，返回HTTPInput
     *
     * @param record 数据
     * @param prevRecord 存量数据
     * @return 数据输入
     * @throws IOException I/O异常
     */
    private Input openInput(URLRecord record, URLRecord prevRecord) throws IOException {
        if (prevRecord == null) {
            record.fetched = true;
            Input input = new HTTPInput(record, httpClientFactory.getHttpClient(record), spiderConfig);
            input.open();
            return input;
        }
        record.fetched = false;
        record.status = prevRecord.status;
        record.httpCode = prevRecord.httpCode;
        record.redirectURL = prevRecord.redirectURL;
        record.mediaType = prevRecord.mediaType;
        record.charset = prevRecord.charset;
        record.htmlCharset = prevRecord.htmlCharset;
        record.contentLength = prevRecord.contentLength;
        Input input = new OSSInput(prevRecord.fetchContentURL, ossService);
        input.open();
        return input;
    }

    /**
     * 根据URL查找抓取过数据
     * 1. 如果设置allow_repeat=false
     * 2. 时间范围内存在抓取过的数据
     *
     * @param record URL数据
     * @return 如果存在则返回抓取过的数据，否则返回null
     */
    private URLRecord fetchedRecord(URLRecord record) {
        if (record.allowRepeat != null && record.allowRepeat) return null;
        long startTime = System.currentTimeMillis() - spiderConfig.maxRepeatFetchTimeIntervalMs;
        URLRecord prevRecord = urlService.getFetched(record, startTime);
        if (prevRecord == null || StringUtils.isEmpty(prevRecord.fetchContentURL)) return null;
        return ossService.existsByURL(prevRecord.fetchContentURL) ? prevRecord : null;
    }

    /**
     * 并发控制检测
     *
     * @param record 数据
     * @return 通过返回true，否则返回false
     * @throws Exception 异常
     */
    private boolean concurrencyCheck(URLRecord record) throws Exception {
        if (concurrencyControl.allowFetching(record.concurrencyUnit)) return true;
        logger.info("Concurrency check not pass for url:{}", record.url);
        record.popTime = null;
        PushResult pushResult = concurrencyQueue.push(record, 3);
        if (pushResult != PushResult.SUCCESS) {
            if (!urlService.updateStatus(record.key, Constants.URL_STATUS_ERROR)) {
                logger.warn("Update error status failed for url:{}", record.url);
            }
            logger.warn("Push back:{} failed for concurrency check", record.url);
            throw new Exception("Push back failed for concurrency check");
        }
        if (!urlService.updateQueueTime(record)) {
            logger.warn("Update queue time failed when concurrency check for url:{}", record.url);
            throw new Exception("Update queue time failed when concurrency check");
        }
        return false;
    }

    /**
     * 抓取间隔检测
     *
     * @param record 数据
     */
    private void crawlDelayCheck(URLRecord record) {
        crawlDelayControl.delay(record.concurrencyUnit);
        crawlDelayControl.put(record.concurrencyUnit);
    }
}
