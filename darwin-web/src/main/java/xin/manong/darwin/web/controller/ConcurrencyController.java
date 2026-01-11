package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.common.model.URLGroupCount;
import xin.manong.darwin.queue.ConcurrencyControl;
import xin.manong.darwin.queue.ConcurrencyQueue;
import xin.manong.darwin.runner.config.RunnerConfig;
import xin.manong.darwin.service.iface.ConcurrencyService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.darwin.web.request.ConcurrencyUpdateRequest;
import xin.manong.darwin.web.request.CrawlDelayUpdateRequest;
import xin.manong.darwin.web.response.ConcurrencyUnit;
import xin.manong.darwin.web.response.QueueWait;

import java.util.List;
import java.util.Map;

/**
 * 并发队列RESTFul服务
 *
 * @author frankcl
 * @date 2023-08-28 10:30:30
 */
@RestController
@Controller
@Path("/api/concurrency")
@RequestMapping("/api/concurrency")
public class ConcurrencyController {

    @Resource
    private RunnerConfig runnerConfig;
    @Resource
    private ConcurrencyControl concurrencyControl;
    @Resource
    private ConcurrencyQueue concurrencyQueue;
    @Resource
    private ConcurrencyService concurrencyService;
    @Resource
    private URLService urlService;
    @Resource
    private PermissionSupport permissionSupport;

