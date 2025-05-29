package xin.manong.darwin.runner.monitor;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.weapon.base.executor.ExecuteRunner;

/**
 * 过期数据清理
 *
 * @author frankcl
 * @date 2025-05-29 12:55:46
 */
public class ExpiredCleaner extends ExecuteRunner {

    private static final Logger logger = LoggerFactory.getLogger(ExpiredCleaner.class);

    public static final String ID = "ExpiredCleaner";

    private final long maxExpiredIntervalMs;
    @Resource
    private JobService jobService;
    @Resource
    private URLService urlService;

    public ExpiredCleaner(long maxExpiredIntervalMs, long executeTimeIntervalMs) {
        super(ID, executeTimeIntervalMs);
        this.maxExpiredIntervalMs = maxExpiredIntervalMs;
        this.setName("过期数据清理器");
        this.setDescription("负责定时清理过期抓取任务及抓取数据，避免数据积压");
    }

    @Override
    public void execute() throws Exception {
        long expiredTime = System.currentTimeMillis() - maxExpiredIntervalMs;
        int expiredRecords = urlService.deleteExpired(expiredTime);
        int expiredJobs = jobService.deleteExpired(expiredTime);
        logger.info("Delete expired record count:{}, job count:{}", expiredRecords, expiredJobs);
    }
}
