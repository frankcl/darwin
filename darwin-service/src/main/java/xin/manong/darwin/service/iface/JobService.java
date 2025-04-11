package xin.manong.darwin.service.iface;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.config.CacheConfig;
import xin.manong.darwin.service.request.JobSearchRequest;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 任务服务接口定义
 *
 * @author frankcl
 * @date 2023-03-15 14:29:12
 */
public abstract class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    protected CacheConfig cacheConfig;
    protected Cache<String, Optional<Job>> jobCache;
    @Resource
    protected URLService urlService;

    public JobService(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        CacheBuilder<String, Optional<Job>> builder = CacheBuilder.newBuilder()
                .recordStats()
                .concurrencyLevel(1)
                .maximumSize(cacheConfig.jobCacheNum)
                .expireAfterWrite(cacheConfig.jobExpiredMinutes, TimeUnit.MINUTES)
                .removalListener(this::onRemoval);
        jobCache = builder.build();
    }

    /**
     * 缓存移除通知
     *
     * @param notification 移除通知
     */
    private void onRemoval(RemovalNotification<String, Optional<Job>> notification) {
        assert notification.getValue() != null;
        if (notification.getValue().isEmpty()) return;
        logger.info("job[{}] is removed from cache", notification.getValue().get().jobId);
    }

    /**
     * 从cache获取任务
     *
     * @param jobId 任务ID
     * @return 任务信息，如果不存在返回null
     */
    public Job getCache(String jobId) {
        try {
            Optional<Job> optional = jobCache.get(jobId, () -> {
                Job job = get(jobId);
                return Optional.ofNullable(job);
            });
            if (optional.isEmpty()) {
                jobCache.invalidate(jobId);
                return null;
            }
            return optional.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 准备搜索请求
     *
     * @param searchRequest 搜索请求
     * @return 搜索请求
     */
    protected JobSearchRequest prepareSearchRequest(JobSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new JobSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        RangeValue<Long> rangeValue = ModelValidator.validateRangeValue(searchRequest.createTime, Long.class);
        if (rangeValue != null) searchRequest.createTimeRange = rangeValue;
        return searchRequest;
    }

    /**
     * 根据任务ID获取任务信息
     *
     * @param jobId 任务ID
     * @return 任务信息，如果不存在返回null
     */
    public abstract Job get(String jobId);

    /**
     * 添加任务
     *
     * @param job 任务信息
     * @return 成功返回true，否则返回false
     */
    public abstract boolean add(Job job);

    /**
     * 更新任务
     *
     * @param job 任务信息
     * @return 成功返回true，否则返回false
     */
    public abstract boolean update(Job job);

    /**
     * 删除任务
     *
     * @param jobId 任务ID
     * @return 成功返回true，否则返回false
     */
    public abstract boolean delete(String jobId);

    /**
     * 搜索任务列表
     *
     * @param searchRequest 搜索请求
     * @return 分页列表
     */
    public abstract Pager<Job> search(JobSearchRequest searchRequest);

    /**
     * 判断任务是否结束
     * 任务存在且任务URL不存在以下状态则任务结束
     * 1. URL_STATUS_CREATED：1
     * 2. URL_STATUS_QUEUING：3
     * 3. URL_STATUS_FETCHING：4
     *
     * @param jobId 任务ID
     * @return 结束返回true，否则返回false
     */
    public boolean finish(String jobId) {
        Job job = get(jobId);
        if (job == null) {
            logger.warn("job[{}] is not found", jobId);
            return false;
        }
        if (job.status != null && !job.status) return true;
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_CREATED);
        searchRequest.statusList.add(Constants.URL_STATUS_QUEUING);
        searchRequest.statusList.add(Constants.URL_STATUS_FETCHING);
        searchRequest.jobId = jobId;
        Pager<URLRecord> pager = urlService.search(searchRequest);
        return pager.total == 0;
    }

    /**
     * 获取创建时间在before之前的运行任务
     *
     * @param before 创建时间
     * @param size 任务数量
     * @return 任务列表
     */
    public List<Job> getRunningJobs(Long before, int size) {
        JobSearchRequest searchRequest = new JobSearchRequest();
        searchRequest.current = 1;
        searchRequest.size = size <= 0 ? Constants.DEFAULT_PAGE_SIZE : size;
        searchRequest.createTimeRange = new RangeValue<>();
        searchRequest.createTimeRange.end = before;
        searchRequest.createTimeRange.includeUpper = true;
        searchRequest.status = true;
        Pager<Job> pager = search(searchRequest);
        return pager == null || pager.records == null ? new ArrayList<>() : pager.records;
    }
}
