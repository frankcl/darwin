package xin.manong.darwin.queue;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.*;
import org.redisson.client.codec.Codec;
import org.redisson.codec.SnappyCodecV2;
import org.redisson.transaction.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.base.redis.RedisClient;
import xin.manong.weapon.base.redis.RedisMemory;
import xin.manong.weapon.base.util.CommonUtil;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 多级队列：用于爬虫URL调度
 * 1. 以Host维度进行调度，保证每次每个Host调度一定数量URL抓取
 * 2. 维护爬虫任务，感知爬虫任务URL抓取状态
 *
 * @author frankcl
 * @date 2023-03-07 11:41:14
 */
public class MultiQueue {

    private static final Logger logger = LoggerFactory.getLogger(MultiQueue.class);

    private double maxUsedMemoryRatio;

    /**
     * 使用snappy压缩节省URLRecord内存占用空间
     */
    protected Codec codec;
    @Resource
    protected RedisClient redisClient;

    public MultiQueue(double maxUsedMemoryRatio) {
        this.maxUsedMemoryRatio = maxUsedMemoryRatio;
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
                redisMemory.maxMemoryBytes >= maxUsedMemoryRatio;
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
     * 获取当前站点集合
     *
     * @return 站点集合
     */
    public Set<String> hostInQueue() {
        return redisClient.getRedissonClient().getSet(MultiQueueConstants.MULTI_QUEUE_HOSTS_KEY);
    }

    /**
     * 获取当前任务集合
     *
     * @return 任务集合
     */
    public Set<String> jobInQueue() {
        return redisClient.getRedissonClient().getSet(MultiQueueConstants.MULTI_QUEUE_JOBS_KEY);
    }

    /**
     * 清理任务中过期URL记录
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
        String jobMapKey = String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_JOB_KEY_PREFIX, jobId);
        RMap<String, URLRecord> jobMap = redisClient.getRedissonClient().getMap(jobMapKey, codec);
        Iterator<Map.Entry<String, URLRecord>> iterator = jobMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, URLRecord> entry = iterator.next();
            URLRecord record = entry.getValue();
            long timeIntervalMs = System.currentTimeMillis() - record.inQueueTime;
            if (timeIntervalMs <= maxTimeIntervalMs) continue;
            iterator.remove();
            sweepRecords.add(record);
            logger.info("record[{}] is expired for url[{}] and job[{}]", record.key, record.url, record.jobId);
        }
        return sweepRecords;
    }

    /**
     * 从全局站点集合中移除host
     *
     * @param host 站点
     */
    public void removeHost(String host) {
        List<String> hostQueueKeys = buildHostQueueKeys(host);
        for (String hostQueueKey : hostQueueKeys) {
            RBlockingQueue<URLRecord> hostURLQueue = redisClient.getRedissonClient().
                    getBlockingQueue(hostQueueKey, codec);
            if (!hostURLQueue.isEmpty()) return;
        }
        RSetCache<String> hosts = redisClient.getRedissonClient().getSetCache(MultiQueueConstants.MULTI_QUEUE_HOSTS_KEY);
        hosts.add(host, 600, TimeUnit.SECONDS);
        logger.info("remove host[{}] in 600 seconds from global host set", host);
    }

    /**
     * 判断任务映射是否为空
     *
     * @param jobId 任务ID
     * @return 为空返回true，否则返回false
     */
    public boolean isEmptyJobMap(String jobId) {
        if (StringUtils.isEmpty(jobId)) {
            logger.warn("job id is empty");
            return false;
        }
        String jobMapKey = String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_JOB_KEY_PREFIX, jobId);
        RMap<String, URLRecord> jobMap = redisClient.getRedissonClient().getMap(jobMapKey, codec);
        return jobMap.isEmpty();
    }

