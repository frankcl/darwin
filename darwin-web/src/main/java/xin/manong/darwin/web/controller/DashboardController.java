package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Dashboard;
import xin.manong.darwin.common.model.DashboardValue;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.common.model.URLGroupCount;
import xin.manong.darwin.service.iface.DashboardService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.weapon.base.util.CommonUtil;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大盘统计控制器
 *
 * @author frankcl
 * @date 2025-04-23 20:33:10
 */
@RestController
@Controller
@Path("/api/dashboard")
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final String HOUR_FORMAT = "yyyy-MM-dd HH";

    @Resource
    private URLService urlService;
    @Resource
    private DashboardService dashboardService;

    /**
     * 抓取量top站点列表
     *
     * @return top站点列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("topHosts")
    @GetMapping("topHosts")
    @EnableWebLogAspect
    public List<URLGroupCount> topHosts() {
        long currentTime = System.currentTimeMillis();
        RangeValue<Long> timeRange = new RangeValue<>();
        timeRange.start = currentTime - 86400000L;
        timeRange.includeLower = true;
        timeRange.end = currentTime;
        return urlService.topHosts(timeRange, 10);
    }

    /**
     * 获取总量趋势统计
     *
     * @return 总量趋势统计
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("totalTrend")
    @GetMapping("totalTrend")
    @EnableWebLogAspect
    public List<Dashboard> totalTrend() {
        long currentTime = System.currentTimeMillis();
        String endHour = CommonUtil.timeToString(currentTime, HOUR_FORMAT);
        String startHour = CommonUtil.timeToString(currentTime - 86400000L + 3600000L, HOUR_FORMAT);
        return dashboardService.betweenList(startHour, endHour, Constants.DASHBOARD_CATEGORY_TOTAL);
    }

    /**
     * 获取抓取状态趋势统计
     *
     * @return 抓取状态趋势统计
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("statusTrend")
    @GetMapping("statusTrend")
    @EnableWebLogAspect
    public List<Dashboard> statusTrend() {
        long currentTime = System.currentTimeMillis();
        String endHour = CommonUtil.timeToString(currentTime, HOUR_FORMAT);
        String startHour = CommonUtil.timeToString(currentTime - 86400000L + 3600000L, HOUR_FORMAT);
        return dashboardService.betweenList(startHour, endHour, Constants.DASHBOARD_CATEGORY_STATUS);
    }

    /**
     * 获取内容类型趋势统计
     *
     * @return 内容类型趋势统计
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("contentTrend")
    @GetMapping("contentTrend")
    @EnableWebLogAspect
    public List<Dashboard> contentTrend() {
        long currentTime = System.currentTimeMillis();
        String endHour = CommonUtil.timeToString(currentTime, HOUR_FORMAT);
        String startHour = CommonUtil.timeToString(currentTime - 86400000L + 3600000L, HOUR_FORMAT);
        return dashboardService.betweenList(startHour, endHour, Constants.DASHBOARD_CATEGORY_CONTENT);
    }

    /**
     * 计算百分比
     * 1. 抓取成功率
     * 2. 排队率
     *
     * @return 百分比
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("percentage")
    @GetMapping("percentage")
    @EnableWebLogAspect
    public Map<String, List<DashboardValue<Double>>> percentage() {
        Map<String, List<DashboardValue<Double>>> percentageMap = new HashMap<>();
        percentageMap.put("抓取成功率", computeFetchSuccessPercentage());
        percentageMap.put("排队率", computeQueuingPercentage());
        return percentageMap;
    }

    /**
     * 计算平均内容长度
     *
     * @return 平均内容长度
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("averageContentLength")
    @GetMapping("averageContentLength")
    @EnableWebLogAspect
    public Map<String, List<DashboardValue<Long>>> averageContentLength() {
        long currentTime = System.currentTimeMillis();
        long fromTime = currentTime - 86400000L;
        RangeValue<Long> timeRange = new RangeValue<>();
        timeRange.start = fromTime;
        timeRange.includeLower = true;
        timeRange.end = currentTime;
        List<DashboardValue<Long>> averages = new ArrayList<>();
        averages.add(new DashboardValue<>("全部", urlService.avgContentLength(null, timeRange) / 1024));
        averages.add(new DashboardValue<>(Constants.SUPPORT_CONTENT_CATEGORIES.get(Constants.CONTENT_CATEGORY_PAGE),
                urlService.avgContentLength(Constants.CONTENT_CATEGORY_PAGE, timeRange) / 1024));
        averages.add(new DashboardValue<>(Constants.SUPPORT_CONTENT_CATEGORIES.get(Constants.CONTENT_CATEGORY_RESOURCE),
                urlService.avgContentLength(Constants.CONTENT_CATEGORY_RESOURCE, timeRange) / 1024));
        averages.add(new DashboardValue<>(Constants.SUPPORT_CONTENT_CATEGORIES.get(Constants.CONTENT_CATEGORY_STREAM),
                urlService.avgContentLength(Constants.CONTENT_CATEGORY_STREAM, timeRange) / 1024));
        Map<String, List<DashboardValue<Long>>> averageMap = new HashMap<>();
        averageMap.put("平均内容长度", averages);
        return averageMap;
    }

    /**
     * 计算抓取成功率
     *
     * @return 抓取成功率
     */
    private List<DashboardValue<Double>> computeFetchSuccessPercentage() {
        long currentTime = System.currentTimeMillis();
        RangeValue<Long> timeRange = new RangeValue<>();
        timeRange.start = currentTime - 86400000L;
        timeRange.includeLower = true;
        timeRange.end = currentTime;
        List<DashboardValue<Double>> percentage = new ArrayList<>();
        percentage.add(computePercentage(null, fetchSuccessStatus(), fetchStatus(), timeRange));
        percentage.add(computePercentage(Constants.CONTENT_CATEGORY_PAGE,
                fetchSuccessStatus(), fetchStatus(), timeRange));
        percentage.add(computePercentage(Constants.CONTENT_CATEGORY_RESOURCE,
                fetchSuccessStatus(), fetchStatus(), timeRange));
        percentage.add(computePercentage(Constants.CONTENT_CATEGORY_STREAM,
                fetchSuccessStatus(), fetchStatus(), timeRange));
        return percentage;
    }

    /**
     * 计算排队率
     *
     * @return 排队率
     */
    private List<DashboardValue<Double>> computeQueuingPercentage() {
        long currentTime = System.currentTimeMillis();
        RangeValue<Long> timeRange = new RangeValue<>();
        timeRange.includeLower = true;
        timeRange.start = currentTime - 3600000L;
        timeRange.end = currentTime;
        List<DashboardValue<Double>> percentage = new ArrayList<>();
        percentage.add(computePercentage(null, queuingStatus(), null, timeRange));
        percentage.add(computePercentage(Constants.CONTENT_CATEGORY_PAGE,
                queuingStatus(), null, timeRange));
        percentage.add(computePercentage(Constants.CONTENT_CATEGORY_RESOURCE,
                queuingStatus(), null, timeRange));
        percentage.add(computePercentage(Constants.CONTENT_CATEGORY_STREAM,
                queuingStatus(), null, timeRange));
        return percentage;
    }

    /**
     * 计算百分比
     *
     * @param category 内容类型
     * @param numeratorStatusList 分母状态列表
     * @param denominatorStatusList 分子状态列表
     * @param timeRange 时间范围
     * @return 百分比
     */
    private DashboardValue<Double> computePercentage(Integer category,
                                                     List<Integer> numeratorStatusList,
                                                     List<Integer> denominatorStatusList,
                                                     RangeValue<Long> timeRange) {
        long denominator = computeCount(category, denominatorStatusList, timeRange);
        long numerator = computeCount(category, numeratorStatusList, timeRange);
        String name = category == null ? "全部" : Constants.SUPPORT_CONTENT_CATEGORIES.get(category);
        double ratio = denominator == 0d ? 0d : numerator * 100d / denominator;
        return new DashboardValue<>(name, Double.parseDouble(String.format("%.2f", ratio)));
    }

    /**
     * 计算数量
     *
     * @param category 内容类型
     * @param statusList 状态列表
     * @param timeRange 时间范围
     * @return 抓取数量
     */
    private long computeCount(Integer category, List<Integer> statusList, RangeValue<Long> timeRange) {
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.createTimeRange = timeRange;
        searchRequest.category = category;
        searchRequest.statusList = statusList;
        return urlService.selectCount(searchRequest);
    }

    /**
     * 抓取成功状态
     *
     * @return 抓取成功状态
     */
    private List<Integer> fetchSuccessStatus() {
        List<Integer> statusList = new ArrayList<>();
        statusList.add(Constants.URL_STATUS_FETCH_SUCCESS);
        return statusList;
    }

    /**
     * 抓取状态
     *
     * @return 抓取状态
     */
    private List<Integer> fetchStatus() {
        List<Integer> statusList = new ArrayList<>();
        statusList.add(Constants.URL_STATUS_FETCH_SUCCESS);
        statusList.add(Constants.URL_STATUS_FETCH_FAIL);
        statusList.add(Constants.URL_STATUS_ERROR);
        return statusList;
    }

    /**
     * 排队状态
     *
     * @return 排队状态
     */
    private List<Integer> queuingStatus() {
        List<Integer> statusList = new ArrayList<>();
        statusList.add(Constants.URL_STATUS_QUEUING);
        return statusList;
    }
}
