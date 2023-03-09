package xin.manong.darwin.queue.concurrent;

import org.redisson.api.RAtomicLong;

/**
 * 并发连接计数
 *
 * @author frankcl
 * @date 2023-03-09 20:18:08
 */
public class ConcurrentConnectionCount {

    public Long expiredTime;
    public RAtomicLong connectionCount;

    public ConcurrentConnectionCount(RAtomicLong connectionCount, Long expiredTime) {
        this.connectionCount = connectionCount;
        this.expiredTime = expiredTime;
    }
}
