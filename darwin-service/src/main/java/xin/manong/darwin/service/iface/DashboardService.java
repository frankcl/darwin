package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Dashboard;

import java.util.List;

/**
 * 首页大盘统计服务
 *
 * @author frankcl
 * @date 2025-04-23 15:52:20
 */
public interface DashboardService {

    /**
     * 根据小时和分类获取统计结果
     *
     * @param hour 小时
     * @param category 分类
     * @return 统计结果
     */
    Dashboard get(String hour, int category);

    /**
     * 列表时间范围内大盘统计
     *
     * @param fromHour 起始小时
     * @param toHour 结束小时
     * @param category 分类
     * @return 列表
     */
    List<Dashboard> betweenList(String fromHour, String toHour, int category);

    /**
     * 如果统计存在更新数据，否则添加数据
     *
     * @param dashboard 大盘统计
     * @return 成功返回true，否则返回false
     */
    boolean upsert(Dashboard dashboard);

    /**
     * 删除统计时间小于beforeHour统计数据
     *
     * @param beforeHour 小时
     */
    void delete(String beforeHour);
}
