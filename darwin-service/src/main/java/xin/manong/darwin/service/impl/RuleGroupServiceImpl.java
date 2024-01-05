package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.RuleGroup;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.RuleGroupMapper;
import xin.manong.darwin.service.iface.RuleGroupService;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.request.RuleSearchRequest;

import javax.annotation.Resource;

/**
 * MySQL规则分组服务实现
 *
 * @author frankcl
 * @date 2023-03-21 16:05:01
 */
@Service
public class RuleGroupServiceImpl implements RuleGroupService {

    private static final Logger logger = LoggerFactory.getLogger(RuleGroupService.class);

    @Resource
    protected RuleGroupMapper ruleGroupMapper;
    @Resource
    protected RuleService ruleService;

    @Override
    public Boolean add(RuleGroup ruleGroup) {
        LambdaQueryWrapper<RuleGroup> query = new LambdaQueryWrapper<>();
        query.eq(RuleGroup::getName, ruleGroup.name);
        if (ruleGroupMapper.selectCount(query) > 0) {
            logger.error("the same name rule group[{}] has existed", ruleGroup.name);
            throw new RuntimeException(String.format("同名规则分组[%s]已存在", ruleGroup.name));
        }
        return ruleGroupMapper.insert(ruleGroup) > 0;
    }

    @Override
    public Boolean update(RuleGroup ruleGroup) {
        if (ruleGroupMapper.selectById(ruleGroup.id) == null) {
            logger.error("rule group[{}] is not found", ruleGroup.id);
            return false;
        }
        return ruleGroupMapper.updateById(ruleGroup) > 0;
    }

    @Override
    public Boolean delete(Integer id) {
        if (ruleGroupMapper.selectById(id) == null) {
            logger.error("rule group[{}] is not found", id);
            return false;
        }
        RuleSearchRequest searchRequest = new RuleSearchRequest();
        searchRequest.current = 1;
        searchRequest.size = 1;
        searchRequest.ruleGroup = id;
        Pager<Rule> pager = ruleService.search(searchRequest);
        if (pager.total > 0) {
            logger.error("rules are not empty for rule group[{}]", id);
            throw new RuntimeException(String.format("规则分组[%d]中规则不为空", id));
        }
        return ruleGroupMapper.deleteById(id) > 0;
    }

    @Override
    public RuleGroup get(Integer id) {
        if (id == null) {
            logger.error("rule group id is null");
            throw new IllegalArgumentException("规则分组ID为空");
        }
        return ruleGroupMapper.selectById(id);
    }

    @Override
    public Pager<RuleGroup> getList(int current, int size) {
        LambdaQueryWrapper<RuleGroup> query = new LambdaQueryWrapper<>();
        query.orderByDesc(RuleGroup::getCreateTime).orderByAsc(RuleGroup::getName);
        IPage<RuleGroup> page = ruleGroupMapper.selectPage(new Page<>(current, size), query);
        return Converter.convert(page);
    }

    @Override
    public Pager<RuleGroup> search(String name, int current, int size) {
        LambdaQueryWrapper<RuleGroup> query = new LambdaQueryWrapper<>();
        query.like(RuleGroup::getName, name).orderByDesc(RuleGroup::getCreateTime);
        IPage<RuleGroup> page = ruleGroupMapper.selectPage(new Page<>(current, size), query);
        return Converter.convert(page);
    }
}
