package xin.manong.darwin.spider.fetcher;

import kotlin.Pair;
import lombok.Getter;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.model.MediaType;
import xin.manong.darwin.spider.playwright.FetchResponse;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 抓取响应
 *
 * @author frankcl
 * @date 2026-04-22 17:02:07
 */
public class Response<T> implements AutoCloseable {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private final T httpResponse;
    @Getter
    private int httpCode;
    @Getter
    private long contentLength;
    @Getter
    private boolean status;
    @Getter
    private String url;
    @Getter
    private MediaType mediaType;
    @Getter
    private Map<String, String> headers;
    @Getter
    private InputStream responseBody;

    private Response(T httpResponse) {
        this.httpResponse = httpResponse;
    }

    /**
     * 构建响应
     *
     * @param httpResponse okhttp3响应
     * @return 响应
     */
    public static Response<okhttp3.Response> buildResponse(okhttp3.Response httpResponse) {
        Response<okhttp3.Response> response = new Response<>(httpResponse);
        response.url = httpResponse.request().url().toString();
        response.httpCode = httpResponse.code();
        response.status = httpResponse.isSuccessful();
        if (!response.status) return response;
        ResponseBody responseBody = httpResponse.body();
        assert responseBody != null;
        response.contentLength = responseBody.contentLength();
        okhttp3.MediaType mediaType = responseBody.contentType();
        if (mediaType != null) {
            Charset charset = mediaType.charset();
            response.mediaType = new MediaType(mediaType.type(), mediaType.subtype());
            if (charset != null) response.mediaType.charset = charset.name();
        }
        response.headers = new HashMap<>();
        for (Pair<? extends String, ? extends String> header : httpResponse.headers()) {
            response.headers.put(header.getFirst(), header.getSecond());
        }
        response.responseBody = responseBody.byteStream();
        return response;
    }

    /**
     * 构建响应
     *
     * @param httpResponse 浏览器响应
     * @return 响应
     */
    public static Response<FetchResponse> buildResponse(FetchResponse httpResponse) {
        Response<FetchResponse> response = new Response<>(httpResponse);
        response.url = httpResponse.getUrl();
        response.httpCode = httpResponse.getHttpCode();
        response.status = httpResponse.isStatus();
        response.headers = httpResponse.getHeaders();
        response.contentLength = httpResponse.getContentLength();
        response.responseBody = httpResponse.getResponseBody();
        response.mediaType = parseMediaType(httpResponse);
        return response;
    }

    /**
     * 获取HTTP响应头
     *
     * @param name HTTP头名称
     * @return 响应头值，不存在返回null
     */
    public String getHeader(String name) {
        if (headers == null || !headers.containsKey(name)) return null;
        return headers.getOrDefault(name, null);
    }

    /**
     * 从HTTP header中解析媒体类型
     *
     * @param httpResponse 浏览器抓取响应
     * @return 媒体类型
     */
    private static MediaType parseMediaType(FetchResponse httpResponse) {
        Map<String, String> headers = httpResponse.getHeaders();
        if (headers == null || !headers.containsKey(HEADER_CONTENT_TYPE)) return null;
        String contentType = headers.get(HEADER_CONTENT_TYPE);
        if (StringUtils.isEmpty(contentType)) return null;
        String[] parts = contentType.split(";");
        String mimeType = parts[0].trim();
        int pos = mimeType.indexOf("/");
        String type = pos == -1 ? mimeType : mimeType.substring(0, pos).trim();
        String subType = pos == -1 ? null : mimeType.substring(pos + 1).trim();
        MediaType mediaType = new MediaType(type, subType);
        for (int i = 1; i < parts.length; i++) {
            pos = parts[i].indexOf("=");
            if (pos == -1) continue;
            String key = parts[i].substring(0, pos).trim();
            String value = parts[i].substring(pos + 1).trim();
            if (!key.equalsIgnoreCase("charset")) continue;
            mediaType.charset = value;
        }
        return mediaType;
    }

    @Override
    public void close() throws Exception {
        if (httpResponse != null) {
            if (httpResponse instanceof okhttp3.Response) ((okhttp3.Response) httpResponse).close();
            if (httpResponse instanceof FetchResponse) ((FetchResponse) httpResponse).close();
        }
    }
}
