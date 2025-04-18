package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import xin.manong.darwin.common.model.RangeValue;

import java.io.Serial;
import java.util.List;

/**
 * URL搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 17:46:57
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class URLSearchRequest extends SearchRequest {

    @Serial
    private static final long serialVersionUID = -7037469437530155769L;
    /**
     * URL状态
     * 抓取成功:0
     * 创建:1
     * 排队拒绝:2
     * 排队中:3
     * 抓取中:4
     * URL非法:5
     * 超时:6
     * I/O错误:7
     * 抓取失败:8
     * 解析失败:9
     * 未知错误:10
     * 溢出:11
     */
    @JsonProperty("status")
    @QueryParam("status")
    public String status;
    /**
     * 优先级：高0，正常1，低2
     */
    @JsonProperty("priority")
    @QueryParam("priority")
    public Integer priority;
    /**
     * URL分类：内容文本0，内容列表1，资源2，视频流3
     */
    @JsonProperty("category")
    @QueryParam("category")
    public Integer category;
    /**
     * 抓取方式：正常抓取0，长效代理1，短效代理2，渲染3
     */
    @JsonProperty("fetch_method")
    @QueryParam("fetch_method")
    public Integer fetchMethod;
    /**
     * URL地址
     */
    @JsonProperty("url")
    @QueryParam("url")
    public String url;
    /**
     * 任务ID
     */
    @JsonProperty("job_id")
    @QueryParam("job_id")
    public String jobId;
    /**
     * 计划ID
     */
    @JsonProperty("plan_id")
    @QueryParam("plan_id")
    public String planId;
    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    @QueryParam("app_id")
    public Integer appId;
    /**
     * 抓取时间范围
     */
    @JsonProperty("fetch_time")
    @QueryParam("fetch_time")
    public String fetchTime;
    /**
     * 创建时间范围
     */
    @JsonProperty("create_time")
    @QueryParam("create_time")
    public String createTime;

    public RangeValue<Long> fetchTimeRange;
    public RangeValue<Long> createTimeRange;
    public List<Integer> statusList;
}
