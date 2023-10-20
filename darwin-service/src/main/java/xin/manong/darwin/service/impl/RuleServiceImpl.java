package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.RuleMapper;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.request.RuleSearchRequest;

import javax.annotation.Resource;
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
    protected RuleMapper ruleMapper;

    @Override
    public Boolean add(Rule rule) {
        LambdaQueryWrapper<Rule> query = new LambdaQueryWrapper<>();
        query.eq(Rule::getName, rule.name).eq(Rule::getRuleGroup, rule.ruleGroup);
        if (ruleMapper.selectCount(query) > 0) {
            logger.error("rule has existed for name[{}] and group[{}]", rule.name, rule.ruleGroup);
            throw new RuntimeException(String.format("分组[%d]下同名规则[%s]已存在", rule.ruleGroup, rule.name));
        }
        return ruleMapper.insert(rule) > 0;
    }

    @Override
    public Boolean update(Rule rule) {
        if (ruleMapper.selectById(rule.id) == null) {
            logger.error("rule[{}] is not found", rule.id);
            return false;
        }
        int n = ruleMapper.updateById(rule);
        if (n > 0) ruleCache.invalidate(rule.id);
        return n > 0;
    }

    @Override
    public Boolean delete(Long id) {
        if (ruleMapper.selectById(id) == null) {
            logger.error("rule[{}] is not found", id);
            return false;
        }
        int n = ruleMapper.deleteById(id);
        if (n > 0) ruleCache.invalidate(id);
        return n > 0;
    }

    @Override
    public Rule get(Long id) {
        if (id == null) {
            logger.error("rule id is null");
            throw new RuntimeException("规则ID为空");
        }
        return ruleMapper.selectById(id);
    }

    @Override
    public List<Rule> batchGet(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<Rule> rules = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch countDownLatch = new CountDownLatch(ids.size());
        ids.stream().parallel().forEach(id -> {
            try {
                Rule rule = ruleMapper.selectById(id);
                if (rule != null) rules.add(rule);
            } catch (Exception e) {
                logger.error("exception occurred for getting rule[{}]", id);
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
        LambdaQueryWrapper<Rule> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Rule::getCreateTime);
        if (searchRequest.category != null) query.eq(Rule::getCategory, searchRequest.category);
        if (searchRequest.ruleGroup != null) query.eq(Rule::getRuleGroup, searchRequest.ruleGroup);
        if (searchRequest.scriptType != null) query.eq(Rule::getScriptType, searchRequest.scriptType);
        if (!StringUtils.isEmpty(searchRequest.domain)) query.eq(Rule::getDomain, searchRequest.domain);
        if (!StringUtils.isEmpty(searchRequest.name)) query.like(Rule::getName, searchRequest.name);
        IPage<Rule> page = ruleMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }

    @Override
    public Boolean match(URLRecord record, Rule rule) {
        if (record == null || StringUtils.isEmpty(record.url)) {
            logger.error("url is empty");
            return false;
        }
        if (rule.category != null && rule.category == Constants.RULE_CATEGORY_GLOBAL_LINK &&
                (record.category == null || (record.category != null &&
                        record.category == Constants.CONTENT_CATEGORY_LIST))) {
            return true;
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
