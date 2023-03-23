package xin.manong.darwin.common.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        public Builder linkURL(LinkURL linkURL) {
            template.linkURL = linkURL;
            return this;
        }

        public Builder content(String content) {
            template.content = content;
            return this;
        }

        public ParseRequest build() {
            ParseRequest request = new ParseRequest();
            request.content = template.content;
            request.linkURL = template.linkURL;
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
    public LinkURL linkURL;

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (linkURL == null || !linkURL.check()) {
            logger.error("link url is null or invalid");
            return false;
        }
        return true;
    }
}
