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
import xin.manong.darwin.common.model.URLGroupCount;
import xin.manong.darwin.queue.ConcurrencyControl;
import xin.manong.darwin.queue.ConcurrencyQueue;
import xin.manong.darwin.runner.config.RunnerConfig;
import xin.manong.darwin.service.iface.ConcurrencyService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.darwin.web.request.ConcurrencyUpdateRequest;
import xin.manong.darwin.web.response.ConcurrencyUnit;
import xin.manong.darwin.web.response.ConcurrencyQueueSnapshot;
import xin.manong.weapon.base.redis.RedisMemory;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * 获取并发单元列表
     *
     * @return 并发单元列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getConcurrencyUnits")
    @GetMapping("getConcurrencyUnits")
    @EnableWebLogAspect
    public List<String> getConcurrencyUnits() {
        return new ArrayList<>(concurrencyQueue.concurrencyUnitsSnapshots());
    }

    /**
     * 获取并发单元信息
     *
     * @param unit 并发单元
     * @return 并发单元信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getConcurrencyUnit")
    @GetMapping("getConcurrencyUnit")
    @EnableWebLogAspect
    public ConcurrencyUnit getConcurrencyUnit(@QueryParam("unit") String unit) {
        if (StringUtils.isEmpty(unit)) throw new BadRequestException("并发单元为空");
        ConcurrencyUnit concurrencyUnit = new ConcurrencyUnit();
        concurrencyUnit.fetchCapacity = concurrencyControl.getMaxConcurrencyConnections(unit);
        concurrencyUnit.queuingRecords = concurrencyQueue.queuingRecordSize(unit);
        concurrencyUnit.fetchingRecords = 0;
        concurrencyUnit.expiredRecords = 0;
        Map<String, Long> connectionRecordMap = concurrencyControl.getConcurrencyRecordMap(unit);
        concurrencyUnit.fetchingRecords += connectionRecordMap.size();
        concurrencyUnit.expiredRecords += computeExpiredRecords(connectionRecordMap);
        concurrencyUnit.spareRecords = concurrencyUnit.fetchCapacity - concurrencyUnit.fetchingRecords;
        return concurrencyUnit;
    }

    /**
     * 获取并发队列快照
     *
     * @return 并发队列快照
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("concurrencyQueueSnapshot")
    @GetMapping("concurrencyQueueSnapshot")
    @EnableWebLogAspect
    public ConcurrencyQueueSnapshot concurrencyQueueSnapshot() {
        ConcurrencyQueueSnapshot concurrencyQueueSnapshot = new ConcurrencyQueueSnapshot();
        concurrencyQueueSnapshot.memoryWaterLevel = concurrencyQueue.getMemoryWaterLevel();
        Set<String> concurrentUnits = concurrencyQueue.concurrencyUnitsSnapshots();
        concurrencyQueueSnapshot.concurrentUnits = concurrentUnits.size();
        concurrencyQueueSnapshot.fetchingRecords = 0;
        concurrencyQueueSnapshot.queuingRecords = 0;
        concurrencyQueueSnapshot.expiredRecords = 0;
        for (String concurrentUnit : concurrentUnits) {
            concurrencyQueueSnapshot.queuingRecords += concurrencyQueue.queuingRecordSize(concurrentUnit);
            Map<String, Long> concurrentRecordMap = concurrencyControl.getConcurrencyRecordMap(concurrentUnit);
            concurrencyQueueSnapshot.fetchingRecords += concurrentRecordMap.size();
            concurrencyQueueSnapshot.expiredRecords += computeExpiredRecords(concurrentRecordMap);
        }
        return concurrencyQueueSnapshot;
    }

    /**
     * 统计当前头部n个抓取量最大的并发单元
     *
     * @param n 头部数量n
     * @return 统计列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("topConcurrencyUnits")
    @GetMapping("topConcurrencyUnits")
    @EnableWebLogAspect
    public List<URLGroupCount> topConcurrencyUnits(@QueryParam("n") int n) {
        if (n <= 0 || n > 100) throw new BadRequestException("top数非法");
        return urlService.topConcurrencyUnits(n);
    }

    /**
     * 获取队列当前内存信息
     *
     * @return 当前内存信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("concurrencyQueueMemory")
    @GetMapping("concurrencyQueueMemory")
    @EnableWebLogAspect
    public RedisMemory concurrencyQueueMemory() {
        return concurrencyQueue.getRedisMemory();
    }

    /**
     * 获取缺省并发数
     *
     * @return 缺省并发数
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getDefaultConcurrency")
    @GetMapping("getDefaultConcurrency")
    @EnableWebLogAspect
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
    @EnableWebLogAspect
    public Map<String, Integer> getConcurrencyConnectionMap() {
        return concurrencyService.concurrencyConnectionMap();
    }

    /**
     * 获取缺省并发数
     *
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("updateDefaultConcurrency")
    @GetMapping("updateDefaultConcurrency")
    @EnableWebLogAspect
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
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("updateConcurrencyConnectionMap")
    @GetMapping("updateConcurrencyConnectionMap")
    @EnableWebLogAspect
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
