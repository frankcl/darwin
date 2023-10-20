package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RuleGroup;

/**
 * 规则分组服务定义
 *
 * @author frankcl
 * @date 2023-03-21 15:59:07
 */
public interface RuleGroupService {

    /**
     * 添加规则分组
     *
     * @param ruleGroup 规则分组
     * @return 添加成功返回true，否则返回false
     */
    Boolean add(RuleGroup ruleGroup);

    /**
     * 更新规则分组
     *
     * @param ruleGroup 规则分组
     * @return 更新成功返回true，否则返回false
     */
    Boolean update(RuleGroup ruleGroup);

    /**
     * 根据ID删除规则分组
     *
     * @param id 规则分组ID
     * @return 删除成功返回true，否则返回false
     */
    Boolean delete(Integer id);

    /**
     * 根据ID获取规则分组
     *
     * @param id 规则分组ID
     * @return 存在返回规则分组，否则返回null
     */
    RuleGroup get(Integer id);

    /**
     * 获取分页规则分组列表
     *
     * @param current 页码，从1开始
     * @param size 每页数量
     * @return 分页列表
     */
    Pager<RuleGroup> getList(int current, int size);

    /**
     * 根据名字搜索规则分组
     *
     * @param name 名字
     * @param current 页码，从1开始
     * @param size 每页数量
     * @return 搜索列表
     */
    Pager<RuleGroup> search(String name, int current, int size);
}
