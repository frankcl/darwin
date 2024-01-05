package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RuleUser;
import xin.manong.darwin.service.request.RuleUserSearchRequest;

/**
 * 规则用户关系服务接口定义
 *
 * @author frankcl
 * @date 2023-10-20 20:13:54
 */
public interface RuleUserService {

    /**
     * 添加规则用户关系
     *
     * @param ruleUser 规则用户关系
     * @return 添加成功返回true，否则返回false
     */
    Boolean add(RuleUser ruleUser);

    /**
     * 根据ID删除规则用户关系
     *
     * @param id 规则用户关系ID
     * @return 删除成功返回true，否则返回false
     */
    Boolean delete(Integer id);

    /**
     * 根据ID获取规则用户关系
     *
     * @param id 规则用户关系ID
     * @return 成功返回规则用户关系，否则返回null
     */
    RuleUser get(Integer id);

    /**
     * 用户是否有规则权限
     *
     * @param userId 用户ID
     * @param ruleId 规则ID
     * @return 如果用户具备规则权限返回true，否则返回false
     */
    Boolean hasRulePermission(String userId, Integer ruleId);

    /**
     * 搜索规则用户关系
     *
     * @param searchRequest 搜索请求
     * @return 搜索列表
     */
    Pager<RuleUser> search(RuleUserSearchRequest searchRequest);
}
