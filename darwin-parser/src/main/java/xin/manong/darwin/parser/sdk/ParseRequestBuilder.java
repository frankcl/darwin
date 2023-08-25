package xin.manong.darwin.parser.sdk;

import java.util.Map;

/**
 * 解析请求构建器
 *
 * @author frankcl
 * @date 2023-08-25 15:08:17
 */
public class ParseRequestBuilder {

    private ParseRequest delegate;

    public ParseRequestBuilder() {
        delegate = new ParseRequest();
    }

    /**
     * 设置URL
     *
     * @param url URL
     * @return 构建器
     */
    public ParseRequestBuilder url(String url) {
        delegate.url = url;
        return this;
    }

    /**
     * 设置重定向URL
     *
     * @param redirectURL 重定向URL
     * @return 构建器
     */
    public ParseRequestBuilder redirectURL(String redirectURL) {
        delegate.redirectURL = redirectURL;
        return this;
    }

    /**
     * 设置待解析内容
     *
     * @param html 待解析HTML
     * @return 构建器
     */
    public ParseRequestBuilder html(String html) {
        delegate.html = html;
        return this;
    }

    /**
     * 设置用户自定义数据
     *
     * @param userDefinedMap 用户自定义数据
     * @return 构建器
     */
    public ParseRequestBuilder userDefinedMap(Map<String, Object> userDefinedMap) {
        delegate.userDefinedMap = userDefinedMap;
        return this;
    }

    /**
     * 构建请求对象
     *
     * @return 请求对象
     */
    public ParseRequest build() {
        ParseRequest request = new ParseRequest();
        request.html = delegate.html;
        request.url = delegate.url;
        request.redirectURL = delegate.redirectURL;
        request.userDefinedMap = delegate.userDefinedMap;
        return request;
    }
}
