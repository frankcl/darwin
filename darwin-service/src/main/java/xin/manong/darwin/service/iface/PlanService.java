package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;

/**
 * 计划服务接口定义
 *
 * @author frankcl
 * @date 2023-03-15 14:29:33
 */
public interface PlanService {

    /**
     * 根据计划ID获取计划信息
     *
     * @param planId 计划ID
     * @return 计划信息，如果不存在返回null
     */
    Plan get(String planId);

    /**
     * 添加计划
     *
     * @param plan 计划信息
     * @return 成功返回true，否则返回false
     */
    Boolean add(Plan plan);

    /**
     * 更新计划
     *
     * @param plan 计划信息
     * @return 成功返回true，否则返回false
     */
    Boolean update(Plan plan);

    /**
     * 删除计划
     *
     * @param planId 计划ID
     * @return 成功返回true，否则返回false
     */
    Boolean delete(String planId);

    /**
     * 获取分页计划列表
     *
     * @param current 页码，从1开始
     * @param size 每页数量
     * @return 分页列表
     */
    Pager<Plan> getList(int current, int size);
}