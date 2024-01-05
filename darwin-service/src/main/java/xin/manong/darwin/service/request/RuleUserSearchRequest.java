package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 规则用户关系搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleUserSearchRequest extends SearchRequest {

    /**
     * 规则ID
     */
    @JsonProperty("rule_id")
    public Integer ruleId;
    /**
     * 用户真实姓名
     */
    @JsonProperty("real_name")
    public String realName;
    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    public String userId;
}
