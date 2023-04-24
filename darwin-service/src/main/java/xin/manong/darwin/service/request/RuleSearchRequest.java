package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 规则搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 17:56:29
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleSearchRequest extends SearchRequest {

    /**
     * 规则分组ID
     */
    @JsonProperty("rule_group")
    public Long ruleGroup;
    /**
     * 脚本类型：Groovy脚本1，JavaScript脚本2
     */
    @JsonProperty("script_type")
    public Integer scriptType;
    /**
     * 规则分类：抽链规则1，结构化规则2，全局抽链规则3
     */
    @JsonProperty("category")
    public Integer category;
    /**
     * 规则名称
     */
    @JsonProperty("name")
    public String name;
    /**
     * 规则域名
     */
    @JsonProperty("domain")
    public String domain;
}
