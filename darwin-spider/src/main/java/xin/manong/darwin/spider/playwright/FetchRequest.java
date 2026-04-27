package xin.manong.darwin.spider.playwright;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 抓取请求
 *
 * @author frankcl
 * @date 2026-04-22 10:32:59
 */
@Getter
public class FetchRequest {

    private static final int DEFAULT_TIMEOUT = 30000;
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    private final String requestURL;
    private final String method;
    private final Map<String, String> headers;
    private final Map<String, Object> requestBody;
    private final Integer timeout;

    private FetchRequest(Builder builder) {
        requestURL = builder.requestURL;
        method = StringUtils.isEmpty(builder.method) ? METHOD_GET : builder.method;
        headers = builder.headers;
        requestBody = builder.requestBody;
        timeout = builder.timeout == null || builder.timeout <= 0 ? DEFAULT_TIMEOUT : builder.timeout;
    }

    /**
     * 检测参数有效性，无效参数抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(requestURL)) throw new IllegalArgumentException("requestURL is empty");
        if (!METHOD_GET.equalsIgnoreCase(method) && !METHOD_POST.equalsIgnoreCase(method)) {
            throw new IllegalArgumentException(String.format("Invalid method: %s", method));
        }
    }

    /**
     * 新建构建器
     *
     * @return 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 构建器
     */
    public static class Builder {
        private String requestURL;
        private String method;
        private Map<String, String> headers;
        private Map<String, Object> requestBody;
        private Integer timeout;

        public Builder requestURL(String requestURL) {
            this.requestURL = requestURL;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder requestBody(Map<String, Object> requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder timeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public FetchRequest build() {
            return new FetchRequest(this);
        }
    }
}
