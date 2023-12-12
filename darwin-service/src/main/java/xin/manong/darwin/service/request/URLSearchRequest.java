package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import xin.manong.darwin.common.model.RangeValue;

/**
 * URL搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 17:46:57
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class URLSearchRequest extends SearchRequest {

    /**
     * URL状态：抓取成功0，抓取失败-1，创建1，排队拒绝2，排队中3，抓取中4，非法5
     */
    @JsonProperty("status")
    public Integer status;
    /**
     * 优先级：高0，正常1，低2
     */
    @JsonProperty("priority")
    public Integer priority;
    /**
     * URL分类：内容文本0，内容列表1，资源2，视频流3
     */
    @JsonProperty("category")
    public Integer category;
    /**
     * 抓取方式：正常抓取0，长效代理1，短效代理2，渲染3
     */
    @JsonProperty("fetch_method")
    public Integer fetchMethod;
    /**
     * URL地址
     */
    @JsonProperty("url")
    public String url;
    /**
     * 任务ID
     */
    @JsonProperty("job_id")
    public String jobId;
    /**
     * 计划ID
     */
    @JsonProperty("plan_id")
    public String planId;
    /**
     * 抓取时间范围
     */
    @JsonProperty("fetch_time")
    public RangeValue<Long> fetchTime;
}
