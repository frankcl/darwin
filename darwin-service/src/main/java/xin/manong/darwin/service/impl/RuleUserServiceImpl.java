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
import xin.manong.darwin.common.model.RuleUser;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.RuleUserMapper;
import xin.manong.darwin.service.iface.RuleUserService;
import xin.manong.darwin.service.request.RuleUserSearchRequest;

import javax.annotation.Resource;

/**
 * 规则用户关系服务实现
 *
 * @author frankcl
 * @date 2023-10-20 11:36:36
 */
@Service
public class RuleUserServiceImpl implements RuleUserService {

    private static final Logger logger = LoggerFactory.getLogger(RuleUserServiceImpl.class);

    @Resource
    protected RuleUserMapper ruleUserMapper;

    @Override
    public Boolean add(RuleUser ruleUser) {
        LambdaQueryWrapper<RuleUser> query = new LambdaQueryWrapper<>();
        query.eq(RuleUser::getRuleId, ruleUser.ruleId).eq(RuleUser::getUserId, ruleUser.userId);
        if (ruleUserMapper.selectCount(query) > 0) {
            logger.error("rule user relation has existed for rule id[{}] and user id[{}]",
                    ruleUser.ruleId, ruleUser.userId);
            throw new RuntimeException("规则用户关系已存在");
        }
        return ruleUserMapper.insert(ruleUser) > 0;
    }

    @Override
    public Boolean delete(Integer id) {
        if (ruleUserMapper.selectById(id) == null) {
            logger.error("rule user relation[{}] is not found", id);
            return false;
        }
        return ruleUserMapper.deleteById(id) > 0;
    }

    @Override
    public RuleUser get(Integer id) {
        if (id == null) {
            logger.error("rule user id is null");
            throw new IllegalArgumentException("规则用户关系ID为空");
        }
        return ruleUserMapper.selectById(id);
    }

    @Override
    public Boolean hasRulePermission(String userId, Integer ruleId) {
        RuleUserSearchRequest searchRequest = new RuleUserSearchRequest();
        searchRequest.current = 1;
        searchRequest.size = 1;
        searchRequest.userId = userId;
        searchRequest.ruleId = ruleId;
        Pager<RuleUser> pager = search(searchRequest);
        return pager != null && pager.records != null && pager.records.size() == 1;
    }

    @Override
    public Pager<RuleUser> search(RuleUserSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new RuleUserSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        LambdaQueryWrapper<RuleUser> query = new LambdaQueryWrapper<>();
        query.orderByDesc(RuleUser::getCreateTime);
        if (searchRequest.ruleId != null) query.eq(RuleUser::getRuleId, searchRequest.ruleId);
        if (!StringUtils.isEmpty(searchRequest.userId)) query.eq(RuleUser::getUserId, searchRequest.userId);
        if (!StringUtils.isEmpty(searchRequest.realName)) query.like(RuleUser::getUserRealName, searchRequest.realName);
        IPage<RuleUser> page = ruleUserMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
