package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import xin.manong.darwin.common.model.RangeValue;

import java.io.Serial;

/**
 * 任务搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobSearchRequest extends SearchRequest {

    @Serial
    private static final long serialVersionUID = 1124669204233050210L;
    /**
     * 计划ID
     */
    @JsonProperty("plan_id")
    @QueryParam("plan_id")
    public String planId;
    /**
     * 任务名称
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
     * 任务状态：结束0，运行1
     */
    @JsonProperty("status")
    @QueryParam("status")
    public Integer status;
    /**
     * 创建时间范围
     */
    @JsonProperty("create_time")
    @QueryParam("create_time")
    public String createTime;

    public RangeValue<Long> createTimeRange;
}
