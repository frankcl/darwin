package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.service.config.CacheConfig;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.RuleHistoryMapper;
import xin.manong.darwin.service.dao.mapper.RuleMapper;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.request.RuleSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MySQL规则服务实现
 *
 * @author frankcl
 * @date 2023-03-21 19:57:27
 */
@Service
public class RuleServiceImpl extends RuleService {

    @Resource
    private RuleMapper ruleMapper;
    @Resource
    private RuleHistoryMapper ruleHistoryMapper;

    @Autowired
    public RuleServiceImpl(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    public boolean add(Rule rule) {
        rule.version = 1;
        return ruleMapper.insert(rule) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Rule rule) {
        Rule prevRule = ruleMapper.selectById(rule.id);
        if (prevRule == null) throw new NotFoundException("规则不存在");
        if (!addHistory(new RuleHistory(prevRule))) throw new IllegalStateException("添加规则历史失败");
        rule.version = prevRule.version + 1;
        if (ruleMapper.updateById(rule) > 0) {
            ruleCache.invalidate(rule.id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Integer id) {
        Rule rule = ruleMapper.selectById(id);
        if (rule == null) throw new NotFoundException("规则不存在");
        if (!removeHistoryList(id)) throw new IllegalStateException("删除规则历史失败");
        if (ruleMapper.deleteById(id) > 0) {
            ruleCache.invalidate(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRules(String planId) {
        List<Rule> rules = getRules(planId);
        if (rules == null || rules.isEmpty()) return true;
        for (Rule rule : rules) {
            if (!delete(rule.id)) throw new IllegalStateException("删除规则失败");
        }
        return true;
    }

    @Override
    public Rule get(Integer id) {
        return ruleMapper.selectById(id);
    }

    @Override
    public List<Rule> getRules(String planId) {
        QueryWrapper<Rule> query = new QueryWrapper<Rule>().select("id", "name", "regex");
        query.eq("plan_id", planId);
        query.orderByDesc("create_time");
        return ruleMapper.selectList(query);
    }

    @Override
    public List<Integer> getRuleIds(String planId) {
        QueryWrapper<Rule> query = new QueryWrapper<Rule>().select("id");
        query.eq("plan_id", planId);
        return ruleMapper.selectList(query).stream().map(rule -> rule.id).collect(Collectors.toList());
    }

    @Override
    public Pager<Rule> search(RuleSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new RuleSearchRequest();
        if (searchRequest.pageNum == null || searchRequest.pageNum < 1) searchRequest.pageNum = Constants.DEFAULT_PAGE_NUM;
        if (searchRequest.pageSize == null || searchRequest.pageSize <= 0) searchRequest.pageSize = Constants.DEFAULT_PAGE_SIZE;
        ModelValidator.validateOrderBy(Rule.class, searchRequest);
        QueryWrapper<Rule> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (searchRequest.scriptType != null) query.eq("script_type", searchRequest.scriptType);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like("name", searchRequest.name);
        IPage<Rule> page = ruleMapper.selectPage(new Page<>(searchRequest.pageNum, searchRequest.pageSize), query);
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
    public boolean removeHistoryList(Integer ruleId) {
        LambdaQueryWrapper<RuleHistory> query = new LambdaQueryWrapper<>();
        query.eq(RuleHistory::getRuleId, ruleId);
        if (ruleHistoryMapper.selectCount(query) == 0) return true;
        return ruleHistoryMapper.delete(query) > 0;
    }

    @Override
    public RuleHistory getHistory(Integer id) {
        return ruleHistoryMapper.selectById(id);
    }

    @Override
    public Pager<RuleHistory> getHistoryList(Integer ruleId, int pageNum, int pageSize) {
        pageNum = pageNum < 1 ? Constants.DEFAULT_PAGE_NUM : pageNum;
        pageSize = pageSize <= 0 ? Constants.DEFAULT_PAGE_SIZE : pageSize;
        LambdaQueryWrapper<RuleHistory> query = new LambdaQueryWrapper<>();
        query.orderByDesc(RuleHistory::getCreateTime);
        query.eq(RuleHistory::getRuleId, ruleId);
        IPage<RuleHistory> page = ruleHistoryMapper.selectPage(new Page<>(pageNum, pageSize), query);
        return Converter.convert(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollback(Integer ruleId, Integer ruleHistoryId, String modifier) {
        RuleHistory ruleHistory = getHistory(ruleHistoryId);
        if (ruleHistory == null) throw new NotFoundException("规则历史不存在");
        if (!ruleHistory.ruleId.equals(ruleId)) throw new IllegalStateException("变更历史不属于此规则");
        Rule rule = new Rule();
        rule.id = ruleId;
        rule.regex = ruleHistory.regex;
        rule.script = ruleHistory.script;
        rule.scriptType = ruleHistory.scriptType;
        rule.changeLog = ruleHistory.changeLog;
        rule.modifier = modifier;
        return update(rule);
    }
}
