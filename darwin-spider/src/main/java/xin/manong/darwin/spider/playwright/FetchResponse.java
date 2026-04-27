package xin.manong.darwin.spider.playwright;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 抓取响应
 *
 * @author frankcl
 * @date 2026-04-22 10:32:59
 */
@Getter
public class FetchResponse implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(FetchResponse.class);

    private final boolean status;
    private final int httpCode;
    private final long contentLength;
    private final String url;
    private final String message;
    private final String tempFile;
    private final Map<String, String> headers;
    private final InputStream responseBody;

    private FetchResponse(Builder builder) {
        status = builder.status;
        httpCode = builder.httpCode;
        contentLength = builder.contentLength;
        headers = builder.headers;
        url = builder.url;
        message = builder.message;
        tempFile = builder.tempFile;
        responseBody = builder.responseBody;
    }

    @Override
    public void close() throws IOException {
        if (responseBody != null) responseBody.close();
        if (StringUtils.isNotEmpty(tempFile)) {
            if (!new File(tempFile).delete()) logger.warn("Delete temp file failed for {}", tempFile);
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
        private boolean status;
        private int httpCode;
        private long contentLength;
        private String message;
        private String tempFile;
        private String url;
        private Map<String, String> headers;
        private InputStream responseBody;

        public Builder httpCode(int httpCode) {
            this.httpCode = httpCode;
            return this;
        }

        public Builder contentLength(long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder tempFile(String tempFile) {
            this.tempFile = tempFile;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder responseBody(InputStream responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public Builder status(boolean status) {
            this.status = status;
            return this;
        }

        public Builder copy(FetchResponse fetchResponse) {
            this.status = fetchResponse.status;
            this.httpCode = fetchResponse.httpCode;
            this.contentLength = fetchResponse.contentLength;
            this.message = fetchResponse.message;
            this.tempFile = fetchResponse.tempFile;
            this.url = fetchResponse.url;
            this.headers = fetchResponse.headers;
            this.responseBody = fetchResponse.responseBody;
            return this;
        }

        public FetchResponse build() {
            return new FetchResponse(this);
        }
    }
}
