package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 任务搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobSearchRequest implements Serializable {

    @JsonProperty("plan_id")
    public String planId;
    @JsonProperty("name")
    public String name;
    @JsonProperty("priority")
    public Integer priority;
    @JsonProperty("status")
    public Integer status;
}
