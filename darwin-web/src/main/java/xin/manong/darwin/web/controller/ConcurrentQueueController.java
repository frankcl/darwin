package xin.manong.darwin.web.controller;

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

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
@Path("/concurrent_queue")
@RequestMapping("/concurrent_queue")
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
    public List<String> getConcurrentUnits() {
        return new ArrayList<>(multiQueue.copyCurrentConcurrentUnits());
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
    public ConcurrentUnitInfo getConcurrentUnitInfo(String concurrentUnit) {
        Long currentTime = System.currentTimeMillis();
        ConcurrentUnitInfo queueInfo = new ConcurrentUnitInfo();
        queueInfo.fetchCapacity = concurrentManager.getMaxConcurrentConnectionNum(concurrentUnit);
        queueInfo.queuingSize = multiQueue.getConcurrentUnitQueuingSize(concurrentUnit);
        queueInfo.fetchingSize = 0;
        queueInfo.fetchingExpiredSize = 0;
        Map<String, Long> connectionRecordMap = concurrentManager.getConnectionRecordMap(concurrentUnit);
        for (Map.Entry<String, Long> entry : connectionRecordMap.entrySet()) {
            Long expiredTime = entry.getValue();
            queueInfo.fetchingSize++;
            if (currentTime + webConfig.connectionExpiredTimeMs >= expiredTime) queueInfo.fetchingExpiredSize++;
        }
        queueInfo.availableFetchingSize = queueInfo.fetchCapacity - queueInfo.fetchingSize;
        return queueInfo;
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
    public MultiQueueInfo getMultiQueueInfo() {
        MultiQueueInfo multiQueueInfo = new MultiQueueInfo();
        multiQueueInfo.memoryLevel = multiQueue.getCurrentMemoryLevel();
        Set<String> concurrentUnits = multiQueue.copyCurrentConcurrentUnits();
        multiQueueInfo.concurrentUnitNum = concurrentUnits.size();
        multiQueueInfo.fetchingRecordNum = 0;
        multiQueueInfo.queuingRecordNum = 0;
        multiQueueInfo.fetchingExpiredRecordNum = 0;
        Long currentTime = System.currentTimeMillis();
        for (String concurrentUnit : concurrentUnits) {
            multiQueueInfo.queuingRecordNum += multiQueue.getConcurrentUnitQueuingSize(concurrentUnit);
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
    public RedisMemory getMultiQueueMemory() {
        return multiQueue.getMemoryInfo();
    }
}
