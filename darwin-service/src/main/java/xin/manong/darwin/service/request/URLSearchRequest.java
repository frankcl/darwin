package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import xin.manong.darwin.common.model.RangeValue;

import java.util.List;

/**
 * URL搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 17:46:57
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class URLSearchRequest extends SearchRequest {

    /**
     * URL状态
     *
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
    @JsonProperty("status_list")
    public List<Integer> statusList;
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
    /**
     * 创建时间范围
     */
    @JsonProperty("create_time")
    public RangeValue<Long> createTime;
}
