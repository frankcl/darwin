package xin.manong.darwin.common.parser;

/**
 * 解析请求
 *
 * @author frankcl
 * @date 2023-03-16 15:15:08
 */
public class ParseRequest {

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

        public Builder html(String html) {
            template.html = html;
            return this;
        }

        public ParseRequest build() {
            ParseRequest request = new ParseRequest();
            request.html = template.html;
            request.linkURL = template.linkURL;
            return request;
        }
    }

    /**
     * 网页内容HTML
     */
    public String html;
    /**
     * 链接信息
     */
    public LinkURL linkURL;
}
