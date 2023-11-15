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
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.concurrent.ConcurrentManager;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.iface.URLService;
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
    protected URLService urlService;
    @Resource
    protected MultiQueue multiQueue;
    @Resource
    protected ConcurrentManager concurrentManager;
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
        int appliedConnections = 0, acquiredConnections = 0;
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
            List<URLRecord> records = multiQueue.pop(concurrentUnit, appliedConnections);
            acquiredConnections = records.size();
            for (URLRecord record : records) handleURLRecord(record, concurrentUnit);
            logger.info("handle records[{}] for concurrent unit[{}]", records.size(), concurrentUnit);
        } catch (Exception e) {
            concurrentContext.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_FAIL);
            concurrentContext.put(Constants.DARWIN_DEBUG_MESSAGE, e.getMessage());
            concurrentContext.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("process concurrent unit[{}] failed while scheduling", concurrentUnit);
            logger.error(e.getMessage(), e);
        } finally {
            commitAspectLog(concurrentContext, concurrentUnit, appliedConnections, acquiredConnections);
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
        } catch (Exception ex) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, ex.getMessage());
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(ex));
            logger.error("process record failed for key[{}]", record.key);
            logger.error(ex.getMessage(), ex);
        } finally {
            commitAspectLog(context, record);
        }
    }
}
