package xin.manong.darwin.executor.scheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.concurrent.ConcurrentManager;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.notify.JobCompleteNotifier;
import xin.manong.darwin.service.notify.URLCompleteNotifier;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.kafka.KafkaProducer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * URL调度器
 *
 * @author frankcl
 * @date 2023-03-22 17:33:16
 */
public class URLScheduler extends AspectLogSupport {

    private static final Logger logger = LoggerFactory.getLogger(URLScheduler.class);

    public static final String NAME = "URLScheduler";

    private final long maxOverflowTimeMs;
    private final String topic;
    @Resource
    protected URLService urlService;
    @Resource
    protected JobService jobService;
    @Resource
    protected MultiQueue multiQueue;
    @Resource
    protected ConcurrentManager concurrentManager;
    @Resource
    protected URLCompleteNotifier urlCompleteNotifier;
    @Resource
    protected JobCompleteNotifier jobCompleteNotifier;
    @Resource
    protected KafkaProducer producer;

    public URLScheduler(String topic, Long executeIntervalMs, Long maxOverflowTimeMs) {
        super(NAME, executeIntervalMs);
        this.maxOverflowTimeMs = maxOverflowTimeMs;
        this.topic = topic;
    }

    @Override
    public void execute() {
        if (!multiQueue.tryLockOutQueue()) {
            logger.info("acquired URL queue lock for popping failed");
            return;
        }
        try {
            Set<String> concurrentUnits = multiQueue.concurrentUnitsSnapshots();
            for (String concurrentUnit : concurrentUnits) handleConcurrentUnit(concurrentUnit);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            multiQueue.unlockOutQueue();
        }
    }

    /**
     * 并发单元数据调度
     *
     * @param concurrentUnit 并发单元
     */
    private void handleConcurrentUnit(String concurrentUnit) {
        int appliedConnections = 0, acquiredConnections = 0, overflowConnections = 0;
        Context context = new Context();
        try {
            appliedConnections = concurrentManager.getAvailableConnectionCount(concurrentUnit);
            if (appliedConnections <= 0) {
                context.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_FAIL);
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "没有可用连接");
                logger.warn("no available connections[{}] for concurrent unit[{}]", appliedConnections, concurrentUnit);
                return;
            }
            context.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_SUCCESS);
            int popConnections = appliedConnections;
            while (popConnections > 0) {
                List<URLRecord> records = multiQueue.pop(concurrentUnit, popConnections);
                for (URLRecord record : records) {
                    if (isOverflowRecord(record)) {
                        handleOverflowRecord(record);
                        overflowConnections++;
                    } else {
                        handleURLRecord(record, concurrentUnit);
                        acquiredConnections++;
                    }
                }
                if (records.size() < popConnections) break;
                popConnections = appliedConnections - acquiredConnections;
            }
            logger.info("success handle records[{}] for concurrent unit[{}], acquired[{}], overflow[{}]",
                    acquiredConnections + overflowConnections, concurrentUnit, acquiredConnections, overflowConnections);
        } catch (Exception e) {
            context.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_FAIL);
            context.put(Constants.DARWIN_DEBUG_MESSAGE, e.getMessage());
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("process concurrent unit[{}] failed while scheduling", concurrentUnit);
            logger.error(e.getMessage(), e);
        } finally {
            commitAspectLog(context, concurrentUnit, appliedConnections,
                    acquiredConnections, overflowConnections);
        }
    }

    /**
     * 处理溢出数据
     *
     * @param record 溢出数据
     */
    private void handleOverflowRecord(URLRecord record) {
        Context context = new Context();
        context.put(Constants.DARWIN_STAGE, Constants.STAGE_POP);
        urlCompleteNotifier.onComplete(buildOverflowRecord(record), context);
        if (jobService.finish(record.jobId)) {
            jobCompleteNotifier.onComplete(record.jobId, new Context());
        }
    }

    /**
     * 处理URL
     * 1. 发送URL到消息队列，等待爬虫spider抓取
     * 2. 在redis并发单元中记录URL信息
     *
     * @param record URL数据
     * @param concurrentUnit 并发单元
     */
    private void handleURLRecord(URLRecord record, String concurrentUnit) {
        Context context = new Context();
        context.put(Constants.DARWIN_STAGE, Constants.STAGE_POP);
        try {
            record.status = Constants.URL_STATUS_FETCHING;
            record.popTime = System.currentTimeMillis();
            if (!urlService.updateQueueTime(record)) logger.warn("update url record[{}] failed", record.key);
            byte[] bytes = JSON.toJSONString(record, SerializerFeature.DisableCircularReferenceDetect).
                    getBytes(StandardCharsets.UTF_8);
            byte[] category = String.format("%d", record.category == null ?
                    Constants.CONTENT_CATEGORY_CONTENT : record.category).getBytes(StandardCharsets.UTF_8);
            RecordHeaders headers = new RecordHeaders();
            headers.add(Constants.CATEGORY, category);
            RecordMetadata metadata = producer.send(record.key, bytes, topic, headers);
            if (metadata == null) {
                logger.warn("send record[{}] for fetching failed", record.key);
                return;
            }
            context.put(Constants.DARWIN_MESSAGE_KEY, record.key);
            concurrentManager.putConnectionRecord(concurrentUnit, record.key);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, e.getMessage());
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("process record failed for key[{}]", record.key);
            logger.error(e.getMessage(), e);
        } finally {
            commitAspectLog(context, record);
        }
    }

    /**
     * 判断是否为溢出数据
     *
     * @param record 数据
     * @return 溢出返回true，否则返回false
     */
    private boolean isOverflowRecord(URLRecord record) {
        return record.pushTime == null || System.currentTimeMillis() -
                record.pushTime > maxOverflowTimeMs;
    }

    /**
     * 构建溢出记录
     *
     * @param record URL记录
     * @return 溢出记录
     */
    private URLRecord buildOverflowRecord(URLRecord record) {
        URLRecord overflowRecord = new URLRecord();
        overflowRecord.key = record.key;
        overflowRecord.status = Constants.URL_STATUS_OVERFLOW;
        overflowRecord.depth = null;
        overflowRecord.fieldMap = null;
        overflowRecord.userDefinedMap = null;
        overflowRecord.headers = null;
        return overflowRecord;
    }
}
