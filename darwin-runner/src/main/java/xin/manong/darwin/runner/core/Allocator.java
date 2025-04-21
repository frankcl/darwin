package xin.manong.darwin.runner.core;

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
import xin.manong.darwin.queue.ConcurrencyControl;
import xin.manong.darwin.queue.ConcurrencyQueue;
import xin.manong.darwin.service.event.URLEventListener;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.event.JobEventListener;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.event.ErrorEvent;
import xin.manong.weapon.base.kafka.KafkaProducer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * URL调度分配
 *
 * @author frankcl
 * @date 2023-03-22 17:33:16
 */
public class Allocator extends AspectLogSupport {

    private static final Logger logger = LoggerFactory.getLogger(Allocator.class);

    public static final String ID = "allocator";

    private final long maxOverflowIntervalMs;
    private final String topic;
    @Resource
    protected URLService urlService;
    @Resource
    protected ConcurrencyQueue concurrencyQueue;
    @Resource
    protected ConcurrencyControl concurrencyControl;
    @Resource
    protected URLEventListener urlEventListener;
    @Resource
    protected JobEventListener jobEventListener;
    @Resource
    protected KafkaProducer producer;

    public Allocator(String topic, Long executeIntervalMs, Long maxOverflowIntervalMs) {
        super(ID, executeIntervalMs);
        this.topic = topic;
        this.maxOverflowIntervalMs = maxOverflowIntervalMs;
        this.setName("爬虫链接分配器");
        this.setDescription("根据并发控制原则从多级队列中以并发单位为粒度分配链接并送入爬虫抓取");
    }

    @Override
    public void execute() {
        if (!concurrencyQueue.acquirePopLock()) {
            logger.info("Acquired MultiLevelQueue lock for popping failed");
            return;
        }
        try {
            Set<String> concurrentUnits = concurrencyQueue.concurrentUnitsSnapshots();
            for (String concurrentUnit : concurrentUnits) allocate(concurrentUnit);
        } finally {
            concurrencyQueue.releasePopLock();
        }
    }

    /**
     * 并发单元链接分配
     *
     * @param concurrentUnit 并发单元
     */
    private void allocate(String concurrentUnit) {
        int applyRecordNum = 0, allocateRecordNum = 0, overflowRecordNum = 0;
        Context context = new Context();
        try {
            applyRecordNum = concurrencyControl.getAvailableConnections(concurrentUnit);
            if (applyRecordNum <= 0) {
                context.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_FAIL);
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "没有可用连接");
                logger.warn("No available connections for concurrent unit:{}", concurrentUnit);
                return;
            }
            context.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_SUCCESS);
            int n = applyRecordNum;
            while (n > 0) {
                List<URLRecord> records = concurrencyQueue.pop(concurrentUnit, n);
                for (URLRecord record : records) {
                    if (record.isOverflow(maxOverflowIntervalMs)) {
                        handleOverflow(record);
                        overflowRecordNum++;
                        continue;
                    }
                    handleNormal(record);
                    allocateRecordNum++;
                }
                if (records.size() < n) break;
                n = applyRecordNum - allocateRecordNum;
            }
            logger.info("Handle concurrent unit:{} success, allocate num:{}, overflow num:{}",
                    concurrentUnit, allocateRecordNum, overflowRecordNum);
        } catch (Exception e) {
            context.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_FAIL);
            context.put(Constants.DARWIN_DEBUG_MESSAGE, e.getMessage());
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("Handle concurrent unit:{} failed", concurrentUnit);
            logger.error(e.getMessage(), e);
            notifyErrorEvent(new ErrorEvent(e.getMessage(), e));
        } finally {
            commitAspectLog(context, concurrentUnit, applyRecordNum, allocateRecordNum, overflowRecordNum);
        }
    }

    /**
     * 处理溢出数据
     *
     * @param record 溢出数据
     */
    private void handleOverflow(URLRecord record) {
        Context context = new Context();
        context.put(Constants.DARWIN_STAGE, Constants.STAGE_POP);
        if (!urlService.updateStatus(record.key, Constants.URL_STATUS_OVERFLOW)) {
            logger.warn("Update overflow status failed for url:{}", record.url);
        }
        urlEventListener.onComplete(record.key, context);
        jobEventListener.onComplete(record.jobId, new Context());
    }

    /**
     * 处理普通链接
     * 1. 发送链接到消息队列，等待爬虫抓取
     * 2. 在并发控制单元中添加抓取连接
     *
     * @param record URL数据
     */
    private void handleNormal(URLRecord record) {
        Context context = new Context();
        context.put(Constants.DARWIN_STAGE, Constants.STAGE_POP);
        try {
            record.status = Constants.URL_STATUS_FETCHING;
            record.popTime = System.currentTimeMillis();
            if (!urlService.updateQueueTime(record)) logger.warn("Update url record:{} failed", record.key);
            if (!pushMessage(record)) return;
            logger.info("Push fetching message success for url:{}", record.url);
            context.put(Constants.DARWIN_MESSAGE_KEY, record.key);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, e.getMessage());
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("Handle record failed for key:{}", record.key);
            logger.error(e.getMessage(), e);
        } finally {
            commitAspectLog(context, record);
        }
    }

    /**
     * 发送抓取链接到消息队列
     *
     * @param record 链接数据
     * @return 成功返回true，否则返回false
     */
    private boolean pushMessage(URLRecord record) {
        byte[] bytes = JSON.toJSONString(record, SerializerFeature.DisableCircularReferenceDetect).
                getBytes(StandardCharsets.UTF_8);
        byte[] category = String.format("%d", record.category == null ?
                Constants.CONTENT_CATEGORY_CONTENT : record.category).getBytes(StandardCharsets.UTF_8);
        RecordHeaders headers = new RecordHeaders();
        headers.add(Constants.CATEGORY, category);
        RecordMetadata metadata = producer.send(record.key, bytes, topic, headers);
        if (metadata != null) return true;
        logger.warn("Push fetching message failed for url:{}", record.url);
        return false;
    }
}
