package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.service.request.PlanSearchRequest;

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
     * 搜索计划列表
     *
     * @param searchRequest 搜索请求
     * @return 搜索列表
     */
    Pager<Plan> search(PlanSearchRequest searchRequest);

    /**
     * 执行计划
     * 1. 根据计划生成任务，将任务添加到任务表
     * 2. 将任务种子URL添加到URL记录表
     * 3. 将任务种子URL添加到MultiQueue
     * 4. 更新周期计划下次执行时间
     *
     * @param plan 计划
     * @return 成功返回计划相关任务，否则返回null
     */
    Job execute(Plan plan);
}
