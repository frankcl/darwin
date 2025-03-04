package xin.manong.darwin.service.iface;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.RuleHistory;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.config.CacheConfig;
import xin.manong.darwin.service.request.RuleSearchRequest;

import java.util.List;
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

    private static final Logger logger = LoggerFactory.getLogger(RuleService.class);

    protected CacheConfig cacheConfig;
    protected Cache<Integer, Optional<Rule>> ruleCache;

    public RuleService(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        CacheBuilder<Integer, Optional<Rule>> builder = CacheBuilder.newBuilder()
                .recordStats()
                .concurrencyLevel(1)
                .maximumSize(cacheConfig.ruleCacheNum)
                .expireAfterWrite(cacheConfig.ruleExpiredMinutes, TimeUnit.MINUTES)
                .removalListener(this::onRemoval);
        ruleCache = builder.build();
    }

    /**
     * 缓存移除通知
     *
     * @param notification 移除通知
     */
    private void onRemoval(RemovalNotification<Integer, Optional<Rule>> notification) {
        assert notification.getValue() != null;
        if (notification.getValue().isEmpty()) return;
        logger.info("rule[{}] is removed from cache", notification.getValue().get().id);
    }

    /**
     * 从cache获取规则
     *
     * @param ruleId 规则ID
     * @return 任务信息，如果不存在返回null
     */
    public Rule getCache(Integer ruleId) {
        try {
            Optional<Rule> optional = ruleCache.get(ruleId, () -> {
                Rule rule = get(ruleId);
                return Optional.ofNullable(rule);
            });
            if (optional.isEmpty()) {
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
    public abstract boolean add(Rule rule);

    /**
     * 更新规则
     *
     * @param rule 规则
     * @return 更新成功返回true，否则返回false
     */
    public abstract boolean update(Rule rule);

    /**
     * 根据ID删除规则
     *
     * @param id 规则ID
     * @return 删除成功返回true，否则返回false
     */
    public abstract boolean delete(Integer id);

    /**
     * 根据ID获取规则
     *
     * @param id 规则ID
     * @return 存在返回规则，否则返回null
     */
    public abstract Rule get(Integer id);

    /**
     * 批量获取规则
     *
     * @param ids 规则ID列表
     * @return 规则列表
     */
    public abstract List<Rule> batchGet(List<Integer> ids);

    /**
     * 搜索规则列表
     *
     * @param searchRequest 搜索请求
     * @return 搜索列表
     */
    public abstract Pager<Rule> search(RuleSearchRequest searchRequest);

    /**
     * 添加规则历史
     *
     * @param ruleHistory 规则历史
     * @return 成功返回true，否则返回false
     */
    public abstract boolean addHistory(RuleHistory ruleHistory);

    /**
     * 移除规则历史
     *
     * @param id 规则历史ID
     * @return 成功返回true，否则返回false
     */
    public abstract boolean removeHistory(Integer id);

    /**
     * 移除规则所有关联历史
     *
     * @param ruleId 规则ID
     * @return 成功返回true，否则返回false
     */
    public abstract boolean removeAllHistory(Integer ruleId);

    /**
     * 获取规则历史
     *
     * @param id 规则历史ID
     * @return 成功返回规则历史信息，否则返回null
     */
    public abstract RuleHistory getRuleHistory(Integer id);

    /**
     * 列表规则历史
     *
     * @param ruleId 规则ID
     * @param current 页码，从1开始
     * @param size 分页数量
     *
     * @return 规则历史分页列表
     */
    public abstract Pager<RuleHistory> listHistory(Integer ruleId, int current, int size);

    /**
     * 使用规则历史回滚规则
     *
     * @param ruleId 规则ID
     * @param ruleHistoryId 规则历史ID
     * @return 成功返回true，否则返回false
     */
    public abstract boolean rollBack(Integer ruleId, Integer ruleHistoryId);

    /**
     * 判断URL是否匹配规则
     *
     * @param record URL记录
     * @param rule 规则
     * @return 匹配返回true，否则返回false
     */
    public abstract boolean match(URLRecord record, Rule rule);

    /**
     * 获取URL匹配规则数量
     *
     * @param record URL记录
     * @param rules 规则列表
     * @return 匹配规则数量
     */
    public abstract int matchRuleCount(URLRecord record, List<Rule> rules);
}
