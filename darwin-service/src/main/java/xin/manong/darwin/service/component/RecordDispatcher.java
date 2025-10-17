package xin.manong.darwin.service.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.iface.URLService;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * URL数据分发
 *
 * @author frankcl
 * @date 2025-10-17 10:43:19
 */
public class RecordDispatcher implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RecordDispatcher.class);

    private final BlockingQueue<URLRecord> queue;
    private final URLService urlService;
    private volatile boolean running;
    private Thread thread;

    public RecordDispatcher(URLService urlService) {
        queue = new ArrayBlockingQueue<>(500);
        this.urlService = urlService;
    }

    /**
     * 启动
     */
    public void start() {
        running = true;
        thread = new Thread(this, this.getClass().getSimpleName());
        thread.start();
        logger.info("{} has been started", this.getClass().getSimpleName());
    }

    /**
     * 停止
     */
    public void stop() {
        running = false;
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            thread = null;
            logger.info("{} has been stopped", this.getClass().getSimpleName());
        }
    }

    /**
     * 推送分发数据
     *
     * @param record 数据
     * @return 成功返回true，否则返回false
     */
    public boolean push(URLRecord record) {
        try {
            if (record == null || record.status != Constants.URL_STATUS_FETCH_SUCCESS) return false;
            queue.put(record);
            return true;
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                URLRecord record = queue.poll(3, TimeUnit.SECONDS);
                if (record == null) continue;
                urlService.dispatch(record);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
