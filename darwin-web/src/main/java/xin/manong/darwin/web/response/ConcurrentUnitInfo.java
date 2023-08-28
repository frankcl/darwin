package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 并发单元信息
 *
 * @author frankcl
 * @date 2023-08-28 10:41:01
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConcurrentUnitInfo implements Serializable {

    private static final long serialVersionUID = 2085942654869013790L;

    /**
     * 抓取队列容量
     */
    @JsonProperty("fetch_capacity")
    public Integer fetchCapacity;

    /**
     * 可用抓取容量
     */
    @JsonProperty("available_fetching_size")
    public Integer availableFetchingSize;

    /**
     * 当前排队数量
     */
    @JsonProperty("queuing_size")
    public Integer queuingSize;

    /**
     * 当前抓取数量
     */
    @JsonProperty("fetching_size")
    public Integer fetchingSize;

    /**
     * 当前抓取过期数量
     */
    @JsonProperty("fetching_expired_size")
    public Integer fetchingExpiredSize;
}
