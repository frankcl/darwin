package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 计划搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanSearchRequest extends SearchRequest {

    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    public Integer appId;
    /**
     * 计划分类：一次性计划任务0，周期性计划任务1，消费型计划任务2
     */
    @JsonProperty("category")
    public Integer category;
    /**
     * 计划名称
     */
    @JsonProperty("name")
    public String name;
    /**
     * 优先级：高0，正常1，低2
     */
    @JsonProperty("priority")
    public Integer priority;
    /**
     * 计划状态：停止0，运行1
     */
    @JsonProperty("status")
    public Integer status;
}
