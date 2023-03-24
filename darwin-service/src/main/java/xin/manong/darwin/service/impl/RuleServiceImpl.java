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
import java.util.regex.Pattern;

/**
 * MySQL规则服务实现
 *
 * @author frankcl
 * @date 2023-03-21 19:57:27
 */
@Service
public class RuleServiceImpl implements RuleService {

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
            throw new RuntimeException(String.format("规则[%d]不存在", rule.id));
        }
        return ruleMapper.updateById(rule) > 0;
    }

    @Override
    public Boolean delete(Long id) {
        if (ruleMapper.selectById(id) == null) {
            logger.error("rule[{}] is not found", id);
            throw new RuntimeException(String.format("规则[%d]不存在", id));
        }
        return ruleMapper.deleteById(id) > 0;
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
    public Pager<Rule> search(RuleSearchRequest searchRequest, int current, int size) {
        LambdaQueryWrapper<Rule> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Rule::getCreateTime);
        if (searchRequest != null) {
            if (searchRequest.category != null) query.eq(Rule::getCategory, searchRequest.category);
            if (searchRequest.ruleGroup != null) query.eq(Rule::getRuleGroup, searchRequest.ruleGroup);
            if (searchRequest.scriptType != null) query.eq(Rule::getScriptType, searchRequest.scriptType);
            if (!StringUtils.isEmpty(searchRequest.domain)) query.eq(Rule::getDomain, searchRequest.domain);
            if (!StringUtils.isEmpty(searchRequest.name)) query.like(Rule::getName, searchRequest.name);
        }
        IPage<Rule> page = ruleMapper.selectPage(new Page<>(current, size), query);
        return Converter.convert(page);
    }

    @Override
    public Boolean match(URLRecord record, Rule rule) {
        if (record == null || StringUtils.isEmpty(record.url)) {
            logger.error("url is empty");
            return false;
        }
        if (rule.category != null && rule.category == Constants.RULE_CATEGORY_GLOBAL_LINK_FOLLOW &&
            record.category != null && record.category == Constants.CONTENT_CATEGORY_LIST) {
            return true;
        }
        if (rule == null || StringUtils.isEmpty(rule.regex)) {
            logger.error("match rule is null");
            return false;
        }
        Pattern pattern = Pattern.compile(rule.regex);
        return pattern.matcher(record.url).matches();
    }
}
