package xin.manong.darwin.spider.playwright;

import com.google.gson.Gson;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 会话
 *
 * @author frankcl
 * @date 2026-04-22 10:23:26
 */
public class Session implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Session.class);

    private static final String DOWNLOAD_KEYWORD = "Download is starting";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE_APPLICATION_FORM = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE_MULTIPART_FORM = "multipart/form-data";
    private static final String FETCH_SCRIPT = """
            async ({ url, method, requestBody, headers }) => {
                    function isFileType(mimeType) {
                        return mimeType !== 'application/json' && mimeType !== 'application/xhtml+xml' &&
                                mimeType !== 'application/x-javascript' && mimeType !== 'application/javascript' &&
                                mimeType !== 'application/xml' && mimeType !== '' &&
                                (mimeType.startswith('application/') || mimeType.startswith('image/') ||
                                 mimeType.startswith('video/') || mimeType.startswith('audio/'));
                    }
                    const request = {
                        method: method,
                        credentials: 'include'
                    }
                    if (headers) request.headers = headers;
                    if (requestBody && method.toLowerCase() === 'post') {
                        request.body = requestBody;
                        const contentType = headers ? headers.get('Content-Type') : '';
                        if (contentType === 'multipart/form-data') {
                            const form = new FormData();
                            Object.entries(fields).forEach(([k, v]) => form.append(k, v));
                            request.body = form;
                        }
                    }
                    const resp = await fetch(url, request);
                    const contentType = resp.headers.get('Content-Type') || '';
                    const mimeType = contentType.split(';')[0].trim();
                    const disposition = resp.headers.get('Content-Disposition') || '';
                    if (disposition.includes('attachment') || isFileType(mimeType)) {
                        const blob = await resp.blob();
                        const match = disposition.match(/filename[^;=\\n]*=(['"]*)(.*?)\\1/);
                        const pos = mineType.indexOf('/')
                        let suffix = pos == -1 ? '' : mineType.substring(pos + 1);
                        if (!/^[a-zA-Z0-9]+$/.test(suffix)) suffix = ''
                        const filename = match ? match[2] : (suffix === '' ? 'file' : 'file.' + suffix);
                        const blobUrl = URL.createObjectURL(blob);
                        const a = document.createElement('a');
                        a.href = blobUrl;
                        a.download = filename;
                        document.body.appendChild(a);
                        a.click();
                        document.body.removeChild(a);
                        URL.revokeObjectURL(blobUrl);
                        return {
                            status: resp.status,
                            headers: Object.fromEntries(resp.headers.entries())
                        };
                    } else {
                        const buf = await resp.arrayBuffer();
                        const bytes = new Uint8Array(buf);
                        let bin = '';
                        bytes.forEach(b => bin += String.fromCharCode(b));
                        return {
                            status: resp.status,
                            headers: Object.fromEntries(resp.headers.entries()),
                            base64: btoa(bin)
                        };
                    }
                }
            """;

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
     * 构建请求数据
     *
     * @param request 抓取请求
     * @return 请求数据
     */
    private Map<String, Object> buildRequestMap(FetchRequest request) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("url", request.getRequestURL());
        requestMap.put("method", request.getMethod());
        if (request.getHeaders() != null) requestMap.put("headers", request.getHeaders());
        if (request.getRequestBody() != null && request.getMethod().equalsIgnoreCase("post")) {
            String contentType = getRequestContentType(request);
            Map<String, Object> requestBody = request.getRequestBody();
            switch (contentType) {
                case CONTENT_TYPE_APPLICATION_JSON: requestMap.put("requestBody", new Gson().toJson(requestBody)); break;
                case CONTENT_TYPE_APPLICATION_FORM: requestMap.put("requestBody", encodeFormBody(requestBody)); break;
                case CONTENT_TYPE_MULTIPART_FORM: requestMap.put("requestBody", request.getRequestBody()); break;
                default: {
                    requestMap.put("requestBody", new Gson().toJson(requestBody));
                }
            }
        }
        return requestMap;
    }

    /**
     * 编码form表单数据
     *
     * @param form form表单数据
     * @return 编码结果
     */
    private String encodeFormBody(Map<String, Object> form) {
        return form.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(e.getValue().toString(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    /**
     * 获取HTTP请求头Content-Type
     *
     * @param request 请求
     * @return 存在返回值，否则返回application/json
     */
    private String getRequestContentType(FetchRequest request) {
        if (request.getHeaders() == null) return CONTENT_TYPE_APPLICATION_JSON;
        return request.getHeaders().getOrDefault(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON);
    }

    /**
     * 获取执行结果
     *
     * @param future 异步任务
     * @param timeoutSeconds 超时时间，单位：毫秒
     * @return 结果
     * @param <T> 类型
     */
    private <T> T getFutureQuietly(CompletableFuture<T> future, int timeoutSeconds) {
        try {
            return future.get(timeoutSeconds, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return null;
        }
    }

    public FetchResponse execute(FetchRequest request) {
        beforeFetch(request);
        CompletableFuture<Download> downloadFuture = new CompletableFuture<>();
        CompletableFuture<Map<String, String>> headerFuture  = new CompletableFuture<>();
        page.onDownload(download -> {
            if (!downloadFuture.isDone()) downloadFuture.complete(download);
        });
        page.onResponse(response -> {
            if (response.url().equals(request.getRequestURL()) && !headerFuture.isDone()) {
                headerFuture.complete(response.headers());
            }
        });
        Object raw = page.evaluate(FETCH_SCRIPT, buildRequestMap(request));
        Map<String, String> headers = getFutureQuietly(headerFuture, request.getTimeout());
        String ct = headers.getOrDefault("content-type",
                headers.getOrDefault("Content-Type", ""));
        String cd = headers.getOrDefault("content-disposition",
                headers.getOrDefault("Content-Disposition", ""));
        return null;
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
