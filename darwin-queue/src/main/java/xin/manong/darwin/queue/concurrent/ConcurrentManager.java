package xin.manong.darwin.queue.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.weapon.base.redis.RedisClient;

import javax.annotation.Resource;

/**
 * 连接并发管理器：控制站点并发
 *
 * @author frankcl
 * @date 2023-03-09 17:30:39
 */
public class ConcurrentManager {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentManager.class);

    private int concurrentConnectionNum;
    @Resource
    private RedisClient redisClient;

    public ConcurrentManager(int concurrentConnectionNum) {
        this.concurrentConnectionNum = concurrentConnectionNum;
    }
}
