package xin.manong.darwin.queue.multi;

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

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 多级队列：用于爬虫URL调度
 * 1. 以并发单元(Host/Domain)维度进行调度，保证每次每个并发单元调度一定数量URL抓取
 * 2. 维护爬虫任务，感知爬虫任务URL抓取状态
 *
 * @author frankcl
 * @date 2023-03-07 11:41:14
 */
public class MultiQueue {

    private static final Logger logger = LoggerFactory.getLogger(MultiQueue.class);

    private MultiQueueConfig config;
    private RSet<String> jobs;
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
        RedisMemory redisMemory = getCurrentMemory();
        if (redisMemory == null) return false;
        return redisMemory.maxMemoryBytes == 0 || redisMemory.usedMemoryRssBytes * 1.0d /
                redisMemory.maxMemoryBytes >= config.maxUsedMemoryRatio;
    }

    /**
     * 获取当前内存等级
     *
     * @return 当前内存等级
     */
    public int getCurrentMemoryLevel() {
        RedisMemory redisMemory = getCurrentMemory();
        if (redisMemory == null) return MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_NORMAL;
        double memoryRatio = redisMemory.maxMemoryBytes == 0 ? 0d : redisMemory.usedMemoryRssBytes * 1.0d /
                redisMemory.maxMemoryBytes;
        if (memoryRatio < config.warnUsedMemoryRatio) return MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_NORMAL;
        else if (memoryRatio < config.maxUsedMemoryRatio) return MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_WARN;
        return MultiQueueConstants.MULTI_QUEUE_MEMORY_LEVEL_REFUSED;
    }

    /**
     * 获取当前redis内存情况
     *
     * @return redis内存，如果不支持返回null
     */
    public RedisMemory getCurrentMemory() {
        return redisClient.getMemoryInfo();
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
    public RSetCache<String> currentConcurrentUnits() {
        if (concurrentUnits != null) return concurrentUnits;
        concurrentUnits = redisClient.getRedissonClient().getSetCache(
                MultiQueueConstants.MULTI_QUEUE_CONCURRENT_UNITS);
        return concurrentUnits;
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
            urlQueue.isExistsAsync();
        }
        BatchResult result = batch.execute();
        List<Boolean> responses = result.getResponses();
        for (Boolean response : responses) if (response) return false;
        RSetCache<String> concurrentUnits = currentConcurrentUnits();
        concurrentUnits.add(concurrentUnit, config.maxConcurrentUnitExpiredTimeSeconds, TimeUnit.SECONDS);
        logger.info("remove concurrent unit[{}] in {} seconds from global concurrent unit set", concurrentUnit,
                config.maxConcurrentUnitExpiredTimeSeconds);
        return true;
    }

    /**
     * 获取当前任务集合
     *
     * @return 任务集合
     */
    public RSet<String> currentJobs() {
        if (jobs != null) return jobs;
        jobs = redisClient.getRedissonClient().getSet(MultiQueueConstants.MULTI_QUEUE_JOBS);
        return jobs;
    }

    /**
     * 清理任务过期URL记录
     *
     * @param jobId 任务ID
     * @param maxTimeIntervalMs 最大时间间隔（毫秒）
     * @return 清理数据列表
     */
    public List<URLRecord> sweepExpiredJobRecords(String jobId, long maxTimeIntervalMs) {
        List<URLRecord> sweepRecords = new ArrayList<>();
        if (StringUtils.isEmpty(jobId)) {
            logger.warn("job id is empty");
            return sweepRecords;
        }
        String jobRecordMapKey = String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_JOB_RECORD, jobId);
        RMap<String, URLRecord> jobRecordMap = redisClient.getRedissonClient().getMap(jobRecordMapKey, codec);
        Iterator<Map.Entry<String, URLRecord>> iterator = jobRecordMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, URLRecord> entry = iterator.next();
            URLRecord record = entry.getValue();
            long timeIntervalMs = System.currentTimeMillis() - record.inQueueTime;
            if (timeIntervalMs <= maxTimeIntervalMs) continue;
            iterator.remove();
            String concurrentUnitQueueKey = buildConcurrentUnitQueueKey(record);
            RBlockingQueue<URLRecord> queue = redisClient.getRedissonClient().
                    getBlockingQueue(concurrentUnitQueueKey, codec);
            if (queue != null) queue.remove(record);
            sweepRecords.add(record);
            logger.info("record[{}] is expired for url[{}] of job[{}]", record.key, record.url, record.jobId);
        }
        return sweepRecords;
    }

    /**
     * 判断任务数据是否为空
     * 如果任务对应的抓取记录为空返回true，反之返回false
     *
     * @param jobId 任务ID
     * @return 为空返回true，否则返回false
     */
    public boolean isEmptyJobRecordMap(String jobId) {
        if (StringUtils.isEmpty(jobId)) {
            logger.warn("job id is empty");
            return false;
        }
        String jobRecordMapKey = String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_JOB_RECORD, jobId);
        RMap<String, URLRecord> jobRecordMap = redisClient.getRedissonClient().getMap(jobRecordMapKey, codec);
        return jobRecordMap.isEmpty();
    }

    /**
     * 删除任务记录映射
     *
     * @param jobId 任务ID
     */
    public void deleteJobRecordMap(String jobId) {
        if (StringUtils.isEmpty(jobId)) {
            logger.warn("job id is empty");
            return;
        }
        String jobRecordMapKey = String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_JOB_RECORD, jobId);
        RSet<String> jobs = currentJobs();
        if (jobs.contains(jobId)) jobs.remove(jobId);
        RMap<String, URLRecord> jobRecordMap = redisClient.getRedissonClient().getMap(jobRecordMapKey, codec);
        jobRecordMap.delete();
        logger.info("delete job record map[{}] success", jobRecordMapKey);
    }

    /**
     * 从任务记录映射中移除URL数据
     *
     * @param record URL数据
     */
    public void removeFromJobRecordMap(URLRecord record) {
        if (record == null) return;
        if (StringUtils.isEmpty(record.jobId)) {
            logger.warn("job id is empty");
            return;
        }
        String jobRecordMapKey = String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_JOB_RECORD, record.jobId);
        RMap<String, URLRecord> jobRecordMap = redisClient.getRedissonClient().getMap(jobRecordMapKey, codec);
        if (!jobRecordMap.containsKey(record.key)) return;
        jobRecordMap.remove(record.key);
        logger.info("delete record[{}] success from job record map[{}]", record.key, record.jobId);
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
     *
     * @param record URL数据
     * @return 状态
     */
    public MultiQueueStatus push(URLRecord record) {
        if (record == null || !record.check()) {
            logger.error("record is null or is invalid");
            return MultiQueueStatus.ERROR;
        }
        String concurrentUnit = ConcurrentUnitComputer.compute(record);
        if (StringUtils.isEmpty(concurrentUnit)) {
            logger.error("get concurrent unit failed for url[{}]", record.url);
            return MultiQueueStatus.ERROR;
        }
        record.inQueueTime = System.currentTimeMillis();
        String jobRecordMapKey = String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_JOB_RECORD, record.jobId);
        String concurrentUnitQueueKey = buildConcurrentUnitQueueKey(record);
        RBlockingQueue<URLRecord> queue = redisClient.getRedissonClient().
                getBlockingQueue(concurrentUnitQueueKey, codec);
        int queueSize = queue.size();
        if (config.maxQueueSize > 0 && queueSize >= config.maxQueueSize) {
            logger.warn("queue is full for concurrent unit[{}]", concurrentUnit);
            return MultiQueueStatus.FULL;
        }
        BatchOptions batchOptions = BatchOptions.defaults().
                executionMode(BatchOptions.ExecutionMode.IN_MEMORY_ATOMIC).
                responseTimeout(3, TimeUnit.SECONDS).
                retryInterval(2, TimeUnit.SECONDS).retryAttempts(3);
        RBatch batch = redisClient.getRedissonClient().createBatch(batchOptions);
        try {
            batch.getBlockingQueue(concurrentUnitQueueKey, codec).offerAsync(record);
            batch.getMap(jobRecordMapKey, codec).putAsync(record.key, record);
            batch.getSetCache(MultiQueueConstants.MULTI_QUEUE_CONCURRENT_UNITS).addAsync(
                    concurrentUnit, 0, TimeUnit.SECONDS);
            batch.getSet(MultiQueueConstants.MULTI_QUEUE_JOBS).addAsync(record.jobId);
            BatchResult result = batch.execute();
            List responses = result.getResponses();
            if (!((Boolean) responses.get(0))) throw new MultiQueueException("push record failed");
            return MultiQueueStatus.OK;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            BatchOptions rollbackOptions = BatchOptions.defaults().skipResult().
                    executionMode(BatchOptions.ExecutionMode.IN_MEMORY_ATOMIC).
                    responseTimeout(3, TimeUnit.SECONDS).
                    retryInterval(2, TimeUnit.SECONDS).retryAttempts(3);
            RBatch rollbackBatch = redisClient.getRedissonClient().createBatch(rollbackOptions);
            rollbackBatch.getMap(jobRecordMapKey, codec).removeAsync(record.key);
            rollbackBatch.getBlockingQueue(concurrentUnitQueueKey, codec).removeAsync(record);
            rollbackBatch.execute();
            record.inQueueTime = null;
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
