package xin.manong.darwin.parser.service.request;

import java.util.Map;

/**
 * HTML及脚本请求构建器
 *
 * @author frankcl
 * @date 2023-08-25 15:08:17
 */
public class HTMLScriptRequestBuilder {

    private HTMLScriptRequest delegate;

    public HTMLScriptRequestBuilder() {
        delegate = new HTMLScriptRequest();
    }

    /**
     * 设置URL
     *
     * @param url URL
     * @return 构建器
     */
    public HTMLScriptRequestBuilder url(String url) {
        delegate.url = url;
        return this;
    }

    /**
     * 设置重定向URL
     *
     * @param redirectURL 重定向URL
     * @return 构建器
     */
    public HTMLScriptRequestBuilder redirectURL(String redirectURL) {
        delegate.redirectURL = redirectURL;
        return this;
    }

    /**
     * 设置待解析内容
     *
     * @param html 待解析HTML
     * @return 构建器
     */
    public HTMLScriptRequestBuilder html(String html) {
        delegate.html = html;
        return this;
    }

    /**
     * 设置脚本类型
     *
     * @param scriptType 脚本类型
     * @return 构建器
     */
    public HTMLScriptRequestBuilder scriptType(int scriptType) {
        delegate.scriptType = scriptType;
        return this;
    }

    /**
     * 设置脚本代码
     *
     * @param scriptCode 脚本代码
     * @return 构建器
     */
    public HTMLScriptRequestBuilder scriptCode(String scriptCode) {
        delegate.scriptCode = scriptCode;
        return this;
    }

    /**
     * 设置全局抽链范围
     *
     * @param scope 全局抽链范围
     * @return 构建器
     */
    public HTMLScriptRequestBuilder scope(int scope) {
        delegate.scope = scope;
        return this;
    }

    /**
     * 设置用户自定义数据
     *
     * @param userDefinedMap 用户自定义数据
     * @return 构建器
     */
    public HTMLScriptRequestBuilder userDefinedMap(Map<String, Object> userDefinedMap) {
        delegate.userDefinedMap = userDefinedMap;
        return this;
    }

    /**
     * 构建请求对象
     *
     * @return 请求对象
     */
    public HTMLScriptRequest build() {
        HTMLScriptRequest request = new HTMLScriptRequest();
        request.html = delegate.html;
        request.url = delegate.url;
        request.redirectURL = delegate.redirectURL;
        request.userDefinedMap = delegate.userDefinedMap;
        request.scriptType = delegate.scriptType;
        request.scriptCode = delegate.scriptCode;
        return request;
    }
}
