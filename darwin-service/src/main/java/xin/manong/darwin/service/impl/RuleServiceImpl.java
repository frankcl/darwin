package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.service.config.CacheConfig;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.RuleHistoryMapper;
import xin.manong.darwin.service.dao.mapper.RuleMapper;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.request.RuleSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

/**
 * MySQL规则服务实现
 *
 * @author frankcl
 * @date 2023-03-21 19:57:27
 */
@Service
public class RuleServiceImpl extends RuleService {

    private static final Logger logger = LoggerFactory.getLogger(RuleServiceImpl.class);

    @Resource
    @Lazy
    protected PlanService planService;
    @Resource
    protected RuleMapper ruleMapper;
    @Resource
    protected RuleHistoryMapper ruleHistoryMapper;

    @Autowired
    public RuleServiceImpl(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(Rule rule) {
        LambdaQueryWrapper<Rule> query = new LambdaQueryWrapper<>();
        query.eq(Rule::getName, rule.name).eq(Rule::getPlanId, rule.planId);
        if (ruleMapper.selectCount(query) > 0) throw new IllegalStateException("同名规则已存在");
        int n = ruleMapper.insert(rule);
        if (n > 0) {
            if (!addHistory(new RuleHistory(rule))) throw new IllegalStateException("添加规则历史失败");
            if (!planService.addRule(rule.planId, rule.id)) throw new IllegalStateException("添加计划规则失败");
        }
        return n > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Rule rule) {
        if (ruleMapper.selectById(rule.id) == null) throw new NotFoundException("规则不存在");
        int n = ruleMapper.updateById(rule);
        if (n > 0) {
            Rule wholeRule = get(rule.id);
            if (wholeRule != null) {
                if (!addHistory(new RuleHistory(wholeRule))) throw new IllegalStateException("添加规则历史失败");
            }
            ruleCache.invalidate(rule.id);
        }
        return n > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Integer id) {
        Rule rule = ruleMapper.selectById(id);
        if (rule == null) throw new NotFoundException("规则不存在");
        if (!planService.removeRule(rule.planId, id)) throw new IllegalStateException("移除计划规则失败");
        if (!removeAllHistory(id)) throw new IllegalStateException("删除规则历史失败");
        int n = ruleMapper.deleteById(id);
        if (n > 0) ruleCache.invalidate(id);
        return n > 0;
    }

    @Override
    public Rule get(Integer id) {
        if (id == null) throw new IllegalArgumentException("规则ID为空");
        return ruleMapper.selectById(id);
    }

    @Override
    public List<Rule> batchGet(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<Rule> rules = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch countDownLatch = new CountDownLatch(ids.size());
        ids.stream().parallel().forEach(id -> {
            try {
                Rule rule = ruleMapper.selectById(id);
                if (rule != null) rules.add(rule);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<>(rules);
    }

    @Override
    public Pager<Rule> search(RuleSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new RuleSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(Rule.class, searchRequest);
        QueryWrapper<Rule> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (searchRequest.scriptType != null) query.eq("script_type", searchRequest.scriptType);
        if (!StringUtils.isEmpty(searchRequest.domain)) query.eq("domain", searchRequest.domain);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        IPage<Rule> page = ruleMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }

    @Override
    public boolean addHistory(RuleHistory ruleHistory) {
        assert ruleHistory != null;
        ruleHistory.check();
        return ruleHistoryMapper.insert(ruleHistory) > 0;
    }

    @Override
    public boolean removeHistory(Integer id) {
        if (ruleHistoryMapper.selectById(id) == null) throw new NotFoundException("规则历史不存在");
        return ruleHistoryMapper.deleteById(id) > 0;
    }

    @Override
    public boolean removeAllHistory(Integer ruleId) {
        LambdaQueryWrapper<RuleHistory> query = new LambdaQueryWrapper<>();
        query.eq(RuleHistory::getRuleId, ruleId);
        return ruleHistoryMapper.delete(query) > 0;
    }

    @Override
    public RuleHistory getRuleHistory(Integer id) {
        if (id == null) throw new BadRequestException("规则历史ID为空");
        return ruleHistoryMapper.selectById(id);
    }

    @Override
    public Pager<RuleHistory> listHistory(Integer ruleId, int current, int size) {
        current = current < 1 ? Constants.DEFAULT_CURRENT : current;
        size = size <= 0 ? Constants.DEFAULT_PAGE_SIZE : size;
        LambdaQueryWrapper<RuleHistory> query = new LambdaQueryWrapper<>();
        query.orderByDesc(RuleHistory::getCreateTime);
        query.eq(RuleHistory::getRuleId, ruleId);
        IPage<RuleHistory> page = ruleHistoryMapper.selectPage(new Page<>(current, size), query);
        return Converter.convert(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollBack(Integer ruleId, Integer ruleHistoryId) {
        RuleHistory ruleHistory = getRuleHistory(ruleHistoryId);
        if (ruleHistory == null) throw new NotFoundException("规则历史不存在");
        if (!ruleHistory.ruleId.equals(ruleId)) throw new IllegalStateException("历史不属于此规则");
        Rule rule = new Rule();
        rule.id = ruleId;
        rule.regex = ruleHistory.regex;
        rule.script = ruleHistory.script;
        rule.scriptType = ruleHistory.scriptType;
        rule.domain = ruleHistory.domain;
        return update(rule);
    }

    @Override
    public boolean match(URLRecord record, Rule rule) {
        if (record == null || StringUtils.isEmpty(record.url)) {
            logger.error("url is empty");
            return false;
        }
        if (rule == null || StringUtils.isEmpty(rule.regex)) {
            logger.error("match rule is null");
            return false;
        }
        Pattern pattern = Pattern.compile(rule.regex);
        return pattern.matcher(record.url).matches();
    }

    @Override
    public int matchRuleCount(URLRecord record, List<Rule> rules) {
        int matchCount = 0;
        for (Rule rule : rules) if (match(record, rule)) matchCount++;
        return matchCount;
    }
}
