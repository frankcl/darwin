package xin.manong.darwin.spider.fetcher;

import lombok.Getter;

import java.util.Map;

/**
 * 抓取请求
 *
 * @author frankcl
 * @date 2026-04-22 17:01:58
 */
@Getter
public class Request {

    private String requestURL;
    private Map<String, String> headers;
    private Integer timeout;

    private Request(Request request) {
        requestURL = request.getRequestURL();
        headers = request.getHeaders();
        timeout = request.getTimeout();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Request request;

        public Builder requestURL(String requestURL) {
            request.requestURL = requestURL;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            request.headers = headers;
            return this;
        }

        public Builder timeout(Integer timeout) {
            request.timeout = timeout;
            return this;
        }

        public Request build() {
            return new Request(request);
        }
    }
}
