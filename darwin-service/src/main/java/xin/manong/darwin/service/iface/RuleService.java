package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.service.request.RuleSearchRequest;

/**
 * 规则服务接口定义
 *
 * @author frankcl
 * @date 2023-03-21 17:53:04
 */
public interface RuleService {

    /**
     * 添加规则
     *
     * @param rule 规则
     * @return 添加成功返回true，否则返回false
     */
    Boolean add(Rule rule);

    /**
     * 更新规则
     *
     * @param rule 规则
     * @return 更新成功返回true，否则返回false
     */
    Boolean update(Rule rule);

    /**
     * 根据ID删除规则
     *
     * @param id 规则ID
     * @return 删除成功返回true，否则返回false
     */
    Boolean delete(Long id);

    /**
     * 根据ID获取规则
     *
     * @param id 规则ID
     * @return 存在返回规则，否则返回null
     */
    Rule get(Long id);

    /**
     * 搜索规则列表
     *
     * @param searchRequest 搜索请求
     * @param current 页码，从1开始
     * @param size 每页数量
     * @return 搜索列表
     */
    Pager<Rule> search(RuleSearchRequest searchRequest, int current, int size);
}
