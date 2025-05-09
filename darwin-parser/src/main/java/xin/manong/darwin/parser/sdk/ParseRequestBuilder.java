package xin.manong.darwin.parser.sdk;

import java.util.Map;

/**
 * 解析请求构建器
 *
 * @author frankcl
 * @date 2023-08-25 15:08:17
 */
public class ParseRequestBuilder {

    private final ParseRequest delegate;

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
     * @param text 待解析文本
     * @return 构建器
     */
    public ParseRequestBuilder text(String text) {
        delegate.text = text;
        return this;
    }

    /**
     * 设置抽链范围
     *
     * @param linkScope 抽链范围
     * @return 构建器
     */
    public ParseRequestBuilder linkScope(int linkScope) {
        delegate.linkScope = linkScope;
        return this;
    }

    /**
     * 设置用户自定义数据
     *
     * @param customMap 用户自定义数据
     * @return 构建器
     */
    public ParseRequestBuilder customMap(Map<String, Object> customMap) {
        delegate.customMap = customMap;
        return this;
    }

    /**
     * 构建请求对象
     *
     * @return 请求对象
     */
    public ParseRequest build() {
        ParseRequest request = new ParseRequest();
        request.text = delegate.text;
        request.url = delegate.url;
        request.linkScope = delegate.linkScope;
        request.redirectURL = delegate.redirectURL;
        request.customMap = delegate.customMap;
        return request;
    }
}
