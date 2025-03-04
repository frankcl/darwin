package xin.manong.darwin.spider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 放回策略：针对拒绝任务，将URL记录放回队列
 *
 * @author frankcl
 * @date 2023-03-24 15:33:35
 */
public class PushBackPolicy implements RejectedExecutionHandler {

    private static final Logger logger = LoggerFactory.getLogger(PushBackPolicy.class);

    private final BlockingQueue<SpiderRecord> recordQueue;

    public PushBackPolicy(BlockingQueue<SpiderRecord> recordQueue) {
        this.recordQueue = recordQueue;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (!(r instanceof SpiderTask spiderTask)) return;
        if (spiderTask.spiderRecord == null) return;
        try {
            recordQueue.put(spiderTask.spiderRecord);
        } catch (Exception e) {
            logger.error("push back record[{}] failed", spiderTask.spiderRecord.record.key);
            logger.error(e.getMessage(), e);
        }
    }
}
