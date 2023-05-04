package xin.manong.darwin.queue.concurrent;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RMap;
import org.redisson.client.codec.Codec;
import org.redisson.codec.SnappyCodecV2;
import xin.manong.weapon.base.redis.RedisClient;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接并发管理器：控制站点并发
 *
 * @author frankcl
 * @date 2023-03-09 17:30:39
 */
public class ConcurrentManager {

    private static final String CONCURRENT_COUNT_PREFIX = "DARWIN_CONCURRENT_COUNT";
    private static final String CONCURRENT_RECORD_PREFIX = "DARWIN_CONCURRENT_RECORD";

    private int maxConcurrentConnectionNum;
    private int concurrentConnectionTtlSecond;
    private Map<String, ConcurrentConnectionCount> concurrentConnectionCountMap;
    private Map<String, Integer> concurrentUnitMaxConnectionMap;
    /**
     * 使用snappy压缩节省URLRecord内存占用空间
     */
    protected Codec codec;
    @Resource
    protected RedisClient redisClient;

    public ConcurrentManager(Map<String, Integer> concurrentUnitMaxConnectionMap,
                             int maxConcurrentConnectionNum,
                             int concurrentConnectionTtlSecond) {
        this.concurrentUnitMaxConnectionMap = concurrentUnitMaxConnectionMap;
        this.maxConcurrentConnectionNum = maxConcurrentConnectionNum;
        this.concurrentConnectionTtlSecond = concurrentConnectionTtlSecond;
        this.concurrentConnectionCountMap = new ConcurrentHashMap<>();
        this.codec = new SnappyCodecV2();
    }

    /**
     * 增加并发单元连接记录
     *
     * @param concurrentUnit 并发单元
     * @param key 连接key
     */
    public void putConnectionRecord(String concurrentUnit, String key) {
        String redisKey = String.format("%s_%s", CONCURRENT_RECORD_PREFIX, concurrentUnit);
        RMap<String, Long> connectionRecordMap = redisClient.getRedissonClient().getMap(redisKey, codec);
        connectionRecordMap.put(key, System.currentTimeMillis());
        connectionRecordMap.expireAsync(Duration.ofSeconds(concurrentConnectionTtlSecond));
    }

    /**
     * 移除并发单元连接记录
     *
     * @param concurrentUnit 并发单元
     * @param key 连接key
     * @return 成功返回true，否则返回false
     */
    public boolean removeConnectionRecord(String concurrentUnit, String key) {
        String redisKey = String.format("%s_%s", CONCURRENT_RECORD_PREFIX, concurrentUnit);
        RMap<String, Long> connectionRecordMap = redisClient.getRedissonClient().getMap(redisKey, codec);
        boolean result = connectionRecordMap.remove(key) != null;
        connectionRecordMap.expireAsync(Duration.ofSeconds(concurrentConnectionTtlSecond));
        return result;
    }

    /**
     * 获取并发单元可用连接数
     *
     * @param concurrentUnit 并发单元
     * @return 可用连接数
     */
    public int getAvailableConnectionCount(String concurrentUnit) {
        String redisKey = String.format("%s_%s", CONCURRENT_RECORD_PREFIX, concurrentUnit);
        RMap<String, Long> connectionRecordMap = redisClient.getRedissonClient().getMap(redisKey, codec);
        int size = connectionRecordMap == null ? 0 : connectionRecordMap.size();
        int availableCount = getMaxConcurrentConnectionNum(concurrentUnit) - size;
        return availableCount < 0 ? 0 : availableCount;
    }

    /**
     * 获取并发单元连接记录map
     *
     * @param concurrentUnit 并发单元
     * @return 并发单元连接记录map
     */
    public Map<String, Long> getConnectionRecordMap(String concurrentUnit) {
        String redisKey = String.format("%s_%s", CONCURRENT_RECORD_PREFIX, concurrentUnit);
        return redisClient.getRedissonClient().getMap(redisKey, codec);
    }

    /**
     * 获取并发单元当前可用连接数
     *
     * @param concurrentUnit 并发单元
     * @return 当前可用连接数
     */
    public int getAvailableConnections(String concurrentUnit) {
        ConcurrentConnectionCount concurrentConnectionCount = getConcurrentConnectionCount(concurrentUnit);
        int availableConnections = (int) (getMaxConcurrentConnectionNum(concurrentUnit) -
                concurrentConnectionCount.connectionCount.get());
        return availableConnections < 0 ? 0 : availableConnections;
    }

