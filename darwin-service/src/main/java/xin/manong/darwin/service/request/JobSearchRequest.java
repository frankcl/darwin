package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 任务搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobSearchRequest extends SearchRequest {

    /**
     * 计划ID
     */
    @JsonProperty("plan_id")
    public String planId;
    /**
     * 任务名称
     */
    @JsonProperty("name")
    public String name;
    /**
     * 优先级：高0，正常1，低2
     */
    @JsonProperty("priority")
    public Integer priority;
    /**
     * 任务状态：结束0，运行1
     */
    @JsonProperty("status")
    public Integer status;
}
