package xin.manong.darwin.parser.service.request;

import java.util.Map;

/**
 * 规则解析请求构建器
 *
 * @author frankcl
 * @date 2023-08-25 15:08:17
 */
public class RuleParseRequestBuilder {

    private final RuleParseRequest delegate;

    public RuleParseRequestBuilder() {
        delegate = new RuleParseRequest();
    }

    /**
     * 设置URL
     *
     * @param url URL
     * @return 构建器
     */
    public RuleParseRequestBuilder url(String url) {
        delegate.url = url;
        return this;
    }

    /**
     * 设置重定向URL
     *
     * @param redirectURL 重定向URL
     * @return 构建器
     */
    public RuleParseRequestBuilder redirectURL(String redirectURL) {
        delegate.redirectURL = redirectURL;
        return this;
    }

    /**
     * 设置待解析内容
     *
     * @param text 待解析文本
     * @return 构建器
     */
    public RuleParseRequestBuilder text(String text) {
        delegate.text = text;
        return this;
    }

    /**
     * 设置规则ID
     *
     * @param ruleId 规则ID
     * @return 构建器
     */
    public RuleParseRequestBuilder ruleId(int ruleId) {
        delegate.ruleId = ruleId;
        return this;
    }

    /**
     * 设置抽链范围
     *
     * @param linkScope 抽链范围
     * @return 构建器
     */
    public RuleParseRequestBuilder linkScope(int linkScope) {
        delegate.linkScope = linkScope;
        return this;
    }

    /**
     * 设置用户自定义数据
     *
     * @param customMap 用户自定义数据
     * @return 构建器
     */
    public RuleParseRequestBuilder customMap(Map<String, Object> customMap) {
        delegate.customMap = customMap;
        return this;
    }

    /**
     * 构建请求对象
     *
     * @return 请求对象
     */
    public RuleParseRequest build() {
        RuleParseRequest request = new RuleParseRequest();
        request.text = delegate.text;
        request.url = delegate.url;
        request.linkScope = delegate.linkScope;
        request.redirectURL = delegate.redirectURL;
        request.customMap = delegate.customMap;
        request.ruleId = delegate.ruleId;
        return request;
    }
}
