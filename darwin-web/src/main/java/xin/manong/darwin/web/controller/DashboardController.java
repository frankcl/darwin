package xin.manong.darwin.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Trend;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.common.model.URLGroupCount;
import xin.manong.darwin.queue.ConcurrencyQueue;
import xin.manong.darwin.service.iface.TrendService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.darwin.web.response.DownAnalysis;
import xin.manong.darwin.web.response.FetchAnalysis;
import xin.manong.darwin.web.response.QueueMemory;
import xin.manong.darwin.web.response.QueueWait;
import xin.manong.weapon.base.redis.RedisMemory;
import xin.manong.weapon.base.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

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

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private ConcurrencyQueue concurrencyQueue;
    @Resource
    private URLService urlService;
    @Resource
    private TrendService trendService;

    /**
     * 获取24小时抓取量分析
     *
     * @param contentType 内容类型
     * @return 抓取量分析
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getFetchAnalysis")
    @GetMapping("getFetchAnalysis")
    public FetchAnalysis getFetchAnalysis(@QueryParam("content_type") Integer contentType) {
        FetchAnalysis fetchAnalysis = new FetchAnalysis();
        RangeValue<Long> timeRange = new RangeValue<>();
        timeRange.end = System.currentTimeMillis();
        timeRange.start = timeRange.end - 86400000L;
        timeRange.includeLower = timeRange.includeUpper = true;
        fetchAnalysis.fetchCount = selectCount(contentType, fetchStatus(), timeRange);
        fetchAnalysis.fetchSuccessCount = selectCount(contentType, fetchSuccessStatus(), timeRange);
        fetchAnalysis.fetchSuccessRatio = fetchAnalysis.fetchCount == 0L ? 0d :
                fetchAnalysis.fetchSuccessCount * 1.0d / fetchAnalysis.fetchCount;
        fetchAnalysis.fetchSuccessRatio = Double.parseDouble(
                String.format("%.2f", fetchAnalysis.fetchSuccessRatio * 100d));
        timeRange.end = timeRange.start;
        timeRange.start = timeRange.end - 86400000L;
        long yesterdayFetchCount = selectCount(contentType, fetchStatus(), timeRange);
        if (yesterdayFetchCount > 0) {
            fetchAnalysis.proportion = (fetchAnalysis.fetchCount -
                    yesterdayFetchCount) * 1.0d / yesterdayFetchCount;
            fetchAnalysis.proportion = Double.parseDouble(
                    String.format("%.2f", fetchAnalysis.proportion * 100d));
        }
        return fetchAnalysis;
    }

    /**
     * 获取24小时下载内容分析
     *
     * @param contentType 数据类型
     * @return 下载内容分析
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getDownAnalysis")
    @GetMapping("getDownAnalysis")
    public DownAnalysis getDownAnalysis(@QueryParam("content_type") Integer contentType) {
        if (contentType != null && !Constants.SUPPORT_CONTENT_TYPES.containsKey(contentType)) {
            throw new BadRequestException("不支持内容类型");
        }
        DownAnalysis downAnalysis = new DownAnalysis();
        RangeValue<Long> timeRange = new RangeValue<>();
        timeRange.end = System.currentTimeMillis();
        timeRange.start = timeRange.end - 86400000L;
        timeRange.includeLower = timeRange.includeUpper = true;
        downAnalysis.avgDownTime = urlService.avgDownTime(contentType, timeRange);
        downAnalysis.avgContentLength = urlService.avgContentLength(contentType, timeRange);
        return downAnalysis;
    }

    /**
     * 获取队列内存信息
     *
     * @return 队列内存信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getQueueMemory")
    @GetMapping("getQueueMemory")
    public QueueMemory getQueueMemory() {
        QueueMemory queueMemory = new QueueMemory();
        RedisMemory redisMemory = concurrencyQueue.getRedisMemory();
        queueMemory.useMemory = redisMemory.usedMemoryRssBytes;
        queueMemory.maxMemory = redisMemory.maxMemoryBytes == 0L ?
                redisMemory.totalSystemMemoryBytes : redisMemory.maxMemoryBytes;
        queueMemory.waterLevel = Constants.MEMORY_WATER_LEVEL_MAP.get(concurrencyQueue.getMemoryWaterLevel());
        queueMemory.useRatio = queueMemory.maxMemory == 0L ? 0d : queueMemory.useMemory * 1.0d / queueMemory.maxMemory;
        queueMemory.useRatio = Double.parseDouble(String.format("%.2f", queueMemory.useRatio * 100d));
        return queueMemory;
    }

    /**
     * 获取队列排队信息
     *
     * @return 队列排队信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getQueueWait")
    @GetMapping("getQueueWait")
    public QueueWait getQueueWait() {
        QueueWait queueWait = new QueueWait();
        queueWait.waitCount = urlService.queueWaitCount(null);
        queueWait.waitTime = urlService.queueWaitTime(null);
        long fetchingCount = urlService.fetchingCount(null);
        long totalCount = fetchingCount + queueWait.waitCount;
        queueWait.queueRatio = totalCount == 0L ? 0d : queueWait.waitCount * 1.0d / totalCount;
        queueWait.queueRatio = Double.parseDouble(String.format("%.2f", queueWait.queueRatio * 100d));
        return queueWait;
    }

    /**
     * 排队等待数据优先级分布统计
     *
     * @return 优先级分布统计
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("queueWaitPriority")
    @GetMapping("queueWaitPriority")
    public List<URLGroupCount> queueWaitPriority() {
        return urlService.queueWaitPriority();
    }

    /**
     * 24小时抓取量TOP10站点列表
     *
     * @return 站点列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("hostFetchCount")
    @GetMapping("hostFetchCount")
    public List<URLGroupCount> hostFetchCount() {
        long currentTime = System.currentTimeMillis();
        RangeValue<Long> timeRange = new RangeValue<>();
        timeRange.start = currentTime - 86400000L;
        timeRange.includeLower = true;
        return urlService.hostFetchCount(timeRange, 10);
    }

    /**
     * 计算抓取量趋势
     *
     * @return 抓取量趋势
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("fetchCountTrend")
    @GetMapping("fetchCountTrend")
    public List<Trend> fetchCountTrend() {
        long currentTime = System.currentTimeMillis();
        String endHour = CommonUtil.timeToString(currentTime, HOUR_FORMAT);
        String fromHour = CommonUtil.timeToString(currentTime - 86400000L + 3600000L, HOUR_FORMAT);
        return trendService.between(fromHour, endHour, Constants.TREND_CATEGORY_FETCH_COUNT);
    }

    /**
     * 数据状态分布统计
     *
     * @param jobId 任务ID
     * @param timeRange 时间范围
     * @return 统计结果
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("statusGroupCount")
    @GetMapping("statusGroupCount")
    public List<URLGroupCount> statusGroupCount(@QueryParam("job_id") String jobId,
                                                @QueryParam("time_range") String timeRange) {
        RangeValue<Long> rangeValue = null;
        if (StringUtils.isNotEmpty(timeRange)) rangeValue = parseTimeRange(timeRange);
        return urlService.statusGroupCount(jobId, rangeValue);
    }

    /**
     * 内容类型分布统计
     *
     * @param jobId 任务ID
     * @param timeRange 时间范围
     * @return 统计结果
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("contentGroupCount")
    @GetMapping("contentGroupCount")
    public List<URLGroupCount> contentGroupCount(@QueryParam("job_id") String jobId,
                                                 @QueryParam("time_range") String timeRange) {
        RangeValue<Long> rangeValue = null;
        if (StringUtils.isNotEmpty(timeRange)) rangeValue = parseTimeRange(timeRange);
        return urlService.contentGroupCount(jobId, rangeValue);
    }

    /**
     * 计算数量
     *
     * @param contentType 内容类型
     * @param statusList 状态列表
     * @param createTimeRange 创建时间范围
     * @return 抓取数量
     */
    private long selectCount(Integer contentType, List<Integer> statusList,
                             RangeValue<Long> createTimeRange) {
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.createTimeRange = createTimeRange;
        searchRequest.contentType = contentType;
        searchRequest.statusList = statusList;
        return urlService.selectCount(searchRequest);
    }

    /**
     * 解析时间范围字符串
     *
     * @param timeRange 时间范围字符串
     * @return 时间范围
     */
    private RangeValue<Long> parseTimeRange(String timeRange) {
        try {
            return objectMapper.readValue(timeRange, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new BadRequestException("非法时间范围");
        }
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
     * @return 抓取成功状态
     */
    private List<Integer> fetchStatus() {
        List<Integer> statusList = new ArrayList<>();
        statusList.add(Constants.URL_STATUS_FETCH_SUCCESS);
        statusList.add(Constants.URL_STATUS_FETCH_FAIL);
        statusList.add(Constants.URL_STATUS_TIMEOUT);
        statusList.add(Constants.URL_STATUS_ERROR);
        return statusList;
    }
}
