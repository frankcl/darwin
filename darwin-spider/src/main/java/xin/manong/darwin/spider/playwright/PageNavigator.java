package xin.manong.darwin.spider.playwright;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 页面导航
 *
 * @author frankcl
 * @date 2026-05-04 13:21:03
 */
public class PageNavigator {

    private static final Logger logger = LoggerFactory.getLogger(PageNavigator.class);

    private static final int DEFAULT_TIMEOUT = 30000;
    private static final long STABLE_TIME_INTERVAL_MS = 800;
    private static final long POLL_TIME_INTERVAL_MS = 200;
    private final Page page;

    public PageNavigator(Page page) {
        this.page = page;
    }

    /**
     * 等待导航稳定
     *
     * @param requestURL 请求URL
     * @param timeout 超时时间，单位：毫秒
     * @return 稳定URL
     */
    public String navigate(String requestURL, Integer timeout) {
        try {
            if (timeout == null || timeout <= 0) timeout = DEFAULT_TIMEOUT;
            page.navigate(requestURL, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.COMMIT)
                    .setTimeout(timeout));
            waitStable(timeout);
            page.waitForLoadState(LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(timeout));
        } catch (Exception e) {
            logger.error("Wait for navigating url:{} failed", requestURL);
            logger.error(e.getMessage(), e);
        }
        return page.url();
    }

    /**
     * 等待导航稳定
     *
     * @param timeout 超时时间，单位：毫秒
     * @throws InterruptedException 中断异常
     */
    @SuppressWarnings("BusyWait")
    private void waitStable(int timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout;
        String lastURL = page.url();
        long lastChangeTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(POLL_TIME_INTERVAL_MS);
            String currentURL = page.url();
            if (!currentURL.equals(lastURL)) {
                lastURL = currentURL;
                lastChangeTime = System.currentTimeMillis();
                continue;
            }
            if (System.currentTimeMillis() - lastChangeTime > STABLE_TIME_INTERVAL_MS) return;
        }
    }
}
