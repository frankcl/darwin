package xin.manong.darwin.runner.core;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Dashboard;
import xin.manong.darwin.common.model.DashboardValue;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.common.model.URLGroupCount;
import xin.manong.darwin.service.iface.DashboardService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.weapon.base.executor.ExecuteRunner;
import xin.manong.weapon.base.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页大盘数据计算
 *
 * @author frankcl
 * @date 2025-04-23 14:30:26
 */
public class DashboardRunner extends ExecuteRunner {

    private static final Logger logger = LoggerFactory.getLogger(DashboardRunner.class);

    private static final String HOUR_FORMAT = "yyyy-MM-dd HH";
    public static final String ID = "dashboard_runner";

    @Resource
    private URLService urlService;
    @Resource
    private DashboardService dashboardService;

    public DashboardRunner(long executeTimeIntervalMs) {
        super(ID, executeTimeIntervalMs);
        this.setName("仪表盘数据计算器");
        this.setDescription("负责首页趋势数据统计工作");
    }

    @Override
    public void execute() throws Exception {
        long currentTime = System.currentTimeMillis();
        String currentHour = CommonUtil.timeToString(currentTime, HOUR_FORMAT);
        String prevHour = CommonUtil.timeToString(currentTime - 60 * 60 * 1000L, HOUR_FORMAT);
        long currentHourTime = CommonUtil.stringToTime(currentHour, HOUR_FORMAT);
        long prevHourTime = CommonUtil.stringToTime(prevHour, HOUR_FORMAT);
        RangeValue<Long> currentHourTimeRange = new RangeValue<>();
        currentHourTimeRange.start = currentHourTime;
        currentHourTimeRange.includeLower = true;
        currentHourTimeRange.end = currentTime;
        RangeValue<Long> prevHourTimeRange = new RangeValue<>();
        prevHourTimeRange.start = prevHourTime;
        prevHourTimeRange.includeLower = true;
        prevHourTimeRange.end = currentHourTime;
        upsertTotalTrend(prevHour, prevHourTimeRange);
        upsertTotalTrend(currentHour, currentHourTimeRange);
        upsertStatusTrend(prevHour, prevHourTimeRange);
        upsertStatusTrend(currentHour, currentHourTimeRange);
        upsertContentTrend(prevHour, prevHourTimeRange);
        upsertContentTrend(currentHour, currentHourTimeRange);
    }

    /**
     * 计算总量趋势
     *
     * @param hour 小时
     * @param timeRange 时间范围
     */
    private void upsertTotalTrend(String hour, RangeValue<Long> timeRange) {
        List<DashboardValue<Integer>> values = new ArrayList<>();
        values.add(new DashboardValue<>("URL", urlService.urlCount(timeRange)));
        values.add(new DashboardValue<>("HOST", urlService.hostCount(timeRange)));
        values.add(new DashboardValue<>("DOMAIN", urlService.domainCount(timeRange)));
        Dashboard dashboard = new Dashboard(hour, Constants.DASHBOARD_CATEGORY_TOTAL, values);
        if (!dashboardService.upsert(dashboard)) logger.warn("Upsert total trend dashboard failed for hour:{}", hour);
    }

    /**
     * 计算数据状态趋势
     *
     * @param hour 小时
     * @param timeRange 时间范围
     */
    private void upsertStatusTrend(String hour, RangeValue<Long> timeRange) {
        List<URLGroupCount> groupCounts = urlService.countGroupByStatus(null, timeRange);
        List<DashboardValue<Integer>> values = new ArrayList<>();
        groupCounts.forEach(groupCount -> values.add(new DashboardValue<>(
                Constants.SUPPORT_URL_STATUSES.get(groupCount.status), groupCount.count)));
        Dashboard dashboard = new Dashboard(hour, Constants.DASHBOARD_CATEGORY_STATUS, values);
        if (!dashboardService.upsert(dashboard)) logger.warn("Upsert status trend dashboard failed for hour:{}", hour);
    }

    /**
     * 计算数据内容趋势
     *
     * @param hour 小时
     * @param timeRange 时间范围
     */
    private void upsertContentTrend(String hour, RangeValue<Long> timeRange) {
        List<URLGroupCount> groupCounts = urlService.countGroupByCategory(null, timeRange);
        List<DashboardValue<Integer>> values = new ArrayList<>();
        groupCounts.forEach(groupCount -> values.add(new DashboardValue<>(
                Constants.SUPPORT_CONTENT_CATEGORIES.get(groupCount.category), groupCount.count)));
        Dashboard dashboard = new Dashboard(hour, Constants.DASHBOARD_CATEGORY_CONTENT, values);
        if (!dashboardService.upsert(dashboard)) logger.warn("Upsert content trend dashboard failed for hour:{}", hour);
    }
}
