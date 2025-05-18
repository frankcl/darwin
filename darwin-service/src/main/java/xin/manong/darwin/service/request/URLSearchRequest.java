package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import xin.manong.darwin.common.model.HTTPRequest;
import xin.manong.darwin.common.model.RangeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * URL搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 17:46:57
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class URLSearchRequest extends SearchRequest {

    /**
     * URL状态
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
     * 内容类型：文本1，图片2，视频3，音频4，其他5
     */
    @JsonProperty("content_type")
    @QueryParam("content_type")
    public Integer contentType;
    /**
     * 抓取方式：正常抓取0，长效代理1，短效代理2，渲染3
     */
    @JsonProperty("fetch_method")
    @QueryParam("fetch_method")
    public Integer fetchMethod;
    /**
     * HTTP请求：PUT, POST, DELETE, GET
     */
    @JsonProperty("http_request")
    @QueryParam("http_request")
    public HTTPRequest httpRequest;
    /**
     * URL地址
     */
    @JsonProperty("url")
    @QueryParam("url")
    public String url;
    /**
     * 站点
     */
    @JsonProperty("host")
    @QueryParam("host")
    public String host;
    /**
     * domain
     */
    @JsonProperty("domain")
    @QueryParam("domain")
    public String domain;
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
     * 并发单元
     */
    @JsonProperty("concurrency_unit")
    @QueryParam("concurrency_unit")
    public String concurrencyUnit;
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

    /**
     * 请求体
     */
    public Map<String, Object> requestBody = new HashMap<>();

    public RangeValue<Long> fetchTimeRange;
    public RangeValue<Long> createTimeRange;
    public List<Integer> statusList;
}
