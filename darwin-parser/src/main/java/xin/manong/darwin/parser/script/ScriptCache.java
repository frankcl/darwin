package xin.manong.darwin.parser.script;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * groovy脚本cache
 *
 * @author frankcl
 * @date 2023-03-16 15:06:10
 */
public class ScriptCache {

    private static final Logger logger = LoggerFactory.getLogger(ScriptCache.class);

    private Cache<String, Script> cache;

    public ScriptCache(int maxSize, int expiredTimeMinutes) {
        CacheBuilder<String, Script> builder = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .maximumSize(maxSize)
                .expireAfterAccess(expiredTimeMinutes, TimeUnit.MINUTES)
                .removalListener(n -> onRemoval(n));
        cache = builder.build();
    }

    /**
     * 缓存溢出回调函数
     *
     * @param notification 移除通知
     */
    private void onRemoval(RemovalNotification<String, Script> notification) {
        Script script = notification.getValue();
        RemovalCause cause = notification.getCause();
        logger.info("{}[{}] is removed, cause[{}]", script.getClass().getSimpleName(),
                notification.getKey(), cause.name());
    }

    /**
     * 添加/更新脚本
     * 如果存在脚本，销毁原来脚本
     *
     * @param script 脚本
     */
    public void put(Script script) {
        if (script == null) {
            logger.warn("script is null for putting cache");
            return;
        }
        try {
            Script previous = cache.get(script.getKey(), () -> script);
            if (previous != null && previous != script) previous.close();
        } catch (Exception e) {
            logger.error("put script into cache failed");
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 根据key获取脚本
     *
     * @param key 脚本key
     * @return 如果存在返回脚本，否则返回null
     */
    public Script get(String key) {
        return cache.getIfPresent(key);
    }

    /**
     * 判断脚本是否改变
     * 1. 缓存中不存在脚本
     * 2. 缓存中存在脚本，并且脚本MD5改变
     *
     * @param script 脚本
     * @return 改变返回true，否则返回false
     */
    public boolean contains(Script script) {
        if (script == null) return false;
        return cache.getIfPresent(script.getKey()) != null;
    }
}
