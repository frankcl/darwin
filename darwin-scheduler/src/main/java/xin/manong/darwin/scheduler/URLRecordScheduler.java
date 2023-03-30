package xin.manong.darwin.scheduler;

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
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.queue.concurrent.ConcurrentManager;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.weapon.aliyun.ons.ONSProducer;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.log.JSONLogger;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

/**
 * URL记录调度器
 *
 * @author frankcl
 * @date 2023-03-22 17:33:16
 */
public class URLRecordScheduler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(URLRecordScheduler.class);

    private boolean running;
    private Thread thread;
    @Resource
    protected URLRecordSchedulerConfig config;
    @Resource
    protected URLService urlService;
    @Resource
    protected MultiQueue multiQueue;
    @Resource
    protected ConcurrentManager concurrentManager;
    @Resource
    protected ONSProducer recordProducer;
    @Resource(name = "scheduleAspectLogger")
    protected JSONLogger aspectLogger;

    public URLRecordScheduler() {
        this.running = false;
    }

    /**
     * 启动URL记录调度器
     */
    public void start() {
        logger.info("{} is starting ...", this.getClass().getSimpleName());
        running = true;
        thread = new Thread(this, this.getClass().getSimpleName());
        thread.start();
        logger.info("{} has been started", this.getClass().getSimpleName());
    }

    /**
     * 停止URL记录调度器
     */
    public void stop() {
        logger.info("{} is stopping ...", this.getClass().getSimpleName());
        running = false;
        if (thread.isAlive()) thread.interrupt();
        try {
            thread.join();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        logger.info("{} has been stopped", this.getClass().getSimpleName());
    }

    @Override
    public void run() {
        while (running) {
            if (!multiQueue.tryLockOutQueue()) {
                logger.info("acquire out queue lock failed");
                waitMoment();
                continue;
            }
            try {
                Set<String> concurrentUnits = multiQueue.concurrentUnitsInQueue();
                for (String concurrentUnit : concurrentUnits) processConcurrentUnit(concurrentUnit);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                multiQueue.unlockOutQueue();
                waitMoment();
            }
        }
    }

    /**
     * 处理并发单元数据调度
     *
     * @param concurrentUnit 并发单元
     */
    private void processConcurrentUnit(String concurrentUnit) {
        int increaseConnections = 0, acquiredConnections = 0, releaseConnections = 0, returnConnections = 0;
        Context concurrentContext = new Context();
        try {
            int connections = concurrentManager.getAvailableConnections(concurrentUnit);
            if (connections <= 0) {
                concurrentContext.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_FAIL);
                concurrentContext.put(Constants.DARWIN_DEBUG_MESSAGE, "没有可用连接");
                logger.warn("available connections[{}] for concurrent unit[{}]", connections, concurrentUnit);
                return;
            }
            increaseConnections = concurrentManager.increaseConnections(concurrentUnit, connections);
            if (increaseConnections <= 0) {
                concurrentContext.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_FAIL);
                concurrentContext.put(Constants.DARWIN_DEBUG_MESSAGE, "申请可用连接失败");
                logger.warn("acquire connections failed for concurrent unit[{}]", concurrentUnit);
                return;
            }
            concurrentContext.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_SUCCESS);
            List<URLRecord> records = multiQueue.pop(concurrentUnit, increaseConnections);
            acquiredConnections = records.size();
            releaseConnections = acquiredConnections;
            returnConnections = increaseConnections - acquiredConnections;
            if (returnConnections > 0) concurrentManager.decreaseConnections(concurrentUnit, returnConnections);
            for (URLRecord record : records) {
                Context recordContext = new Context();
                try {
                    record.status = Constants.URL_STATUS_FETCHING;
                    record.outQueueTime = System.currentTimeMillis();
                    if (!urlService.updateQueueTime(record)) logger.warn("update url record[{}] failed", record.key);
                    byte[] bytes = JSON.toJSONString(record, SerializerFeature.DisableCircularReferenceDetect).
                            getBytes(Charset.forName("UTF-8"));
                    String tags = String.format("%d", record.category == null ?
                            Constants.CONTENT_CATEGORY_TEXT : record.category);
                    Message message = new Message(config.topic, tags, record.key, bytes);
                    SendResult sendResult = recordProducer.send(message);
                    if (sendResult == null || StringUtils.isEmpty(sendResult.getMessageId())) {
                        logger.warn("send record[{}] failed", record.key);
                        continue;
                    }
                    recordContext.put(Constants.DARWIN_MESSAGE_ID, sendResult.getMessageId());
                    recordContext.put(Constants.DARWIN_MESSAGE_KEY, record.key);
                    concurrentManager.putConnectionRecord(concurrentUnit, record.key);
                    releaseConnections--;
                } catch (Exception ex) {
                    recordContext.put(Constants.DARWIN_DEBUG_MESSAGE, ex.getMessage());
                    recordContext.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(ex));
                    logger.error("process record failed for key[{}]", record.key);
                    logger.error(ex.getMessage(), ex);
                } finally {
                    commitAspectLog(recordContext, record);
                }
            }
        } catch (Exception e) {
            concurrentContext.put(Constants.SCHEDULE_STATUS, Constants.SCHEDULE_STATUS_FAIL);
            concurrentContext.put(Constants.DARWIN_DEBUG_MESSAGE, e.getMessage());
            concurrentContext.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("process concurrent unit[{}] failed while scheduling", concurrentUnit);
            logger.error(e.getMessage(), e);
        } finally {
            commitAspectLog(concurrentContext, concurrentUnit, increaseConnections, acquiredConnections,
                    returnConnections, releaseConnections);
            if (releaseConnections > 0) concurrentManager.decreaseConnections(concurrentUnit, releaseConnections);
        }
    }

    /**
     * 等待
     */
    private void waitMoment() {
        try {
            Thread.sleep(config.scheduleTimeIntervalMs);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * 提交调度URL记录切面日志
     *
     * @param context 上下文
     * @param record URL记录
     */
    private void commitAspectLog(Context context, URLRecord record) {
        if (record == null || aspectLogger == null) return;
        DarwinUtil.putContext(context, record);
    }

    /**
     * 提交并发单元切面日志
     *
     * @param concurrentContext 上下文
     * @param concurrentUnit 并发单元
     * @param increaseConnections 申请增加连接数
     * @param acquiredConnections 获取连接数
     * @param returnConnections 归还连接数
     * @param releaseConnections 释放连接数
     */
    private void commitAspectLog(Context concurrentContext, String concurrentUnit, int increaseConnections,
                                 int acquiredConnections, int returnConnections, int releaseConnections) {
        if (aspectLogger == null) return;
        concurrentContext.put(Constants.DARWIN_RECORD_TYPE, Constants.RECORD_TYPE_CONCURRENT_UNIT);
        concurrentContext.put(Constants.CONCURRENT_UNIT, concurrentUnit);
        concurrentContext.put(Constants.INCREASE_CONNECTION_NUM, increaseConnections);
        concurrentContext.put(Constants.ACQUIRE_CONNECTION_NUM, acquiredConnections);
        concurrentContext.put(Constants.RETURN_CONNECTION_NUM, returnConnections);
        concurrentContext.put(Constants.RELEASE_CONNECTION_NUM, releaseConnections);
        aspectLogger.commit(concurrentContext.getFeatureMap());
    }
}
