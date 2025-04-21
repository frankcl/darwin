package xin.manong.darwin.queue;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
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
 * 并发队列：用于爬虫URL调度
 * 以并发单元(Host/Domain)维度进行调度
 * 保证针对同一并发单元每次调度一定数量URL进行抓取
 *
 * @author frankcl
 * @date 2023-03-07 11:41:14
 */
public class ConcurrencyQueue {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyQueue.class);

    private final ConcurrencyQueueConfig config;
    private RSetCache<String> concurrentUnits;

    /**
     * 使用snappy压缩节省URLRecord内存占用空间
     */
    protected Codec codec;
    @Resource
    protected RedisClient redisClient;

    public ConcurrencyQueue(ConcurrencyQueueConfig config) {
        this.config = config;
        this.codec = new SnappyCodecV2();
    }

    /**
     * 计算Redis内存使用率
     *
     * @return Redis内存使用率
     */
    private double calculateRedisMemoryUseRatio() {
        RedisMemory redisMemory = redisClient.getMemoryInfo();
        if (redisMemory == null) {
            logger.warn("not support to get redis memory");
            return 0d;
        }
        long maxMemoryBytes = redisMemory.maxMemoryBytes == 0L ?
                redisMemory.totalSystemMemoryBytes : redisMemory.maxMemoryBytes;
        return maxMemoryBytes == 0L ? 0d : redisMemory.usedMemoryRssBytes * 1.0d / maxMemoryBytes;
    }

    /**
     * 获取当前并发单元集合
     *
     * @return 并发单元(host或domain)集合
     */
    private RSetCache<String> concurrentUnits() {
        if (concurrentUnits != null) return concurrentUnits;
        concurrentUnits = redisClient.getRedissonClient().getSetCache(
                ConcurrencyConstants.CONCURRENT_UNITS);
        return concurrentUnits;
    }

    /**
     * 构建并发队列key列表，包含高中低3个优先级队列
     *
     * @param concurrentUnit 并发单元(host或domain)
     * @return 并发队列key列表
     */
    private List<String> buildConcurrentQueueKeys(String concurrentUnit) {
        List<String> concurrentQueueKeys = new ArrayList<>();
        concurrentQueueKeys.add(String.format("%s%s",
                ConcurrencyConstants.CONCURRENT_HIGH_PRIORITY, concurrentUnit));
        concurrentQueueKeys.add(String.format("%s%s",
                ConcurrencyConstants.CONCURRENT_NORMAL_PRIORITY, concurrentUnit));
        concurrentQueueKeys.add(String.format("%s%s",
                ConcurrencyConstants.CONCURRENT_LOW_PRIORITY, concurrentUnit));
        return concurrentQueueKeys;
    }

    /**
     * 根据URL记录构建并发队列key
     *
     * @param record URL记录
     * @return 并发队列key
     */
    private String buildConcurrentQueueKey(URLRecord record) {
        String concurrentUnit = ConcurrentUnitComputer.compute(record);
        String concurrentQueueKey = String.format("%s%s",
                ConcurrencyConstants.CONCURRENT_NORMAL_PRIORITY, concurrentUnit);
        if (record.priority != null && record.priority == Constants.PRIORITY_HIGH) {
            concurrentQueueKey = String.format("%s%s",
                    ConcurrencyConstants.CONCURRENT_HIGH_PRIORITY, concurrentUnit);
        } else if (record.priority != null && record.priority == Constants.PRIORITY_LOW) {
            concurrentQueueKey = String.format("%s%s",
                    ConcurrencyConstants.CONCURRENT_LOW_PRIORITY, concurrentUnit);
        }
        return concurrentQueueKey;
    }

    /**
     * 从并发队列中弹出URL记录
     *
     * @param queue 并发队列
     * @param n 弹出数量
     * @return URL记录列表
     */
    private List<URLRecord> pop(RBlockingQueue<URLRecord> queue, int n) {
        List<URLRecord> records = new ArrayList<>();
        while (true) {
            URLRecord record = queue.poll();
            if (record == null) break;
            record.popTime = System.currentTimeMillis();
            records.add(record);
            if (records.size() >= n) break;
        }
        return records;
    }

    /**
     * 删除URL数据以回滚push操作
     *
     * @param concurrentQueueKey 并发队列key
     * @param record URL记录
     */
    private void rollback(String concurrentQueueKey, URLRecord record) {
        BatchOptions batchOptions = buildBatchOptions(true);
        RBatch batch = redisClient.getRedissonClient().createBatch(batchOptions);
        batch.getBlockingQueue(concurrentQueueKey, codec).removeAsync(record);
        batch.execute();
    }

    /**
     * 构建批处理选项
     *
     * @param skipResult 是否跳过结果
     * @return 批处理选项
     */
    private BatchOptions buildBatchOptions(boolean skipResult) {
        BatchOptions batchOptions = BatchOptions.defaults().
                executionMode(BatchOptions.ExecutionMode.IN_MEMORY_ATOMIC).
                responseTimeout(3, TimeUnit.SECONDS).
                retryInterval(2, TimeUnit.SECONDS).
                retryAttempts(3);
        if (skipResult) batchOptions.skipResult();
        return batchOptions;
    }

    /**
     * 判断是否可以推送数据
     * 内存使用率超过阈值则拒绝数据推送
     *
     * @return 可以推送返回true，否则返回false
     */
    public boolean canPush() {
        double redisMemoryUseRatio = calculateRedisMemoryUseRatio();
        return redisMemoryUseRatio < config.dangerMemoryUsedRatio;
    }

    /**
     * 获取Redis内存信息
     *
     * @return Redis内存信息
     */
    public RedisMemory getRedisMemory() {
        return redisClient.getMemoryInfo();
    }

    /**
     * 获取内存水位等级
     *
     * @return 内存水位等级
     */
    public int getMemoryWaterLevel() {
        double redisMemoryUseRatio = calculateRedisMemoryUseRatio();
        if (redisMemoryUseRatio < config.waringMemoryUsedRatio) return ConcurrencyConstants.MEMORY_WATER_LEVEL_NORMAL;
        else if (redisMemoryUseRatio < config.dangerMemoryUsedRatio) return ConcurrencyConstants.MEMORY_WATER_LEVEL_WARNING;
        return ConcurrencyConstants.MEMORY_WATER_LEVEL_DANGER;
    }

    /**
     * 获取多级队列插入数据锁
     *
     * @return 获取成功返回true，否则返回false
     */
    public boolean acquirePushLock() {
        return redisClient.tryLock(ConcurrencyConstants.CONCURRENT_PUSH_LOCK, null);
    }

    /**
     * 释放多级队列插入数据锁
     */
    public void releasePushLock() {
        redisClient.unlock(ConcurrencyConstants.CONCURRENT_PUSH_LOCK);
    }

    /**
     * 获取多级队列弹出数据锁
     *
     * @return 获取成功返回true，否则返回false
     */
    public boolean acquirePopLock() {
        return redisClient.tryLock(ConcurrencyConstants.CONCURRENT_POP_LOCK, null);
    }

    /**
     * 释放多级队列弹出数据锁
     */
    public void releasePopLock() {
        redisClient.unlock(ConcurrencyConstants.CONCURRENT_POP_LOCK);
    }

    /**
     * 获取当前并发单元集合快照
     *
     * @return 当前并发单元集合快照
     */
    public Set<String> concurrentUnitsSnapshots() {
        RSetCache<String> concurrentUnits = concurrentUnits();
        return new HashSet<>(concurrentUnits);
    }

    /**
     * 移除并发单元
     * 如果并发队列中存在记录则不能移除并发单元
     *
     * @param concurrentUnit 并发单元(host或domain)
     * @return 成功返回true，否则返回false
     */
    public boolean removeConcurrentUnit(String concurrentUnit) {
        List<String> concurrentQueueKeys = buildConcurrentQueueKeys(concurrentUnit);
        BatchOptions batchOptions = buildBatchOptions(false);
        RBatch batch = redisClient.getRedissonClient().createBatch(batchOptions);
        for (String concurrentQueueKey : concurrentQueueKeys) {
            batch.getBlockingQueue(concurrentQueueKey, codec).sizeAsync();
        }
        BatchResult<?> result = batch.execute();
        List<?> responses = result.getResponses();
        for (Object response : responses) if (response != null && (Integer) response > 0) return false;
        RSetCache<String> concurrentUnits = concurrentUnits();
        concurrentUnits.remove(concurrentUnit);
        logger.info("remove concurrent unit:{} success", concurrentUnit);
        return true;
    }

    /**
     * 获取并发单元排队URL数量
     *
     * @param concurrentUnit 并发单元
     * @return 排队URL数量
     */
    public int queuingRecordSize(String concurrentUnit) {
        int total = 0;
        if (StringUtils.isEmpty(concurrentUnit)) return total;
        List<String> concurrentQueueKeys = buildConcurrentQueueKeys(concurrentUnit);
        for (String concurrentQueueKey : concurrentQueueKeys) {
            RBlockingQueue<URLRecord> queue = redisClient.getRedissonClient().
                    getBlockingQueue(concurrentQueueKey, codec);
            total += queue.size();
        }
        return total;
    }

    /**
     * 从并发队列移除URL记录
     *
     * @param record URL记录
     */
    public void remove(URLRecord record) {
        String concurrentQueueKey = buildConcurrentQueueKey(record);
        RBlockingQueue<URLRecord> queue = redisClient.getRedissonClient().
                getBlockingQueue(concurrentQueueKey, codec);
        if (queue == null) return;
        if (!queue.remove(record)) logger.warn("Remove record failed from queue for url:{}", record.url);
    }

    /**
     * 从并发队列中获取指定数量URL记录
     *
     * @param concurrentUnit 并发单元(host或domain)
     * @param n 弹出数量
     * @return URL记录列表
     */
    public List<URLRecord> pop(String concurrentUnit, int n) {
        List<URLRecord> records = new ArrayList<>();
        if (StringUtils.isEmpty(concurrentUnit)) return records;
        List<String> concurrentQueueKeys = buildConcurrentQueueKeys(concurrentUnit);
        for (String concurrentQueueKey : concurrentQueueKeys) {
            RBlockingQueue<URLRecord> queue = redisClient.getRedissonClient().
                    getBlockingQueue(concurrentQueueKey, codec);
            records.addAll(pop(queue, n - records.size()));
            if (records.size() >= n) break;
        }
        return records;
    }

    /**
     * 推送多条数据进入多级队列
     *
     * @param records URL列表
     * @return 推送结果
     */
    public List<PushResult> push(@NotNull List<URLRecord> records) {
        List<PushResult> pushResults = new ArrayList<>();
        for (URLRecord record : records) pushResults.add(push(record));
        return pushResults;
    }

    /**
     * 推送数据到多级队列，重试retryCnt
     *
     * @param record URL数据
     * @param retryCnt 重试次数
     * @return 结果
     */
    public PushResult push(URLRecord record, int retryCnt) {
        PushResult pushResult = PushResult.ERROR;
        for (int i = 0; i < retryCnt; i++) {
            pushResult = push(record);
            if (pushResult != PushResult.FULL) return pushResult;
        }
        return pushResult;
    }

    /**
     * 推送数据到多级队列
     *
     * @param record URL数据
     * @return 结果
     */
    public PushResult push(@NotNull URLRecord record) {
        String concurrentUnit = ConcurrentUnitComputer.compute(record);
        record.pushTime = System.currentTimeMillis();
        String concurrentQueueKey = buildConcurrentQueueKey(record);
        RBlockingQueue<URLRecord> queue = redisClient.getRedissonClient().
                getBlockingQueue(concurrentQueueKey, codec);
        int queueSize = queue.size();
        if (config.maxQueueCapacity > 0 && queueSize >= config.maxQueueCapacity) {
            logger.warn("queue is full for concurrent unit:{}", concurrentUnit);
            record.status = Constants.URL_STATUS_QUEUE_FULL;
            return PushResult.FULL;
        }
        BatchOptions batchOptions = buildBatchOptions(false);
        RBatch batch = redisClient.getRedissonClient().createBatch(batchOptions);
        try {
            record.status = Constants.URL_STATUS_QUEUING;
            batch.getBlockingQueue(concurrentQueueKey, codec).offerAsync(record);
            batch.getSetCache(ConcurrencyConstants.CONCURRENT_UNITS).addAsync(
                    concurrentUnit, 0, TimeUnit.SECONDS);
            BatchResult<?> result = batch.execute();
            List<?> responses = result.getResponses();
            if (responses.size() != 2 || !((Boolean) responses.get(0))) throw new IllegalStateException("push failed");
            return PushResult.SUCCESS;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollback(concurrentQueueKey, record);
            record.pushTime = null;
            record.status = Constants.URL_STATUS_ERROR;
            return PushResult.ERROR;
        }
    }
}
