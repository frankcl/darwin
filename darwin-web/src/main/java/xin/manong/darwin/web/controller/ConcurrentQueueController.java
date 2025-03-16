package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.queue.concurrent.ConcurrentManager;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.web.config.WebConfig;
import xin.manong.darwin.web.response.ConcurrentUnitInfo;
import xin.manong.darwin.web.response.MultiQueueInfo;
import xin.manong.weapon.base.redis.RedisMemory;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 多级队列及并发控制RESTFul服务
 *
 * @author frankcl
 * @date 2023-08-28 10:30:30
 */
@RestController
@Controller
@Path("/api/concurrent_queue")
@RequestMapping("/api/concurrent_queue")
public class ConcurrentQueueController {

    @Resource
    protected WebConfig webConfig;
    @Resource
    protected ConcurrentManager concurrentManager;
    @Resource
    protected MultiQueue multiQueue;

    /**
     * 获取并发单元列表
     *
     * @return 并发单元列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getConcurrentUnits")
    @GetMapping("getConcurrentUnits")
    @EnableWebLogAspect
    public List<String> getConcurrentUnits() {
        return new ArrayList<>(multiQueue.concurrentUnitsSnapshots());
    }

    /**
     * 获取并发单元信息
     *
     * @param concurrentUnit 并发单元
     * @return 并发单元信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getConcurrentUnitInfo")
    @GetMapping("getConcurrentUnitInfo")
    @EnableWebLogAspect
    public ConcurrentUnitInfo getConcurrentUnitInfo(@QueryParam("concurrent_unit") String concurrentUnit) {
        if (StringUtils.isEmpty(concurrentUnit)) throw new BadRequestException("并发单元为空");
        Long currentTime = System.currentTimeMillis();
        ConcurrentUnitInfo concurrentUnitInfo = new ConcurrentUnitInfo();
        concurrentUnitInfo.fetchCapacity = concurrentManager.getMaxConcurrentConnectionNum(concurrentUnit);
        concurrentUnitInfo.queuingSize = multiQueue.getRecordSize(concurrentUnit);
        concurrentUnitInfo.fetchingSize = 0;
        concurrentUnitInfo.fetchingExpiredSize = 0;
        Map<String, Long> connectionRecordMap = concurrentManager.getConnectionRecordMap(concurrentUnit);
        for (Map.Entry<String, Long> entry : connectionRecordMap.entrySet()) {
            Long expiredTime = entry.getValue();
            concurrentUnitInfo.fetchingSize++;
            if (currentTime + webConfig.connectionExpiredTimeMs >= expiredTime) concurrentUnitInfo.fetchingExpiredSize++;
        }
        concurrentUnitInfo.availableFetchingSize = concurrentUnitInfo.fetchCapacity - concurrentUnitInfo.fetchingSize;
        return concurrentUnitInfo;
    }

    /**
     * 获取多级队列信息
     *
     * @return 多级队列信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getMultiQueueInfo")
    @GetMapping("getMultiQueueInfo")
    @EnableWebLogAspect
    public MultiQueueInfo getMultiQueueInfo() {
        MultiQueueInfo multiQueueInfo = new MultiQueueInfo();
        multiQueueInfo.memoryLevel = multiQueue.getCurrentMemoryLevel();
        Set<String> concurrentUnits = multiQueue.concurrentUnitsSnapshots();
        multiQueueInfo.concurrentUnitNum = concurrentUnits.size();
        multiQueueInfo.fetchingRecordNum = 0;
        multiQueueInfo.queuingRecordNum = 0;
        multiQueueInfo.fetchingExpiredRecordNum = 0;
        Long currentTime = System.currentTimeMillis();
        for (String concurrentUnit : concurrentUnits) {
            multiQueueInfo.queuingRecordNum += multiQueue.getRecordSize(concurrentUnit);
            Map<String, Long> connectionRecordMap = concurrentManager.getConnectionRecordMap(concurrentUnit);
            for (Map.Entry<String, Long> entry : connectionRecordMap.entrySet()) {
                Long expiredTime = entry.getValue();
                multiQueueInfo.fetchingRecordNum++;
                if (currentTime + webConfig.connectionExpiredTimeMs >= expiredTime) {
                    multiQueueInfo.fetchingExpiredRecordNum++;
                }
            }
        }
        return multiQueueInfo;
    }

    /**
     * 获取队列当前内存信息
     *
     * @return 当前内存信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getMultiQueueMemory")
    @GetMapping("getMultiQueueMemory")
    @EnableWebLogAspect
    public RedisMemory getMultiQueueMemory() {
        return multiQueue.getMemoryInfo();
    }
}
