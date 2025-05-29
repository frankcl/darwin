package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * 计划搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanSearchRequest extends SearchRequest {

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
     * 计划状态
     */
    @JsonProperty("status")
    @QueryParam("status")
    public Boolean status;
}
