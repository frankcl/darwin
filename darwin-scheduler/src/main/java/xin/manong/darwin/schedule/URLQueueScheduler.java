package xin.manong.darwin.schedule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.concurrent.ConcurrentManager;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.notify.JobCompleteNotifier;
import xin.manong.darwin.service.notify.URLCompleteNotifier;
import xin.manong.weapon.aliyun.ons.ONSProducer;
import xin.manong.weapon.base.common.Context;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

/**
 * URL队列调度器
 *
 * @author frankcl
 * @date 2023-03-22 17:33:16
 */
public class URLQueueScheduler extends ExecuteRunner {

    private static final Logger logger = LoggerFactory.getLogger(URLQueueScheduler.class);

    private String topic;
    @Resource
    protected ScheduleConfig config;
    @Resource
    protected URLService urlService;
    @Resource
    protected MultiQueue multiQueue;
    @Resource
    protected ConcurrentManager concurrentManager;
    @Resource
    protected URLCompleteNotifier urlCompleteNotifier;
    @Resource
    protected JobCompleteNotifier jobCompleteNotifier;
    @Resource
    protected ONSProducer producer;

    public URLQueueScheduler(String topic, Long executeIntervalMs) {
        super(executeIntervalMs);
        this.topic = topic;
    }

    @Override
    public void execute() {
        if (!multiQueue.tryLockOutQueue()) {
            logger.info("acquired out queue lock failed");
            return;
        }
        try {
            Set<String> concurrentUnits = multiQueue.copyCurrentConcurrentUnits();
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
        Context concurrentContext = new Context();
        try {
            appliedConnections = concurrentManager.getAvailableConnectionCount(concurrentUnit);
            if (appliedConnections <= 0) {
                concurrentContext.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_FAIL);
                concurrentContext.put(Constants.DARWIN_DEBUG_MESSAGE, "没有可用连接");
                logger.warn("available connections[{}] for concurrent unit[{}]", appliedConnections, concurrentUnit);
                return;
            }
            concurrentContext.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_SUCCESS);
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
            logger.info("handle records[{}] for concurrent unit[{}], normal[{}], overflow[{}]",
                    acquiredConnections + overflowConnections, concurrentUnit, acquiredConnections, overflowConnections);
        } catch (Exception e) {
            concurrentContext.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_FAIL);
            concurrentContext.put(Constants.DARWIN_DEBUG_MESSAGE, e.getMessage());
            concurrentContext.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("process concurrent unit[{}] failed while scheduling", concurrentUnit);
            logger.error(e.getMessage(), e);
        } finally {
            commitAspectLog(concurrentContext, concurrentUnit, appliedConnections,
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
        urlCompleteNotifier.onComplete(buildOverflowRecord(record), context);
        if (multiQueue.isEmptyJobRecordMap(record.jobId)) {
            multiQueue.deleteJobRecordMap(record.jobId);
            jobCompleteNotifier.onComplete(new Job(record.jobId, record.appId), new Context());
        }
        commitAspectLog(context, record);
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
        try {
            record.status = Constants.URL_STATUS_FETCHING;
            record.outQueueTime = System.currentTimeMillis();
            if (!urlService.updateQueueTime(record)) logger.warn("update url record[{}] failed", record.key);
            byte[] bytes = JSON.toJSONString(record, SerializerFeature.DisableCircularReferenceDetect).
                    getBytes(Charset.forName("UTF-8"));
            String tags = String.format("%d", record.category == null ?
                    Constants.CONTENT_CATEGORY_CONTENT : record.category);
            Message message = new Message(topic, tags, record.key, bytes);
            SendResult sendResult = producer.send(message);
            if (sendResult == null || StringUtils.isEmpty(sendResult.getMessageId())) {
                logger.warn("send record[{}] failed", record.key);
                return;
            }
            context.put(Constants.DARWIN_MESSAGE_ID, sendResult.getMessageId());
            context.put(Constants.DARWIN_MESSAGE_KEY, record.key);
            concurrentManager.putConnectionRecord(concurrentUnit, record.key);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, e.getMessage());
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
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
        return record.inQueueTime == null || System.currentTimeMillis() -
                record.inQueueTime > config.maxOverflowTimeMs;
    }

    /**
     * 构建溢出记录
     *
     * @param record URL记录
     * @return 溢出记录
     */
    private URLRecord buildOverflowRecord(URLRecord record) {
        URLRecord overflow = new URLRecord();
        overflow.key = record.key;
        overflow.status = Constants.URL_STATUS_OVERFLOW;
        overflow.depth = null;
        overflow.fieldMap = null;
        overflow.userDefinedMap = null;
        overflow.headers = null;
        return overflow;
    }
}
