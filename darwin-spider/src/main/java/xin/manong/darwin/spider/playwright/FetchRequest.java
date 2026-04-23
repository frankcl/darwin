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

    private final String requestURL;
    private final Map<String, String> headers;
    private final Integer timeout;

    private FetchRequest(Builder builder) {
        requestURL = builder.requestURL;
        headers = builder.headers;
        timeout = builder.timeout == null || builder.timeout <= 0 ? DEFAULT_TIMEOUT : builder.timeout;
    }

    /**
     * 检测参数有效性，无效参数抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(requestURL)) throw new IllegalArgumentException("requestURL is empty");
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
        private Map<String, String> headers;
        private Integer timeout;

        public Builder requestURL(String requestURL) {
            this.requestURL = requestURL;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder timeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public FetchRequest build() {
            return new FetchRequest(this);
        }
    }
}
