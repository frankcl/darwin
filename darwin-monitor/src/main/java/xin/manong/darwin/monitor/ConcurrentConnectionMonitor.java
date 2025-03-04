package xin.manong.darwin.monitor;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.queue.concurrent.ConcurrentManager;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.weapon.base.executor.ExecuteRunner;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 并发连接监控器
 *
 * @author frankcl
 * @date 2023-03-10 10:35:26
 */
public class ConcurrentConnectionMonitor extends ExecuteRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentConnectionMonitor.class);

    private final long expiredTimeIntervalMs;
    @Resource
    protected MultiQueue multiQueue;
    @Resource
    protected ConcurrentManager concurrentManager;

    public ConcurrentConnectionMonitor(long checkTimeIntervalMs, long expiredTimeIntervalMs) {
        super("ConcurrentConnectionMonitor", checkTimeIntervalMs);
        this.expiredTimeIntervalMs = expiredTimeIntervalMs;
    }

    @Override
    public void execute() {
        int releaseConnectionNum = 0, scanConcurrentUnitNum = 0;
        Set<String> concurrentUnits = multiQueue.concurrentUnitsSnapshots();
        for (String concurrentUnit : concurrentUnits) {
            scanConcurrentUnitNum++;
            Map<String, Long> connectionRecordMap = concurrentManager.getConnectionRecordMap(concurrentUnit);
            Iterator<Map.Entry<String, Long>> iterator = connectionRecordMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Long> entry = iterator.next();
                String recordKey = entry.getKey();
                Long recordTime = entry.getValue();
                Long currentTime = System.currentTimeMillis();
                long timeInterval = recordTime == null ? currentTime : currentTime - recordTime;
                if (timeInterval < expiredTimeIntervalMs) continue;
                iterator.remove();
                releaseConnectionNum++;
                logger.info("release expired connection[{}] for concurrent unit[{}]", recordKey, concurrentUnit);
            }
        }
        logger.info("scanning concurrent unit num[{}], releasing connection num[{}]",
                scanConcurrentUnitNum, releaseConnectionNum);
    }
}