    /**
     * 获取并发单元信息
     *
     * @param name 并发单元
     * @return 并发单元信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getConcurrencyUnit")
    @GetMapping("getConcurrencyUnit")
    public ConcurrencyUnit getConcurrencyUnit(@QueryParam("name") String name) {
        if (StringUtils.isEmpty(name)) throw new BadRequestException("并发单元名称为空");
        ConcurrencyUnit concurrencyUnit = new ConcurrencyUnit(name);
        concurrencyUnit.fetchCapacity = concurrencyControl.getMaxConcurrencyConnections(name);
        concurrencyUnit.queuingRecords = concurrencyQueue.queuingRecordSize(name);
        if (concurrencyUnit.queuingRecords == 0) throw new NotFoundException("并发单元没有排队数据");
        concurrencyUnit.fetchingRecords = 0;
        concurrencyUnit.expiredRecords = 0;
        Map<String, Long> connectionRecordMap = concurrencyControl.getConcurrencyRecordMap(name);
        concurrencyUnit.fetchingRecords += connectionRecordMap.size();
        concurrencyUnit.expiredRecords += computeExpiredRecords(connectionRecordMap);
        concurrencyUnit.spareRecords = concurrencyUnit.fetchCapacity - connectionRecordMap.size();
        return concurrencyUnit;
    }

    /**
     * 获取并发单元排队信息
     *
     * @param name 并发单元
     * @return 排队信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getConcurrencyQueueWait")
    @GetMapping("getConcurrencyQueueWait")
    public QueueWait getConcurrencyQueueWait(@QueryParam("name") String name) {
        QueueWait queueWait = new QueueWait();
        queueWait.waitCount = urlService.queueWaitCount(name);
        queueWait.waitTime = urlService.queueWaitTime(name);
        RangeValue<Long> fetchTimeRange = new RangeValue<>();
        fetchTimeRange.start = System.currentTimeMillis() - 5 * 60 * 1000L;
        fetchTimeRange.includeLower = true;
        queueWait.fiveMinutesFetchCount = urlService.fetchTimeRangeFetchCount(name, fetchTimeRange);
        long fetchingCount = urlService.fetchingCount(name);
        long totalCount = fetchingCount + queueWait.waitCount;
        queueWait.queueRatio = totalCount == 0L ? 0d : queueWait.waitCount * 1.0d / totalCount;
        queueWait.queueRatio = Double.parseDouble(String.format("%.2f", queueWait.queueRatio * 100d));
        return queueWait;
    }

    /**
     * TOP排队等待并发单元列表
     *
     * @param n top数量
     * @return 并发单元列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("waitConcurrencyUnits")
    @GetMapping("waitConcurrencyUnits")
    public List<URLGroupCount> waitConcurrencyUnits(@QueryParam("n") int n) {
        if (n <= 0 || n > 100) throw new BadRequestException("TOP数量超过限制");
        return urlService.waitConcurrencyUnits(n);
    }

    /**
     * 获取缺省抓取间隔
     *
     * @return 缺省抓取间隔
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getDefaultCrawlDelay")
    @GetMapping("getDefaultCrawlDelay")
    public long getDefaultCrawlDelay() {
        return concurrencyService.defaultCrawlDelay();
    }

    /**
     * 获取抓取间隔配置
     *
     * @return 抓取间隔配置
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getCrawlDelayMap")
    @GetMapping("getCrawlDelayMap")
    public Map<String, Long> getCrawlDelayMap() {
        return concurrencyService.crawlDelayMap();
    }

    /**
     * 更新默认抓取间隔
     *
     * @param request 更新请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("updateDefaultCrawlDelay")
    @GetMapping("updateDefaultCrawlDelay")
    public boolean updateDefaultCrawlDelay(@RequestBody CrawlDelayUpdateRequest request) {
        request.check();
        permissionSupport.checkAdmin();
        concurrencyService.defaultCrawlDelay(request.defaultCrawlDelay);
        return true;
    }

    /**
     * 更新抓取间隔配置
     *
     * @param crawlDelayMap 抓取间隔配置
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("updateCrawlDelayMap")
    @GetMapping("updateCrawlDelayMap")
    public boolean updateCrawlDelayMap(@RequestBody Map<String, Long> crawlDelayMap) {
        if (crawlDelayMap == null) throw new BadRequestException("抓取间隔配置为空");
        permissionSupport.checkAdmin();
        concurrencyService.crawlDelayMap(crawlDelayMap);
        return true;
    }

    /**
     * 获取默认并发连接
     *
     * @return 默认并发连接
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getDefaultConcurrency")
    @GetMapping("getDefaultConcurrency")
    public int getDefaultConcurrency() {
        return concurrencyService.defaultConcurrency();
    }

    /**
     * 获取并发连接配置
     *
     * @return 并发连接配置
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getConcurrencyConnectionMap")
    @GetMapping("getConcurrencyConnectionMap")
    public Map<String, Integer> getConcurrencyConnectionMap() {
        return concurrencyService.concurrencyConnectionMap();
    }

    /**
     * 更新默认并发连接
     *
     * @param request 更新请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("updateDefaultConcurrency")
    @GetMapping("updateDefaultConcurrency")
    public boolean updateDefaultConcurrency(@RequestBody ConcurrencyUpdateRequest request) {
        request.check();
        permissionSupport.checkAdmin();
        concurrencyService.defaultConcurrency(request.defaultConcurrency);
        return true;
    }

    /**
     * 更新并发连接配置
     *
     * @param concurrencyConnectionMap 并发连接配置
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("updateConcurrencyConnectionMap")
    @GetMapping("updateConcurrencyConnectionMap")
    public boolean updateConcurrencyConnectionMap(@RequestBody Map<String, Integer> concurrencyConnectionMap) {
        if (concurrencyConnectionMap == null) throw new BadRequestException("并发连接配置为空");
        permissionSupport.checkAdmin();
        concurrencyService.concurrencyConnectionMap(concurrencyConnectionMap);
        return true;
    }

    /**
     * 计算并发单元过期数据数量
     *
     * @param concurrentRecordMap 并发单元连接记录
     * @return 过期数据数量
     */
    private int computeExpiredRecords(Map<String, Long> concurrentRecordMap) {
        long currentTime = System.currentTimeMillis();
        int expiredRecords = 0;
        for (Map.Entry<String, Long> entry : concurrentRecordMap.entrySet()) {
            long interval = currentTime - entry.getValue();
            if (interval >= runnerConfig.concurrencyQueueExpiredTimeIntervalMs) expiredRecords++;
        }
        return expiredRecords;
    }
}
