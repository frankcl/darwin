package xin.manong.darwin.common.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.URLRecord;

/**
 * 解析请求
 *
 * @author frankcl
 * @date 2023-03-16 15:15:08
 */
public class ParseRequest {

    private static final Logger logger = LoggerFactory.getLogger(ParseRequest.class);

    /**
     * 解析请求构建器
     */
    public static class Builder {
        private ParseRequest template;

        public Builder() {
            template = new ParseRequest();
        }

        public Builder record(URLRecord record) {
            template.record = record;
            return this;
        }

        public Builder content(String content) {
            template.content = content;
            return this;
        }

        public ParseRequest build() {
            ParseRequest request = new ParseRequest();
            request.content = template.content;
            request.record = template.record;
            return request;
        }
    }

    /**
     * 网页内容HTML
     */
    public String content;
    /**
     * 链接信息
     */
    public URLRecord record;

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (record == null || !record.check()) {
            logger.error("url record is null or invalid");
            return false;
        }
        return true;
    }
}
