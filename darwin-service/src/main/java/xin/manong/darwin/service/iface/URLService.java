package xin.manong.darwin.service.iface;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.FetchRecord;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.request.URLSearchRequest;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * URL服务接口定义
 *
 * @author frankcl
 * @date 2023-03-20 20:00:56
 */
public abstract class URLService {

    private static final Logger logger = LoggerFactory.getLogger(URLService.class);

    protected Cache<String, Optional<URLRecord>> recordCache;

    public URLService() {
        CacheBuilder<String, Optional<URLRecord>> builder = CacheBuilder.newBuilder()
                .recordStats()
                .concurrencyLevel(1)
                .maximumSize(100)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .removalListener(n -> onRemoval(n));
        recordCache = builder.build();
    }

    /**
     * 缓存移除通知
     *
     * @param notification 移除通知
     */
    private void onRemoval(RemovalNotification<String, Optional<URLRecord>> notification) {
        if (!notification.getValue().isPresent()) return;
        logger.info("url record[{}] is removed from cache", notification.getValue().get().url);
    }

    /**
     * 从cache获取URL记录
     *
     * @param url URL
     * @return URL记录，如果不存在返回null
     */
    public URLRecord getCache(String url) {
        try {
            String hash = DigestUtils.md5Hex(url);
            Optional<URLRecord> optional = recordCache.get(hash, () -> {
                URLSearchRequest searchRequest = new URLSearchRequest();
                searchRequest.url = url;
                searchRequest.status = Constants.URL_STATUS_SUCCESS;
                searchRequest.fetchTime = new RangeValue<>();
                searchRequest.fetchTime.start = System.currentTimeMillis() - 86400000L;
                Pager<URLRecord> pager = search(searchRequest, 1, 1);
                return Optional.ofNullable(pager.records.size() > 0 ? pager.records.get(0) : null);
            });
            if (!optional.isPresent()) {
                recordCache.invalidate(hash);
                return null;
            }
            return optional.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加URL记录
     *
     * @param record url记录
     * @return 添加成功返回true，否则返回false
     */
    public abstract Boolean add(URLRecord record);

    /**
     * 更新抓取结果
     *
     * @param fetchRecord 抓取结果
     * @return 更新成功返回true，否则返回false
     */
    public abstract Boolean updateWithFetchRecord(FetchRecord fetchRecord);

    /**
     * 更新入队出队时间
     *
     * @param record URL记录
     * @return 更新成功返回true，否则返回false
     */
    public abstract Boolean updateQueueTime(URLRecord record);

    /**
     * 更新URL状态
     *
     * @param key URL key
     * @param status 状态
     * @return 更新成功返回true，否则返回false
     */
    public abstract Boolean updateStatus(String key, int status);

    /**
     * 根据key获取URL记录
     *
     * @param key 唯一key
     * @return URL记录，无记录返回null
     */
    public abstract URLRecord get(String key);

    /**
     * 根据key删除URL记录
     *
     * @param key 唯一key
     * @return 成功返回true，否则返回false
     */
    public abstract Boolean delete(String key);

    /**
     * 搜索URL列表
     *
     * @param searchRequest 搜索请求
     * @param current 页码，从1开始
     * @param size 每页数量
     * @return 搜索列表
     */
    public abstract Pager<URLRecord> search(URLSearchRequest searchRequest, int current, int size);
}
