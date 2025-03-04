package xin.manong.darwin.queue.multi;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.*;
import org.redisson.client.codec.Codec;
import org.redisson.codec.SnappyCodecV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.computer.ConcurrentUnitComputer;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.base.redis.RedisClient;
import xin.manong.weapon.base.redis.RedisMemory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 多级队列：用于爬虫URL调度
 * 以并发单元(Host/Domain)维度进行调度，保证每次每个并发单元调度一定数量URL抓取
 *
 * @author frankcl
 * @date 2023-03-07 11:41:14
 */
public class MultiQueue {

    private static final Logger logger = LoggerFactory.getLogger(MultiQueue.class);

    private final MultiQueueConfig config;
    private RSetCache<String> concurrentUnits;

    /**
     * 使用snappy压缩节省URLRecord内存占用空间
     */
    protected Codec codec;
    @Resource
    protected RedisClient redisClient;

    public MultiQueue(MultiQueueConfig config) {
        this.config = config;
        this.codec = new SnappyCodecV2();
    }

    /**
     * 判断是否拒绝服务
     * 内存使用率超过阈值则拒绝服务
     *
     * @return 拒绝服务返回true，否则返回false
     */
    public boolean refuseService() {
        RedisMemory redisMemory = redisClient.getMemoryInfo();
        if (redisMemory == null) return false;
        return redisMemory.maxMemoryBytes == 0 || redisMemory.usedMemoryRssBytes * 1.0d /
                redisMemory.maxMemoryBytes >= config.maxUsedMemoryRatio;
    }

    /**
     * 获取内存信息
     *
     * @return 当前内存信息
     */
    public RedisMemory getMemoryInfo() {
        return redisClient.getMemoryInfo();
    }

    /**
     * 获取当前内存等级
     *
     * @return 当前内存等级
     */
    public int getCurrentMemoryLevel() {
        RedisMemory redisMemory = redisClient.getMemoryInfo();
        if (redisMemory == null) return MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_NORMAL;
        double memoryRatio = redisMemory.maxMemoryBytes == 0 ? 0d : redisMemory.usedMemoryRssBytes * 1.0d /
                redisMemory.maxMemoryBytes;
        if (memoryRatio < config.warnUsedMemoryRatio) return MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_NORMAL;
        else if (memoryRatio < config.maxUsedMemoryRatio) return MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_WARN;
        return MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_REFUSED;
    }

    /**
     * 获取多级队列出队锁
     *
     * @return 获取成功返回true，否则返回false
     */
    public boolean tryLockOutQueue() {
        return redisClient.tryLock(MultiQueueConstants.MULTI_QUEUE_OUT_QUEUE_LOCK, null);
    }

    /**
     * 释放多级队列出队锁
     */
    public void unlockOutQueue() {
        redisClient.unlock(MultiQueueConstants.MULTI_QUEUE_OUT_QUEUE_LOCK);
    }

    /**
     * 获取多级队列入队锁
     *
     * @return 获取成功返回true，否则返回false
     */
    public boolean tryLockInQueue() {
        return redisClient.tryLock(MultiQueueConstants.MULTI_QUEUE_IN_QUEUE_LOCK, null);
    }

    /**
     * 释放多级队列入队锁
     */
    public void unlockInQueue() {
        redisClient.unlock(MultiQueueConstants.MULTI_QUEUE_IN_QUEUE_LOCK);
    }

    /**
     * 获取当前并发单元集合
     *
     * @return 并发单元(host或domain)集合
     */
    private RSetCache<String> currentConcurrentUnits() {
        if (concurrentUnits != null) return concurrentUnits;
        concurrentUnits = redisClient.getRedissonClient().getSetCache(
                MultiQueueConstants.MULTI_QUEUE_CONCURRENT_UNITS);
        return concurrentUnits;
    }

    /**
     * 获取当前并发单元集合快照
     *
     * @return 当前并发单元集合快照
     */
    public Set<String> concurrentUnitsSnapshots() {
        RSetCache<String> concurrentUnits = currentConcurrentUnits();
        return new HashSet<>(concurrentUnits);
    }

    /**
     * 移除并发单元
     *
     * @param concurrentUnit 并发单元
     */
    public void removeConcurrentUnit(String concurrentUnit) {
        RSetCache<String> concurrentUnits = currentConcurrentUnits();
        if (concurrentUnits != null) concurrentUnits.remove(concurrentUnit);
    }

