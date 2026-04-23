package xin.manong.darwin.spider.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.weapon.base.util.RandomID;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.file.Path;

/**
 * 会话
 *
 * @author frankcl
 * @date 2026-04-22 10:23:26
 */
public class Session implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Session.class);

    private static final String DOWNLOAD_KEYWORD = "Download is starting";

    @Getter
    private boolean open;
    @Getter
    private final String id;
    private final BrowserContext browserContext;
    private final Page page;
    private Download download;
    @Setter
    private String tempDirectory;

    private Session(BrowserContext browserContext) {
        this.browserContext = browserContext;
        this.page = browserContext.newPage();
        this.id = RandomID.build();
        this.open = true;
    }

    /**
     * 抓取前准备
     *
     * @param request 抓取请求
     */
    private void beforeFetch(FetchRequest request) {
        if (!open) throw new IllegalStateException("Session is not open");
        Validate.notNull(request, "Request is null");
        request.check();
        if (request.getHeaders() != null) page.setExtraHTTPHeaders(request.getHeaders());
    }

    /**
     * 保存下载文件
     *
     * @param download 下载数据
     * @param builder 响应构建器
     * @return 成功返回true，否则返回false
     */
    private boolean saveDownloadedFile(Download download, FetchResponse.Builder builder) {
        try {
            String savedFile = tempDirectory + RandomID.build();
            download.saveAs(Path.of(savedFile));
            builder.tempFile(savedFile).responseBody(new FileInputStream(savedFile));
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 抓取数据
     *
     * @param request 请求
     * @return 响应
     */
    public FetchResponse fetch(FetchRequest request) {
        beforeFetch(request);
        FetchResponse.Builder builder = FetchResponse.builder();
        try {
            Response response = page.navigate(request.getRequestURL(), new Page.NavigateOptions().
                    setWaitUntil(WaitUntilState.COMMIT).setTimeout(request.getTimeout()));
            builder.status(response.ok()).url(response.url()).httpCode(response.status()).headers(response.headers());
            if (response.ok()) {
                byte[] body = response.body();
                builder.contentLength(body.length);
                builder.responseBody(new ByteArrayInputStream(body));
            }
            return builder.build();
        } catch (PlaywrightException e) {
            if (!e.getMessage().contains(DOWNLOAD_KEYWORD)) {
                logger.error(e.getMessage(), e);
                return builder.status(false).build();
            }
            return download(request);
        }
    }

    /**
     * 下载文件
     *
     * @param request 请求
     * @return 响应
     */
    public FetchResponse download(FetchRequest request) {
        beforeFetch(request);
        FetchResponse.Builder builder = FetchResponse.builder();
        page.onResponse(response -> {
            if (response.url().equals(request.getRequestURL()) && response.headers() != null) {
                builder.headers(response.headers());
            }
        });
        download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(request.getTimeout()),
                () -> {
                    try {
                        page.navigate(request.getRequestURL(), new Page.NavigateOptions().
                                setWaitUntil(WaitUntilState.COMMIT));
                    } catch (Exception e) {
                        if (!e.getMessage().contains(DOWNLOAD_KEYWORD)) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
        );
        if (download == null) return builder.status(false).build();
        builder.url(download.url());
        return saveDownloadedFile(download, builder) ?
                builder.status(true).httpCode(200).build() : builder.status(false).build();
    }

    /**
     * 新建会话
     *
     * @param browserContext 浏览器上下文
     * @return 新会话
     */
    static Session buildSession(BrowserContext browserContext) {
        return new Session(browserContext);
    }

    @Override
    public void close() throws Exception {
        if (!open) return;
        if (download != null) download.delete();
        if (page != null) page.close();
        if (browserContext != null) browserContext.close();
        open = false;
    }
}
