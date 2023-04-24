package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import xin.manong.darwin.common.model.URLRecord;

import java.io.Serializable;
import java.util.List;

/**
 * 消费型计划种子URL补充请求
 *
 * @author frankcl
 * @date 2023-04-24 14:22:47
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsumedPlanSeedRequest implements Serializable {

    /**
     * 计划ID
     */
    @JsonProperty("plan_id")
    public String planId;
    /**
     * 种子URL列表
     */
    @JsonProperty("seed_urls")
    public List<URLRecord> seedURLs;
}