    /**
     * 使全局集合中并发单元过期
     * 如果并发队列中存在记录则不能过期并发单元
     *
     * @param concurrentUnit 并发单元(host或domain)
     * @return 成功返回true，否则返回false
     */
    public boolean expiredConcurrentUnit(String concurrentUnit) {
        List<String> concurrentURLQueueKeys = buildConcurrentUnitQueueKeys(concurrentUnit);
        BatchOptions batchOptions = BatchOptions.defaults().
                executionMode(BatchOptions.ExecutionMode.IN_MEMORY_ATOMIC).
                responseTimeout(3, TimeUnit.SECONDS).
                retryInterval(2, TimeUnit.SECONDS).retryAttempts(3);
        RBatch batch = redisClient.getRedissonClient().createBatch(batchOptions);
        for (String concurrentURLQueueKey : concurrentURLQueueKeys) {
            RBlockingQueueAsync<URLRecord> urlQueue = batch.getBlockingQueue(concurrentURLQueueKey, codec);
            urlQueue.sizeAsync();
        }
        BatchResult<?> result = batch.execute();
        List<?> responses = result.getResponses();
        for (Object response : responses) if (response != null && (Integer) response > 0) return false;
        RSetCache<String> concurrentUnits = currentConcurrentUnits();
        concurrentUnits.add(concurrentUnit, config.maxConcurrentUnitExpiredTimeSeconds, TimeUnit.SECONDS);
        logger.info("remove concurrent unit[{}] in {} seconds from global concurrent unit set", concurrentUnit,
                config.maxConcurrentUnitExpiredTimeSeconds);
        return true;
    }

    /**
     * 获取并发单元排队URL数量
     *
     * @param concurrentUnit 并发单元
     * @return 排队URL数量
     */
    public int getRecordSize(String concurrentUnit) {
        int size = 0;
        if (StringUtils.isEmpty(concurrentUnit)) return size;
        List<String> concurrentUnitQueueKeys = buildConcurrentUnitQueueKeys(concurrentUnit);
        for (String concurrentQueueQueueKey : concurrentUnitQueueKeys) {
            RBlockingQueue<URLRecord> urlQueue = redisClient.getRedissonClient().
                    getBlockingQueue(concurrentQueueQueueKey, codec);
            size += urlQueue.size();
        }
        return size;
    }

    /**
     * 从并发队列移除URL数据
     *
     * @param record URL数据
     */
    public void remove(URLRecord record) {
        String concurrentUnitQueueKey = buildConcurrentUnitQueueKey(record);
        RBlockingQueue<URLRecord> recordQueue = redisClient.getRedissonClient().
                getBlockingQueue(concurrentUnitQueueKey, codec);
        if (recordQueue != null) {
            if (!recordQueue.remove(record)) logger.warn("remove URL[{}] failed", record.url);
        }
    }

    /**
     * 从指定并发单元URL队列中获取指定数量URL数据
     *
     * @param concurrentUnit 并发单元(host或domain)
     * @param n URL数量
     * @return URL列表
     */
    public List<URLRecord> pop(String concurrentUnit, int n) {
        List<URLRecord> records = new ArrayList<>();
        if (StringUtils.isEmpty(concurrentUnit)) return records;
        List<String> concurrentUnitQueueKeys = buildConcurrentUnitQueueKeys(concurrentUnit);
        for (String concurrentQueueQueueKey : concurrentUnitQueueKeys) {
            RBlockingQueue<URLRecord> urlQueue = redisClient.getRedissonClient().
                    getBlockingQueue(concurrentQueueQueueKey, codec);
            while (true) {
                URLRecord record = urlQueue.poll();
                if (record == null) break;
                record.outQueueTime = System.currentTimeMillis();
                records.add(record);
                if (records.size() >= n) return records;
            }
        }
        return records;
    }

    /**
     * 推送多条数据进入多级队列
     *
     * @param records URL列表
     * @return 状态列表
     */
    public List<MultiQueueStatus> push(List<URLRecord> records) {
        if (records == null) {
            logger.error("push records are null");
            throw new RuntimeException("push records are null");
        }
        List<MultiQueueStatus> statusList = new ArrayList<>();
        for (URLRecord record : records) statusList.add(push(record));
        return statusList;
    }

    /**
     * 推送数据进入多级队列
     * 如果状态为拒绝或队列满进行重试
     *
     * @param record URL数据
     * @param retryCnt 重试次数
     * @return 状态
     */
    public MultiQueueStatus push(URLRecord record, int retryCnt) {
        MultiQueueStatus status = push(record);
        if (status == MultiQueueStatus.OK || status == MultiQueueStatus.ERROR) return status;
        for (int i = 1; i < retryCnt; i++) {
            status = push(record);
            if (status == MultiQueueStatus.OK || status == MultiQueueStatus.ERROR) return status;
        }
        return status;
    }

