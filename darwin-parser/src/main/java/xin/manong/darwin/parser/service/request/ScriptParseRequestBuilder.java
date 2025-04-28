package xin.manong.darwin.parser.service.request;

import java.util.Map;

/**
 * 脚本解析请求构建器
 *
 * @author frankcl
 * @date 2023-08-25 15:08:17
 */
public class ScriptParseRequestBuilder {

    private final ScriptParseRequest delegate;

    public ScriptParseRequestBuilder() {
        delegate = new ScriptParseRequest();
    }

    /**
     * 设置URL
     *
     * @param url URL
     * @return 构建器
     */
    public ScriptParseRequestBuilder url(String url) {
        delegate.url = url;
        return this;
    }

    /**
     * 设置重定向URL
     *
     * @param redirectURL 重定向URL
     * @return 构建器
     */
    public ScriptParseRequestBuilder redirectURL(String redirectURL) {
        delegate.redirectURL = redirectURL;
        return this;
    }

    /**
     * 设置待解析内容
     *
     * @param text 待解析文本
     * @return 构建器
     */
    public ScriptParseRequestBuilder text(String text) {
        delegate.text = text;
        return this;
    }

    /**
     * 设置脚本类型
     *
     * @param scriptType 脚本类型
     * @return 构建器
     */
    public ScriptParseRequestBuilder scriptType(int scriptType) {
        delegate.scriptType = scriptType;
        return this;
    }

    /**
     * 设置脚本代码
     *
     * @param scriptCode 脚本代码
     * @return 构建器
     */
    public ScriptParseRequestBuilder scriptCode(String scriptCode) {
        delegate.scriptCode = scriptCode;
        return this;
    }

    /**
     * 设置抽链范围
     *
     * @param linkScope 抽链范围
     * @return 构建器
     */
    public ScriptParseRequestBuilder linkScope(int linkScope) {
        delegate.linkScope = linkScope;
        return this;
    }

    /**
     * 设置用户自定义数据
     *
     * @param customMap 用户自定义数据
     * @return 构建器
     */
    public ScriptParseRequestBuilder customMap(Map<String, Object> customMap) {
        delegate.customMap = customMap;
        return this;
    }

    /**
     * 构建请求对象
     *
     * @return 请求对象
     */
    public ScriptParseRequest build() {
        ScriptParseRequest request = new ScriptParseRequest();
        request.text = delegate.text;
        request.url = delegate.url;
        request.linkScope = delegate.linkScope;
        request.redirectURL = delegate.redirectURL;
        request.customMap = delegate.customMap;
        request.scriptType = delegate.scriptType;
        request.scriptCode = delegate.scriptCode;
        return request;
    }
}
