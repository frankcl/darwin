package xin.manong.darwin.runner.core;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Trend;
import xin.manong.darwin.common.model.TrendValue;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.service.iface.TrendService;
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
    private TrendService trendService;

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
        upsertFetchCountTrend(prevHour, prevHourTimeRange);
        upsertFetchCountTrend(currentHour, currentHourTimeRange);
    }

    /**
     * 更新抓取量趋势
     *
     * @param key 小时
     * @param timeRange 时间范围
     */
    private void upsertFetchCountTrend(String key, RangeValue<Long> timeRange) {
        List<TrendValue<?>> values = new ArrayList<>();
        values.add(new TrendValue<>("链接数量", urlService.fetchURLCount(timeRange)));
        values.add(new TrendValue<>("站点数量", urlService.fetchHostCount(timeRange)));
        values.add(new TrendValue<>("域名数量", urlService.fetchDomainCount(timeRange)));
        Trend trend = new Trend(key, Constants.TREND_CATEGORY_FETCH_COUNT, values);
        if (!trendService.upsert(trend)) logger.warn("Upsert fetch count trend failed for key:{}", key);
    }
}