    /**
     * 推送数据进入多级队列
     *
     * @param record URL数据
     * @return 状态
     */
    public MultiQueueStatus push(URLRecord record) {
        if (record == null || !record.check()) {
            logger.error("record is null or is invalid");
            if (record != null) record.status = Constants.URL_STATUS_INVALID;
            return MultiQueueStatus.ERROR;
        }
        String concurrentUnit = ConcurrentUnitComputer.compute(record);
        if (StringUtils.isEmpty(concurrentUnit)) {
            logger.error("get concurrent unit failed for url[{}]", record.url);
            record.status = Constants.URL_STATUS_INVALID;
            return MultiQueueStatus.ERROR;
        }
        record.inQueueTime = System.currentTimeMillis();
        String concurrentUnitQueueKey = buildConcurrentUnitQueueKey(record);
        RBlockingQueue<URLRecord> queue = redisClient.getRedissonClient().
                getBlockingQueue(concurrentUnitQueueKey, codec);
        int queueSize = queue.size();
        if (config.maxQueueSize > 0 && queueSize >= config.maxQueueSize) {
            logger.warn("queue is full for concurrent unit[{}]", concurrentUnit);
            record.status = Constants.URL_STATUS_QUEUING_REFUSED;
            return MultiQueueStatus.FULL;
        }
        BatchOptions batchOptions = BatchOptions.defaults().
                executionMode(BatchOptions.ExecutionMode.IN_MEMORY_ATOMIC).
                responseTimeout(3, TimeUnit.SECONDS).
                retryInterval(2, TimeUnit.SECONDS).retryAttempts(3);
        RBatch batch = redisClient.getRedissonClient().createBatch(batchOptions);
        try {
            record.status = Constants.URL_STATUS_QUEUING;
            batch.getBlockingQueue(concurrentUnitQueueKey, codec).offerAsync(record);
            batch.getSetCache(MultiQueueConstants.MULTI_QUEUE_CONCURRENT_UNITS).addAsync(
                    concurrentUnit, 0, TimeUnit.SECONDS);
            BatchResult<?> result = batch.execute();
            List<?> responses = result.getResponses();
            if (responses.size() != 2 || !((Boolean) responses.get(0))) {
                throw new MultiQueueException("push record failed");
            }
            return MultiQueueStatus.OK;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            BatchOptions rollbackOptions = BatchOptions.defaults().skipResult().
                    executionMode(BatchOptions.ExecutionMode.IN_MEMORY_ATOMIC).
                    responseTimeout(3, TimeUnit.SECONDS).
                    retryInterval(2, TimeUnit.SECONDS).retryAttempts(3);
            RBatch rollbackBatch = redisClient.getRedissonClient().createBatch(rollbackOptions);
            rollbackBatch.getBlockingQueue(concurrentUnitQueueKey, codec).removeAsync(record);
            rollbackBatch.execute();
            record.inQueueTime = null;
            record.status = Constants.URL_STATUS_QUEUING_REFUSED;
            return MultiQueueStatus.REFUSED;
        }
    }

    /**
     * 构建并发单元队列key列表
     *
     * @param concurrentUnit 并发单元(host或domain)
     * @return 并发单元队列key列表
     */
    private List<String> buildConcurrentUnitQueueKeys(String concurrentUnit) {
        List<String> concurrentUnitQueueKeys = new ArrayList<>();
        concurrentUnitQueueKeys.add(String.format("%s%s",
                MultiQueueConstants.MULTI_QUEUE_HIGH_CONCURRENT_UNIT_QUEUE, concurrentUnit));
        concurrentUnitQueueKeys.add(String.format("%s%s",
                MultiQueueConstants.MULTI_QUEUE_NORMAL_CONCURRENT_UNIT_QUEUE, concurrentUnit));
        concurrentUnitQueueKeys.add(String.format("%s%s",
                MultiQueueConstants.MULTI_QUEUE_LOW_CONCURRENT_UNIT_QUEUE, concurrentUnit));
        return concurrentUnitQueueKeys;
    }

    /**
     * 构建并发单元队列key
     *
     * @param record URL数据
     * @return 并发单元队列key
     */
    private String buildConcurrentUnitQueueKey(URLRecord record) {
        String concurrentUnit = ConcurrentUnitComputer.compute(record);
        String concurrentUnitQueueKey = String.format("%s%s",
                MultiQueueConstants.MULTI_QUEUE_NORMAL_CONCURRENT_UNIT_QUEUE, concurrentUnit);
        if (record.priority != null && record.priority == Constants.PRIORITY_HIGH) {
            concurrentUnitQueueKey = String.format("%s%s",
                    MultiQueueConstants.MULTI_QUEUE_HIGH_CONCURRENT_UNIT_QUEUE, concurrentUnit);
        } else if (record.priority != null && record.priority == Constants.PRIORITY_LOW) {
            concurrentUnitQueueKey = String.format("%s%s",
                    MultiQueueConstants.MULTI_QUEUE_LOW_CONCURRENT_UNIT_QUEUE, concurrentUnit);
        }
        return concurrentUnitQueueKey;
    }
}
