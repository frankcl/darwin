package xin.manong.darwin.queue;

import jakarta.annotation.Resource;
import org.redisson.api.RMap;
import org.redisson.client.codec.Codec;
import org.redisson.codec.SnappyCodecV2;
import xin.manong.weapon.base.redis.RedisClient;

import java.time.Duration;
import java.util.Map;

/**
 * 并发单元连接管理：控制并发单元最大并发数量
 *
 * @author frankcl
 * @date 2023-03-09 17:30:39
 */
public class ConcurrencyControl {

    private final int maxConcurrentConnection;
    private final int concurrentConnectionTtlSecond;
    private final Map<String, Integer> maxConcurrentConnectionMap;
    /**
     * 使用snappy压缩节省URLRecord内存占用空间
     */
    private final Codec codec;
    @Resource
    private RedisClient redisClient;

    public ConcurrencyControl(Map<String, Integer> maxConcurrentConnectionMap,
                              int maxConcurrentConnection,
                              int concurrentConnectionTtlSecond) {
        this.maxConcurrentConnectionMap = maxConcurrentConnectionMap;
        this.maxConcurrentConnection = maxConcurrentConnection;
        this.concurrentConnectionTtlSecond = concurrentConnectionTtlSecond;
        this.codec = new SnappyCodecV2();
    }

    /**
     * 增加并发单元连接
     *
     * @param concurrentUnit 并发单元
     * @param key 连接key
     */
    public void putConnection(String concurrentUnit, String key) {
        String redisKey = String.format("%s_%s", ConcurrencyConstants.CONCURRENT_CONTROL, concurrentUnit);
        RMap<String, Long> connectionRecordMap = redisClient.getRedissonClient().getMap(redisKey, codec);
        connectionRecordMap.put(key, System.currentTimeMillis());
        connectionRecordMap.expireAsync(Duration.ofSeconds(concurrentConnectionTtlSecond));
    }

    /**
     * 移除并发单元连接
     *
     * @param concurrentUnit 并发单元
     * @param key 连接key
     * @return 成功返回true，否则返回false
     */
    public boolean removeConnection(String concurrentUnit, String key) {
        String redisKey = String.format("%s_%s", ConcurrencyConstants.CONCURRENT_CONTROL, concurrentUnit);
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
    public int getAvailableConnections(String concurrentUnit) {
        String redisKey = String.format("%s_%s", ConcurrencyConstants.CONCURRENT_CONTROL, concurrentUnit);
        RMap<String, Long> connectionRecordMap = redisClient.getRedissonClient().getMap(redisKey, codec);
        int size = connectionRecordMap == null ? 0 : connectionRecordMap.size();
        int availableCount = getMaxConcurrentConnections(concurrentUnit) - size;
        return Math.max(availableCount, 0);
    }

    /**
     * 并发单元是否允许抓取
     *
     * @param concurrentUnit 并发单元
     * @return 如果可用连接数大于0则允许，否则不允许
     */
    public boolean allowFetching(String concurrentUnit) {
        return getAvailableConnections(concurrentUnit) > 0;
    }

    /**
     * 获取并发单元连接记录map
     *
     * @param concurrentUnit 并发单元
     * @return 并发单元连接记录map
     */
    public Map<String, Long> getConcurrentRecordMap(String concurrentUnit) {
        String redisKey = String.format("%s_%s", ConcurrencyConstants.CONCURRENT_CONTROL, concurrentUnit);
        return redisClient.getRedissonClient().getMap(redisKey, codec);
    }

    /**
     * 根据并发单元获取最大并发连接数量
     *
     * @param concurrentUnit 并发单元
     * @return 最大并发连接数量
     */
    public int getMaxConcurrentConnections(String concurrentUnit) {
        if (maxConcurrentConnectionMap.containsKey(concurrentUnit)) {
            return maxConcurrentConnectionMap.get(concurrentUnit);
        }
        return maxConcurrentConnection;
    }
}
