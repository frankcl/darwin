package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.io.Serial;

/**
 * 计划搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanSearchRequest extends SearchRequest {

    @Serial
    private static final long serialVersionUID = 1216299166699350029L;
    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    @QueryParam("app_id")
    public Integer appId;
    /**
     * 计划分类：一次性计划任务0，周期性计划任务1，消费型计划任务2
     */
    @JsonProperty("category")
    @QueryParam("category")
    public Integer category;
    /**
     * 计划名称
     */
    @JsonProperty("name")
    @QueryParam("name")
    public String name;
    /**
     * 优先级：高0，正常1，低2
     */
    @JsonProperty("priority")
    @QueryParam("priority")
    public Integer priority;
    /**
     * 计划状态：停止0，运行1
     */
    @JsonProperty("status")
    @QueryParam("status")
    public Integer status;
}
