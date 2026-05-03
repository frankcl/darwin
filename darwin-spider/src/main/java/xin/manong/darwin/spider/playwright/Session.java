package xin.manong.darwin.spider.playwright;

import com.google.gson.Gson;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.weapon.base.util.RandomID;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会话
 *
 * @author frankcl
 * @date 2026-04-22 10:23:26
 */
public class Session implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(Session.class);

    private static final String DOWNLOAD_KEYWORD = "Download is starting";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_HOST = "Host";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE_APPLICATION_FORM = "application/x-www-form-urlencoded";

    private static final String REQUEST_PARAM_URL = "url";
    private static final String REQUEST_PARAM_METHOD = "method";
    private static final String REQUEST_PARAM_HEADERS = "headers";
    private static final String REQUEST_PARAM_REQUEST_BODY = "requestBody";

    private static final String RESPONSE_PARAM_STATUS = "status";
    private static final String RESPONSE_PARAM_URL = "url";
    private static final String RESPONSE_PARAM_MESSAGE = "message";
    private static final String RESPONSE_PARAM_HTTP_CODE = "httpCode";
    private static final String RESPONSE_PARAM_HEADERS = "headers";
    private static final String RESPONSE_PARAM_BASE64 = "base64";

    private static final String FETCH_SCRIPT = """
            async ({ url, method, requestBody, headers }) => {
                    function isFileType(mimeType) {
                        mimeType = mimeType.toLowerCase();
                        return mimeType !== 'application/json' && mimeType !== 'application/xhtml+xml' &&
                                mimeType !== 'application/x-javascript' && mimeType !== 'application/javascript' &&
                                mimeType !== 'application/xml' && mimeType !== '' &&
                                (mimeType.startsWith('application/') || mimeType.startsWith('image/') ||
                                 mimeType.startsWith('video/') || mimeType.startsWith('audio/'));
                    }
                    function parseFilename(mimeType, disposition) {
                        const match = disposition.match(/filename[^;=\\n]*=(['"]*)(.*?)\\1/);
                        if (match) return match[2]
                        const pos = mimeType.indexOf('/');
                        let suffix = pos == -1 ? '' : mimeType.substring(pos + 1);
                        if (!/^[a-zA-Z0-9]+$/.test(suffix)) suffix = '';
                        return suffix === '' ? 'file' : 'file.' + suffix;
                    }
                    function toBase64(buf) {
                        const bytes = new Uint8Array(buf);
                        const chunkSize = 8192;
                        let bin = '';
                        for (let i = 0; i < bytes.length; i += chunkSize) {
                            bin += String.fromCharCode(...bytes.subarray(i, i + chunkSize));
                        }
                        return btoa(bin);
                    }
                    try {
                        const request = {
                            method: method,
                            credentials: 'include'
                        };
                        if (headers) request.headers = headers;
                        if (requestBody && method.toLowerCase() === 'post') {
                            request.body = requestBody;
                            const contentType = headers ? headers['Content-Type'] : '';
                            if (contentType === 'multipart/form-data') {
                                const form = new FormData();
                                Object.entries(requestBody).forEach(([k, v]) => form.append(k, v));
                                request.body = form;
                                if (request.headers) delete request.headers['Content-Type']
                            }
                        }
                        const resp = await fetch(url, request);
                        const contentType = resp.headers.get('Content-Type') || '';
                        const mimeType = contentType.split(';')[0].trim();
                        const disposition = resp.headers.get('Content-Disposition') || '';
                        const isFile = disposition.toLowerCase().includes('attachment') || isFileType(mimeType);
                        const buf = await resp.arrayBuffer();
                        return {
                            status: true,
                            httpCode: resp.status,
                            url: resp.url,
                            isFile,
                            filename: isFile ? parseFilename(mimeType, disposition) : null,
                            headers: Object.fromEntries(resp.headers.entries()),
                            base64: toBase64(buf)
                        };
                    } catch (e) {
                        return {
                            status: false,
                            message: e.message
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
        if (request.getHeaders() != null) {
            Map<String, String> headers = new HashMap<>(request.getHeaders());
            headers.remove(HEADER_HOST);
            page.setExtraHTTPHeaders(headers);
        }
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
        requestMap.put(REQUEST_PARAM_URL, request.getRequestURL());
        requestMap.put(REQUEST_PARAM_METHOD, request.getMethod());
        if (request.getHeaders() != null) requestMap.put(REQUEST_PARAM_HEADERS, request.getHeaders());
        if (request.getRequestBody() != null && request.getMethod().equalsIgnoreCase(FetchRequest.METHOD_POST)) {
            String contentType = getRequestContentType(request);
            Map<String, Object> requestBody = request.getRequestBody();
            switch (contentType) {
                case CONTENT_TYPE_APPLICATION_JSON: requestMap.put(REQUEST_PARAM_REQUEST_BODY, new Gson().toJson(requestBody)); break;
                case CONTENT_TYPE_APPLICATION_FORM: requestMap.put(REQUEST_PARAM_REQUEST_BODY, encodeFormBody(requestBody)); break;
                default: {
                    requestMap.put(REQUEST_PARAM_REQUEST_BODY, new Gson().toJson(requestBody));
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
     * 抓取数据
     *
     * @param request 抓取请求
     * @return 抓取响应
     */
    public FetchResponse fetch(FetchRequest request) {
        beforeFetch(request);
        long startTime = System.currentTimeMillis();
        page.onConsoleMessage(msg -> logger.info("JavaScript console message:{}", msg.text()));
        Object response = page.evaluate(FETCH_SCRIPT, buildRequestMap(request));
        FetchResponse fetchResponse = parseResponse(response);
        logger.info("Finish fetching url:{}, status:{}, http code:{}, cost is {} ms",
                request.getRequestURL(), fetchResponse.isStatus(), fetchResponse.getHttpCode(),
                System.currentTimeMillis() - startTime);
        return fetchResponse;
    }

    /**
     * 解析抓取响应
     *
     * @param o 浏览器响应
     * @return 抓取响应
     */
    @SuppressWarnings("unchecked")
    private FetchResponse parseResponse(Object o) {
        if (!(o instanceof Map)) return FetchResponse.builder().status(false).build();
        Map<String, Object> response = (Map<String, Object>) o;
        FetchResponse.Builder builder = FetchResponse.builder();
        Boolean status = (Boolean) response.get(RESPONSE_PARAM_STATUS);
        builder.status(status != null ? status : false);
        if (status == null || !status) return builder.message((String) response.get(RESPONSE_PARAM_MESSAGE)).build();
        builder.url((String) response.get(RESPONSE_PARAM_URL)).httpCode((Integer) response.get(RESPONSE_PARAM_HTTP_CODE)).
                headers((Map<String, String>) response.get(RESPONSE_PARAM_HEADERS));
        String base64 = (String) response.get(RESPONSE_PARAM_BASE64);
        if (StringUtils.isNotEmpty(base64)) {
            byte[] bytes = Base64.getDecoder().decode(base64);
            builder.contentLength(bytes.length).responseBody(new ByteArrayInputStream(bytes));
        }
        return builder.build();
    }
    
    /**
     * 抓取数据
     *
     * @param request 请求
     * @return 响应
     */
    public FetchResponse navigate(FetchRequest request) {
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
    public void close() throws IOException {
        if (!open) return;
        if (download != null) download.delete();
        if (page != null) page.close();
        if (browserContext != null) browserContext.close();
        open = false;
    }
}
