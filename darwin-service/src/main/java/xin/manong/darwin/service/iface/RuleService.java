package xin.manong.darwin.service.iface;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.request.RuleSearchRequest;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 规则服务接口定义
 *
 * @author frankcl
 * @date 2023-03-21 17:53:04
 */
public abstract class RuleService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    protected Cache<Long, Optional<Rule>> ruleCache;

    public RuleService() {
        CacheBuilder<Long, Optional<Rule>> builder = CacheBuilder.newBuilder()
                .recordStats()
                .concurrencyLevel(1)
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .removalListener(n -> onRemoval(n));
        ruleCache = builder.build();
    }

    /**
     * 缓存移除通知
     *
     * @param notification 移除通知
     */
    private void onRemoval(RemovalNotification<Long, Optional<Rule>> notification) {
        if (!notification.getValue().isPresent()) return;
        logger.info("rule[{}] is removed from cache", notification.getValue().get().id);
    }

    /**
     * 从cache获取规则
     *
     * @param ruleId 规则ID
     * @return 任务信息，如果不存在返回null
     */
    public Rule getCache(Long ruleId) {
        try {
            Optional<Rule> optional = ruleCache.get(ruleId, () -> {
                Rule rule = get(ruleId);
                return Optional.ofNullable(rule);
            });
            if (!optional.isPresent()) {
                ruleCache.invalidate(ruleId);
                return null;
            }
            return optional.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加规则
     *
     * @param rule 规则
     * @return 添加成功返回true，否则返回false
     */
    public abstract Boolean add(Rule rule);

    /**
     * 更新规则
     *
     * @param rule 规则
     * @return 更新成功返回true，否则返回false
     */
    public abstract Boolean update(Rule rule);

    /**
     * 根据ID删除规则
     *
     * @param id 规则ID
     * @return 删除成功返回true，否则返回false
     */
    public abstract Boolean delete(Long id);

    /**
     * 根据ID获取规则
     *
     * @param id 规则ID
     * @return 存在返回规则，否则返回null
     */
    public abstract Rule get(Long id);

    /**
     * 搜索规则列表
     *
     * @param searchRequest 搜索请求
     * @return 搜索列表
     */
    public abstract Pager<Rule> search(RuleSearchRequest searchRequest);

    /**
     * 判断URL是否匹配规则
     *
     * @param record URL记录
     * @param rule 规则
     * @return 匹配返回true，否则返回false
     */
    public abstract Boolean match(URLRecord record, Rule rule);
}
