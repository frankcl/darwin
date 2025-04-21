package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.queue.ConcurrencyControl;
import xin.manong.darwin.queue.ConcurrencyQueue;
import xin.manong.darwin.web.config.WebConfig;
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
    protected WebConfig webConfig;
    @Resource
    protected ConcurrencyControl concurrencyControl;
    @Resource
    protected ConcurrencyQueue concurrencyQueue;

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
        return new ArrayList<>(concurrencyQueue.concurrentUnitsSnapshots());
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
        concurrencyUnit.fetchCapacity = concurrencyControl.getMaxConcurrentConnections(unit);
        concurrencyUnit.queuingRecords = concurrencyQueue.queuingRecordSize(unit);
        concurrencyUnit.fetchingRecords = 0;
        concurrencyUnit.expiredRecords = 0;
        Map<String, Long> connectionRecordMap = concurrencyControl.getConcurrentRecordMap(unit);
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
        Set<String> concurrentUnits = concurrencyQueue.concurrentUnitsSnapshots();
        concurrencyQueueSnapshot.concurrentUnits = concurrentUnits.size();
        concurrencyQueueSnapshot.fetchingRecords = 0;
        concurrencyQueueSnapshot.queuingRecords = 0;
        concurrencyQueueSnapshot.expiredRecords = 0;
        for (String concurrentUnit : concurrentUnits) {
            concurrencyQueueSnapshot.queuingRecords += concurrencyQueue.queuingRecordSize(concurrentUnit);
            Map<String, Long> concurrentRecordMap = concurrencyControl.getConcurrentRecordMap(concurrentUnit);
            concurrencyQueueSnapshot.fetchingRecords += concurrentRecordMap.size();
            concurrencyQueueSnapshot.expiredRecords += computeExpiredRecords(concurrentRecordMap);
        }
        return concurrencyQueueSnapshot;
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
            if (interval >= webConfig.maxConnectionExpiredIntervalMs) expiredRecords++;
        }
        return expiredRecords;
    }
}
