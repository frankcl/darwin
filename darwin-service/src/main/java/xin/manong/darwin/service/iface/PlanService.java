package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.service.request.PlanSearchRequest;

import java.util.List;

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
    boolean add(Plan plan);

    /**
     * 更新计划
     *
     * @param plan 计划信息
     * @return 成功返回true，否则返回false
     */
    boolean update(Plan plan);

    /**
     * 更新计划下次调度时间
     *
     * @param plan 计划
     * @param baseTime 基准时间（毫秒时间戳）
     */
    void updateNextTime(Plan plan, Long baseTime);

    /**
     * 更新属于应用ID的所有计划的所属应用名
     *
     * @param appId 应用ID
     * @param appName 应用名
     */
    void updateAppName(int appId, String appName);

    /**
     * 删除计划
     *
     * @param planId 计划ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(String planId);

    /**
     * 最大抓取深度
     *
     * @param planId 计划ID
     * @return 最大抓取深度
     */
    Integer maxDepth(String planId);

    /**
     * 搜索计划列表
     *
     * @param searchRequest 搜索请求
     * @return 搜索列表
     */
    Pager<Plan> search(PlanSearchRequest searchRequest);

    /**
     * 获取开启状态计划列表
     *
     * @param pageNum 页码
     * @param pageSize 分页数量
     * @return 计划列表
     */
    List<Plan> getOpenPlanList(int pageNum, int pageSize);
}
