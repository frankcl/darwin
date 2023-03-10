package xin.manong.darwin.queue.concurrent;

import org.redisson.api.RAtomicLong;
import xin.manong.weapon.base.redis.RedisClient;
import xin.manong.weapon.base.util.FP63;

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

    private static final String CONCURRENT_CONNECTION_PREFIX = "DARWIN_CONCURRENT_";

    private int maxConcurrentConnectionNum;
    private int concurrentConnectionTtlSecond;
    private Map<Long, ConcurrentConnectionCount> concurrentConnectionCountMap;
    private Map<String, Integer> concurrentUnitMaxConnectionMap;
    @Resource
    protected RedisClient redisClient;

    public ConcurrentManager(Map<String, Integer> concurrentUnitMaxConnectionMap,
                             int maxConcurrentConnectionNum,
                             int concurrentConnectionTtlSecond) {
        this.concurrentUnitMaxConnectionMap = concurrentUnitMaxConnectionMap;
        this.maxConcurrentConnectionNum = maxConcurrentConnectionNum;
        this.concurrentConnectionTtlSecond = concurrentConnectionTtlSecond;
        this.concurrentConnectionCountMap = new ConcurrentHashMap<>();
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
            if (connectionCount.compareAndSet(currentConnectionCount, updateConnectionCount)) break;
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
            if (connectionCount.compareAndSet(currentConnectionCount, updateConnectionCount)) break;
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
        Long key = FP63.newFP63(concurrentUnit);
        if (concurrentConnectionCountMap.containsKey(key)) {
            ConcurrentConnectionCount concurrentConnectionCount = concurrentConnectionCountMap.get(key);
            long updateTime = concurrentConnectionCount.expiredTime - concurrentConnectionTtlSecond * 1000L / 2;
            if (System.currentTimeMillis() >= updateTime) {
                concurrentConnectionCount.connectionCount.expireAsync(
                        Duration.ofSeconds(concurrentConnectionTtlSecond));
            }
            return concurrentConnectionCount;
        }
        synchronized (this) {
            if (concurrentConnectionCountMap.containsKey(key)) {
                return concurrentConnectionCountMap.get(key);
            }
            String redisKey = String.format("%s%s", CONCURRENT_CONNECTION_PREFIX, concurrentUnit);
            RAtomicLong connectionCount = redisClient.getRedissonClient().getAtomicLong(redisKey);
            connectionCount.expireAsync(Duration.ofSeconds(concurrentConnectionTtlSecond));
            connectionCount.set(0);
            ConcurrentConnectionCount concurrentConnectionCount = new ConcurrentConnectionCount(
                    connectionCount, System.currentTimeMillis() + concurrentConnectionTtlSecond * 1000L);
            concurrentConnectionCountMap.put(key, concurrentConnectionCount);
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
