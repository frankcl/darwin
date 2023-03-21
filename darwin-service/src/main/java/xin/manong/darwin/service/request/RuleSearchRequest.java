package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 规则搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 17:56:29
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleSearchRequest implements Serializable {

    @JsonProperty("rule_group")
    public Long ruleGroup;
    @JsonProperty("script_type")
    public Integer scriptType;
    @JsonProperty("category")
    public Integer category;
    @JsonProperty("name")
    public String name;
    @JsonProperty("domain")
    public String domain;
}
