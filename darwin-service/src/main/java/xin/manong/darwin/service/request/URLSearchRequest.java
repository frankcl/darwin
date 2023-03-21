package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import xin.manong.darwin.common.model.RangeValue;

import java.io.Serializable;

/**
 * URL搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 17:46:57
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class URLSearchRequest implements Serializable {

    @JsonProperty("status")
    public Integer status;
    @JsonProperty("priority")
    public Integer priority;
    @JsonProperty("category")
    public Integer category;
    @JsonProperty("url")
    public String url;
    @JsonProperty("job_id")
    public String jobId;
    @JsonProperty("fetch_time")
    public RangeValue<Long> fetchTime;
}
