package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 计划搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanSearchRequest implements Serializable {

    @JsonProperty("app_id")
    public Integer appId;
    @JsonProperty("category")
    public Integer category;
    @JsonProperty("name")
    public String name;
    @JsonProperty("priority")
    public Integer priority;
    @JsonProperty("status")
    public Integer status;
}
