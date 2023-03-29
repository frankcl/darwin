package xin.manong.darwin.service.iface;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.request.JobSearchRequest;

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

    protected Cache<String, Optional<Job>> jobCache;

    public JobService() {
        CacheBuilder<String, Optional<Job>> builder = CacheBuilder.newBuilder()
                .recordStats()
                .concurrencyLevel(1)
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .removalListener(n -> onRemoval(n));
        jobCache = builder.build();
    }

    /**
     * 缓存移除通知
     *
     * @param notification 移除通知
     */
    private void onRemoval(RemovalNotification<String, Optional<Job>> notification) {
        if (!notification.getValue().isPresent()) return;
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
                return Optional.of(job);
            });
            if (!optional.isPresent()) jobCache.invalidate(jobId);
            return optional.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
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
    public abstract Boolean add(Job job);

    /**
     * 更新任务
     *
     * @param job 任务信息
     * @return 成功返回true，否则返回false
     */
    public abstract Boolean update(Job job);

    /**
     * 删除任务
     *
     * @param jobId 任务ID
     * @return 成功返回true，否则返回false
     */
    public abstract Boolean delete(String jobId);

    /**
     * 搜索任务列表
     *
     * @param searchRequest 搜索请求
     * @param current 页码，从1开始
     * @param size 每页数量
     * @return 分页列表
     */
    public abstract Pager<Job> search(JobSearchRequest searchRequest, int current, int size);
}
