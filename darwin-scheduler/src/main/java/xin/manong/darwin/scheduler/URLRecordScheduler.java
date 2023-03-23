package xin.manong.darwin.scheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.concurrent.ConcurrentManager;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.weapon.aliyun.ons.ONSProducer;

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
    @Resource(name = "recordProducer")
    protected ONSProducer recordProducer;

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
        int acquiredConnections = 0;
        try {
            int connections = concurrentManager.getAvailableConnections(concurrentUnit);
            if (connections <= 0) {
                logger.warn("available connections[{}] for concurrent unit[{}]", connections, concurrentUnit);
                return;
            }
            int n = concurrentManager.increaseConnections(concurrentUnit, connections);
            if (n <= 0) {
                logger.warn("acquire connections failed for concurrent unit[{}]", concurrentUnit);
                return;
            }
            List<URLRecord> records = multiQueue.pop(concurrentUnit, n);
            acquiredConnections = records.size();
            int returnConnections = n - acquiredConnections;
            if (returnConnections > 0) concurrentManager.decreaseConnections(concurrentUnit, returnConnections);
            for (URLRecord record : records) {
                record.status = Constants.URL_STATUS_FETCHING;
                record.outQueueTime = System.currentTimeMillis();
                if (!urlService.updateQueueTime(record)) logger.warn("update url record[{}] failed", record.key);
                byte[] bytes = JSON.toJSONString(record, SerializerFeature.DisableCircularReferenceDetect).
                        getBytes(Charset.forName("UTF-8"));
                Message message = new Message(config.topic, config.tags, record.key, bytes);
                SendResult sendResult = recordProducer.send(message);
                if (sendResult == null) {
                    logger.warn("send record[{}] failed", record.key);
                    continue;
                }
                concurrentManager.putConnectionRecord(concurrentUnit, record.key);
                acquiredConnections--;
            }
        } catch (Exception e) {
            logger.error("process concurrent unit[{}] failed while scheduling", concurrentUnit);
            logger.error(e.getMessage(), e);
        } finally {
            if (acquiredConnections > 0) concurrentManager.decreaseConnections(concurrentUnit, acquiredConnections);
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
}