    /**
     * 减少并发单元当前连接数
     *
     * @param concurrentUnit 并发单元
     * @param connectionNum 减少连接数
     * @return 实际减少连接数
     */
    public int decreaseConnections(String concurrentUnit, int connectionNum) {
        if (connectionNum <= 0) return 0;
        int decreasedConnections = connectionNum;
        ConcurrentConnectionCount concurrentConnectionCount = getConcurrentConnectionCount(concurrentUnit);
        RAtomicLong connectionCount = concurrentConnectionCount.connectionCount;
        while (true) {
            long currentConnectionCount = connectionCount.get();
            if (currentConnectionCount <= 0) return 0;
            long updateConnectionCount = currentConnectionCount - connectionNum;
            if (updateConnectionCount <= 0) {
                decreasedConnections = (int) currentConnectionCount;
                updateConnectionCount = 0;
            }
            if (connectionCount.compareAndSet(currentConnectionCount, updateConnectionCount)) {
                concurrentConnectionCount.expiredTime = System.currentTimeMillis() +
                        concurrentConnectionTtlSecond * 1000L;
                connectionCount.expireAsync(Duration.ofSeconds(concurrentConnectionTtlSecond));
                break;
            }
        }
        return decreasedConnections;
    }

    /**
     * 增加并发单元当前连接数
     *
     * @param concurrentUnit 并发单元
     * @param connectionNum 增加连接数
     * @return 实际增加连接数
     */
    public int increaseConnections(String concurrentUnit, int connectionNum) {
        int increasedConnections = connectionNum;
        int maxConcurrentConnectionNum = getMaxConcurrentConnectionNum(concurrentUnit);
        ConcurrentConnectionCount concurrentConnectionCount = getConcurrentConnectionCount(concurrentUnit);
        RAtomicLong connectionCount = concurrentConnectionCount.connectionCount;
        while (true) {
            long currentConnectionCount = connectionCount.get();
            long updateConnectionCount = currentConnectionCount + connectionNum;
            if (updateConnectionCount >= maxConcurrentConnectionNum) {
                increasedConnections = (int) (maxConcurrentConnectionNum - currentConnectionCount);
                updateConnectionCount = maxConcurrentConnectionNum;
            }
            if (increasedConnections <= 0) return 0;
            if (connectionCount.compareAndSet(currentConnectionCount, updateConnectionCount)) {
                concurrentConnectionCount.expiredTime = System.currentTimeMillis() +
                        concurrentConnectionTtlSecond * 1000L;
                connectionCount.expireAsync(Duration.ofSeconds(concurrentConnectionTtlSecond));
                break;
            }
        }
        return increasedConnections;
    }

    /**
     * 获取并发单元当前连接计数
     *
     * @param concurrentUnit 并发单元
     * @return 连接计数
     */
    private ConcurrentConnectionCount getConcurrentConnectionCount(String concurrentUnit) {
        if (concurrentConnectionCountMap.containsKey(concurrentUnit)) {
            ConcurrentConnectionCount concurrentConnectionCount = concurrentConnectionCountMap.get(concurrentUnit);
            long updateTime = concurrentConnectionCount.expiredTime - concurrentConnectionTtlSecond * 1000L / 2;
            if (System.currentTimeMillis() >= updateTime) {
                concurrentConnectionCount.expiredTime = System.currentTimeMillis() +
                        concurrentConnectionTtlSecond * 1000L;
                concurrentConnectionCount.connectionCount.expireAsync(
                        Duration.ofSeconds(concurrentConnectionTtlSecond));
            }
            return concurrentConnectionCount;
        }
        synchronized (this) {
            if (concurrentConnectionCountMap.containsKey(concurrentUnit)) {
                return concurrentConnectionCountMap.get(concurrentUnit);
            }
            String redisKey = String.format("%s_%s", CONCURRENT_COUNT_PREFIX, concurrentUnit);
            RAtomicLong connectionCount = redisClient.getRedissonClient().getAtomicLong(redisKey);
            if (connectionCount.get() <= 0L) connectionCount.set(0);
            connectionCount.expireAsync(Duration.ofSeconds(concurrentConnectionTtlSecond));
            ConcurrentConnectionCount concurrentConnectionCount = new ConcurrentConnectionCount(
                    connectionCount, System.currentTimeMillis() + concurrentConnectionTtlSecond * 1000L);
            concurrentConnectionCountMap.put(concurrentUnit, concurrentConnectionCount);
            return concurrentConnectionCount;
        }
    }

    /**
     * 根据并发单元获取最大并发连接数量
     *
     * @param concurrentUnit 并发单元
     * @return 最大并发连接数量
     */
    private int getMaxConcurrentConnectionNum(String concurrentUnit) {
        if (concurrentUnitMaxConnectionMap.containsKey(concurrentUnit)) {
            return concurrentUnitMaxConnectionMap.get(concurrentUnit);
        }
        return maxConcurrentConnectionNum;
    }
}
