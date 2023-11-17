package xin.manong.darwin.parser.service.request;

import java.util.Map;

/**
 * HTML解析请求构建器
 *
 * @author frankcl
 * @date 2023-08-25 15:08:17
 */
public class HTMLParseRequestBuilder {

    private HTMLParseRequest delegate;

    public HTMLParseRequestBuilder() {
        delegate = new HTMLParseRequest();
    }

    /**
     * 设置URL
     *
     * @param url URL
     * @return 构建器
     */
    public HTMLParseRequestBuilder url(String url) {
        delegate.url = url;
        return this;
    }

    /**
     * 设置重定向URL
     *
     * @param redirectURL 重定向URL
     * @return 构建器
     */
    public HTMLParseRequestBuilder redirectURL(String redirectURL) {
        delegate.redirectURL = redirectURL;
        return this;
    }

    /**
     * 设置待解析内容
     *
     * @param html 待解析HTML
     * @return 构建器
     */
    public HTMLParseRequestBuilder html(String html) {
        delegate.html = html;
        return this;
    }

    /**
     * 设置脚本类型
     *
     * @param scriptType 脚本类型
     * @return 构建器
     */
    public HTMLParseRequestBuilder scriptType(int scriptType) {
        delegate.scriptType = scriptType;
        return this;
    }

    /**
     * 设置脚本代码
     *
     * @param scriptCode 脚本代码
     * @return 构建器
     */
    public HTMLParseRequestBuilder scriptCode(String scriptCode) {
        delegate.scriptCode = scriptCode;
        return this;
    }

    /**
     * 设置全局抽链范围
     *
     * @param scope 全局抽链范围
     * @return 构建器
     */
    public HTMLParseRequestBuilder scope(int scope) {
        delegate.scope = scope;
        return this;
    }

    /**
     * 设置页面类型
     *
     * @param category 页面类型
     * @return 构建器
     */
    public HTMLParseRequestBuilder category(int category) {
        delegate.category = category;
        return this;
    }

    /**
     * 设置用户自定义数据
     *
     * @param userDefinedMap 用户自定义数据
     * @return 构建器
     */
    public HTMLParseRequestBuilder userDefinedMap(Map<String, Object> userDefinedMap) {
        delegate.userDefinedMap = userDefinedMap;
        return this;
    }

    /**
     * 构建请求对象
     *
     * @return 请求对象
     */
    public HTMLParseRequest build() {
        HTMLParseRequest request = new HTMLParseRequest();
        request.html = delegate.html;
        request.url = delegate.url;
        request.scope = delegate.scope;
        request.category = delegate.category;
        request.redirectURL = delegate.redirectURL;
        request.userDefinedMap = delegate.userDefinedMap;
        request.scriptType = delegate.scriptType;
        request.scriptCode = delegate.scriptCode;
        return request;
    }
}