    /**
     * 删除任务映射
     *
     * @param jobId 任务ID
     */
    public void deleteJobMap(String jobId) {
        if (StringUtils.isEmpty(jobId)) {
            logger.warn("job id is empty");
            return;
        }
        String jobMapKey = String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_JOB_KEY_PREFIX, jobId);
        RSet<String> jobs = redisClient.getRedissonClient().getSet(MultiQueueConstants.MULTI_QUEUE_JOBS_KEY);
        if (jobs.contains(jobId)) jobs.remove(jobId);
        RMap<String, URLRecord> jobMap = redisClient.getRedissonClient().getMap(jobMapKey, codec);
        jobMap.delete();
        logger.info("delete job map[{}] success", jobMapKey);
    }

    /**
     * 从任务映射中移除URL数据
     *
     * @param record URL数据
     */
    public void removeFromJobMap(URLRecord record) {
        if (record == null) return;
        if (StringUtils.isEmpty(record.jobId)) {
            logger.warn("job id is empty");
            return;
        }
        String jobMapKey = String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_JOB_KEY_PREFIX, record.jobId);
        RMap<String, URLRecord> jobMap = redisClient.getRedissonClient().getMap(jobMapKey, codec);
        if (!jobMap.containsKey(record.key)) return;
        jobMap.remove(record.key);
    }

    /**
     * 从指定站点队列中获取指定数量URL数据
     *
     * @param host 站点
     * @param n URL数量
     * @return URL列表
     */
    public List<URLRecord> pop(String host, int n) {
        List<URLRecord> records = new ArrayList<>();
        if (StringUtils.isEmpty(host)) return records;
        List<String> hostQueueKeys = buildHostQueueKeys(host);
        for (String hostQueueKey : hostQueueKeys) {
            RBlockingQueue<URLRecord> hostURLQueue = redisClient.getRedissonClient().
                    getBlockingQueue(hostQueueKey, codec);
            while (true) {
                URLRecord record = hostURLQueue.poll();
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
        String host = CommonUtil.getHost(record.url);
        if (StringUtils.isEmpty(host)) {
            logger.error("get host failed for url[{}]", record.url);
            return MultiQueueStatus.ERROR;
        }
        record.inQueueTime = System.currentTimeMillis();
        String jobMapKey = String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_JOB_KEY_PREFIX, record.jobId);
        String hostKey = String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_NORMAL_HOST_KEY_PREFIX, host);
        if (record.priority != null && record.priority == Constants.PRIORITY_HIGH) {
            hostKey = String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_HIGH_HOST_KEY_PREFIX, host);
        } else if (record.priority != null && record.priority == Constants.PRIORITY_LOW) {
            hostKey = String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_LOW_HOST_KEY_PREFIX, host);
        }
        RTransaction transaction = redisClient.buildTransaction();
        RSetCache<String> hosts = transaction.getSetCache(MultiQueueConstants.MULTI_QUEUE_HOSTS_KEY);
        RSet<String> jobs = transaction.getSet(MultiQueueConstants.MULTI_QUEUE_JOBS_KEY);
        RMap<String, URLRecord> jobMap = transaction.getMap(jobMapKey, codec);
        RBlockingQueue<URLRecord> hostURLQueue = redisClient.getRedissonClient().
                getBlockingQueue(hostKey, codec);
        if (!hostURLQueue.offer(record)) {
            logger.error("offer record into host queue failed");
            record.inQueueTime = null;
            return MultiQueueStatus.REFUSED;
        }
        try {
            jobMap.put(record.key, record);
            hosts.add(host, 0, TimeUnit.SECONDS);
            if (!jobs.contains(record.jobId)) jobs.add(record.jobId);
            transaction.commit();
            return MultiQueueStatus.OK;
        } catch (TransactionException e) {
            logger.error("commit transaction failed when pushing record");
            logger.error(e.getMessage(), e);
            hostURLQueue.remove(record);
            try {
                transaction.rollback();
            } catch (Exception ex) {
            }
            record.inQueueTime = null;
            return MultiQueueStatus.REFUSED;
        }
    }

    /**
     * 构建站点队列key列表
     *
     * @param host 站点
     * @return 站点队列key列表
     */
    private List<String> buildHostQueueKeys(String host) {
        List<String> hostQueueKeys = new ArrayList<>();
        hostQueueKeys.add(String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_HIGH_HOST_KEY_PREFIX, host));
        hostQueueKeys.add(String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_NORMAL_HOST_KEY_PREFIX, host));
        hostQueueKeys.add(String.format("%s%s", MultiQueueConstants.MULTI_QUEUE_LOW_HOST_KEY_PREFIX, host));
        return hostQueueKeys;
    }
}
