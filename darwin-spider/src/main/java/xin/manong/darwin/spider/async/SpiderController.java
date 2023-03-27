package xin.manong.darwin.spider.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.spider.function.SpiderFactory;
import xin.manong.weapon.base.log.JSONLogger;

import javax.annotation.Resource;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 爬虫控制器
 *
 * @author frankcl
 * @date 2023-03-24 15:04:39
 */
public class SpiderController implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SpiderController.class);

    private static final int TASK_QUEUE_SIZE = 50;

    private boolean running;
    private String name;
    private Thread thread;
    protected BlockingQueue<SpiderRecord> recordQueue;
    protected ExecutorService executorService;
    protected JSONLogger aspectLogger;
    @Resource
    protected SpiderFactory spiderFactory;

    public SpiderController(String name, BlockingQueue<SpiderRecord> recordQueue, int spiderNum) {
        this.running = false;
        this.name = name;
        this.recordQueue = recordQueue;
        this.executorService = new ThreadPoolExecutor(spiderNum, spiderNum, 60L,
                TimeUnit.SECONDS, new ArrayBlockingQueue(TASK_QUEUE_SIZE),
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger();
                    public Thread newThread(Runnable task) {
                        String threadName = String.format("%s-%d", name, this.counter.getAndIncrement());
                        logger.info("create spider thread[{}] success", threadName);
                        return new Thread(task, threadName);
                    }},
                new PushBackPolicy(recordQueue));
    }

    /**
     * 启动爬虫控制器
     */
    public void start() {
        logger.info("spider controller[{}] is starting ...", name);
        running = true;
        thread = new Thread(this, name);
        thread.start();
        logger.info("spider controller[{}] has been started", name);
    }

    /**
     * 停止爬虫控制器
     */
    public void stop() {
        logger.info("spider controller[{}] is stopping ...", name);
        running = false;
        if (thread.isAlive()) thread.interrupt();
        try {
            thread.join();
            if (executorService != null) executorService.shutdown();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        logger.info("spider controller[{}] has been stopped", name);
    }

    @Override
    public void run() {
        while (running) {
            try {
                SpiderRecord spiderRecord = recordQueue.poll(3, TimeUnit.SECONDS);
                if (spiderRecord == null) continue;
                SpiderTask spiderTask = new SpiderTask(spiderRecord, spiderFactory);
                spiderTask.setAspectLogger(aspectLogger);
                executorService.submit(spiderTask);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    /**
     * 设置切面日志
     *
     * @param aspectLogger 切面日志
     */
    public void setAspectLogger(JSONLogger aspectLogger) {
        this.aspectLogger = aspectLogger;
    }
}
