package xin.manong.darwin.service.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.service.iface.ProxyService;

/**
 * 代理保鲜
 *
 * @author frankcl
 * @date 2023-12-13 17:05:13
 */
public class RefreshProxyKeeper implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RefreshProxyKeeper.class);

    private volatile boolean running;
    private long refreshProxyTimeIntervalMs;
    private Thread thread;
    private ProxyService proxyService;

    public RefreshProxyKeeper(ProxyService proxyService, long refreshProxyTimeIntervalMs) {
        this.running = false;
        this.proxyService = proxyService;
        this.refreshProxyTimeIntervalMs = refreshProxyTimeIntervalMs;
    }

    /**
     * 启动
     *
     * @return 成功返回true，否则返回false
     */
    public boolean start() {
        logger.info("{} is starting ...", this.getClass().getSimpleName());
        running = true;
        thread = new Thread(this, this.getClass().getSimpleName());
        thread.start();
        logger.info("{} has been started", this.getClass().getSimpleName());
        return true;
    }

    /**
     * 停止监控
     */
    public void stop() {
        logger.info("{} is stopping", this.getClass().getSimpleName());
        running = false;
        if (thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.info("{} has been stopped", this.getClass().getSimpleName());
    }

    @Override
    public void run() {
        while (running) {
            try {
                for (int proxyCategory : Constants.SUPPORT_PROXY_CATEGORIES.keySet()) {
                    proxyService.refreshCache(proxyCategory);
                }
                logger.info("finish refreshing proxy cache, sleep {} seconds", refreshProxyTimeIntervalMs);
                Thread.sleep(refreshProxyTimeIntervalMs);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
