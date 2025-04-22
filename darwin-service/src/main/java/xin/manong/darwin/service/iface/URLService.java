package xin.manong.darwin.service.iface;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.common.model.URLGroupCount;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.component.ExcelDocumentExporter;
import xin.manong.darwin.service.config.CacheConfig;
import xin.manong.darwin.service.request.OrderByRequest;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

import java.io.IOException;
import java.io.Serial;
import java.util.*;
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

    protected CacheConfig cacheConfig;
    protected Cache<String, Optional<String>> keyCache;
    protected Cache<String, Optional<URLRecord>> recordCache;
    protected static List<String> EXPORT_COLUMNS = new ArrayList<>() {
        @Serial
        private static final long serialVersionUID = 3442274479484811098L;

        {
        add("key");
        add("url");
        add("redirect_url");
        add("parent_url");
        add("fetch_content_url");
        add("job_id");
        add("plan_id");
        add("fetch_time");
        add("status");
        add("http_code");
        add("user_defined_map");
        add("field_map");
    }};

    public URLService(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        keyCache = CacheBuilder.newBuilder()
                .recordStats()
                .concurrencyLevel(1)
                .maximumSize(cacheConfig.urlCacheNum)
                .expireAfterWrite(cacheConfig.urlExpiredMinutes, TimeUnit.MINUTES)
                .removalListener(this::onKeyRemoval).build();
        recordCache = CacheBuilder.newBuilder()
                .recordStats()
                .concurrencyLevel(1)
                .maximumSize(cacheConfig.urlCacheNum)
                .expireAfterWrite(cacheConfig.urlExpiredMinutes, TimeUnit.MINUTES)
                .removalListener(this::onRecordRemoval).build();
    }

    /**
     * URL数据缓存移除通知
     *
     * @param notification 移除通知
     */
    private void onRecordRemoval(RemovalNotification<String, Optional<URLRecord>> notification) {
        Objects.requireNonNull(notification.getValue());
        if (notification.getValue().isEmpty()) return;
        logger.info("Record:{} is removed from cache", notification.getValue().get().url);
    }

    /**
     * key缓存移除通知
     *
     * @param notification 移除通知
     */
    private void onKeyRemoval(RemovalNotification<String, Optional<String>> notification) {
        Objects.requireNonNull(notification.getValue());
        if (notification.getValue().isEmpty()) return;
        logger.info("Key:{} is removed from cache", notification.getValue().get());
    }

    /**
     * 根据URL从cache获取记录
     *
     * @param url URL
     * @return 数据记录，如果不存在返回null
     */
    public URLRecord getCacheByURL(String url) {
        try {
            String hash = DigestUtils.md5Hex(url);
            Optional<String> keyOptional = keyCache.get(hash, () -> {
                URLRecord record = getLatestByURL(url, System.currentTimeMillis() - 86400000L);
                return Optional.ofNullable(record == null ? null : record.key);
            });
            if (keyOptional.isEmpty()) {
                keyCache.invalidate(hash);
                return null;
            }
            String key = keyOptional.get();
            Optional<URLRecord> recordOptional = recordCache.get(key, () -> Optional.ofNullable(get(key)));
            if (recordOptional.isEmpty()) {
                recordCache.invalidate(key);
                keyCache.invalidate(hash);
                return null;
            }
            return recordOptional.get();
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
    public abstract boolean add(URLRecord record);

    /**
     * 更新抓取结果
     *
     * @param record 抓取结果
     * @return 更新成功返回true，否则返回false
     */
    public abstract boolean updateContent(URLRecord record);

    /**
     * 更新入队出队时间
     *
     * @param record URL记录
     * @return 更新成功返回true，否则返回false
     */
    public abstract boolean updateQueueTime(URLRecord record);

    /**
     * 更新URL状态
     *
     * @param key URL key
     * @param status 状态
     * @return 更新成功返回true，否则返回false
     */
    public abstract boolean updateStatus(String key, int status);

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
    public abstract boolean delete(String key);

    /**
     * 搜索URL列表
     *
     * @param searchRequest 搜索请求
     * @return 搜索列表
     */
    public abstract Pager<URLRecord> search(URLSearchRequest searchRequest);

    /**
     * 获取数量
     *
     * @param searchRequest 搜索请求
     * @return 数量
     */
    public abstract long selectCount(URLSearchRequest searchRequest);

    /**
     * 根据分组统计任务抓取的数据状态
     *
     * @param jobId 任务ID
     * @return 统计结果
     */
    public abstract List<URLGroupCount> bucketCountGroupByStatus(String jobId);

    /**
     * 准备搜索请求
     *
     * @param searchRequest 搜索请求
     * @return 搜索请求
     */
    protected URLSearchRequest prepareSearchRequest(URLSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new URLSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        List<Integer> statusList = ModelValidator.validateListField(searchRequest.status, Integer.class);
        if (statusList != null && !statusList.isEmpty()) searchRequest.statusList = statusList;
        RangeValue<Long> rangeValue = ModelValidator.validateRangeValue(searchRequest.fetchTime, Long.class);
        if (rangeValue != null) searchRequest.fetchTimeRange = rangeValue;
        rangeValue = ModelValidator.validateRangeValue(searchRequest.createTime, Long.class);
        if (rangeValue != null) searchRequest.createTimeRange = rangeValue;
        return searchRequest;
    }

    /**
     * 根据URL获取在afterTime之后的最新抓取成功记录
     *
     * @param url URL
     * @param afterTime 起始时间
     * @return 成功返回数据记录，否则返回null
     */
    public URLRecord getLatestByURL(String url, long afterTime) {
        OrderByRequest orderByRequest = new OrderByRequest("fetch_time", false);
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.url = url;
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_SUCCESS);
        searchRequest.fetchTimeRange = new RangeValue<>();
        searchRequest.fetchTimeRange.includeLower = true;
        searchRequest.fetchTimeRange.start = afterTime;
        searchRequest.orderByRequests = new ArrayList<>();
        searchRequest.orderByRequests.add(orderByRequest);
        searchRequest.current = searchRequest.size = 1;
        Pager<URLRecord> pager = search(searchRequest);
        if (pager == null || pager.records.isEmpty()) return null;
        return pager.records.get(0);
    }

    /**
     * 获取指定任务的创建时间小于等于beforeTime的URL记录
     *
     * @param jobId 任务ID
     * @param beforeTime 最小创建时间
     * @param size 数量
     * @return URL列表
     */
    public List<URLRecord> getExpiredRecords(String jobId, long beforeTime, int size) {
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.current = 1;
        searchRequest.size = size <= 0 ? Constants.DEFAULT_PAGE_SIZE : size;
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_CREATED);
        searchRequest.statusList.add(Constants.URL_STATUS_QUEUING);
        searchRequest.statusList.add(Constants.URL_STATUS_FETCHING);
        searchRequest.createTimeRange = new RangeValue<>();
        searchRequest.createTimeRange.end = beforeTime;
        searchRequest.createTimeRange.includeUpper = true;
        searchRequest.jobId = jobId;
        Pager<URLRecord> pager = search(searchRequest);
        if (pager == null || pager.records.isEmpty()) return new ArrayList<>();
        return pager.records;
    }

    /**
     * 导出URL
     * 最多导出10000条记录
     *
     * @param searchRequest 搜索请求
     * @return 成功返回ExcelDocumentExporter实例，否则返回null
     * @throws IOException I/O异常
     */
    public ExcelDocumentExporter export(URLSearchRequest searchRequest) throws IOException {
        int pageSize = 100;
        if (searchRequest == null) searchRequest = new URLSearchRequest();
        searchRequest.current = 1;
        searchRequest.size = pageSize;
        ExcelDocumentExporter exporter = new ExcelDocumentExporter();
        String sheetName = "下载数据";
        exporter.buildSheet(sheetName, EXPORT_COLUMNS);
        int exportCount = 0;
        while (true) {
            Pager<URLRecord> pager = search(searchRequest);
            for (URLRecord record : pager.records) {
                Map<String, Object> data = JSON.parseObject(JSON.toJSONString(record));
                exporter.add(sheetName, data);
                if (++exportCount >= 10000) break;
            }
            if (pager.records.size() < pageSize) break;
            searchRequest.current++;
        }
        return exporter;
    }
}
