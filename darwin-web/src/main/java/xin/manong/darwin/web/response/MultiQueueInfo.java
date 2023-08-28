package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 多级队列信息
 *
 * @author frankcl
 * @date 2023-08-28 11:09:57
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MultiQueueInfo implements Serializable {

    private static final long serialVersionUID = 5192194278750852497L;

    /**
     * 内存级别
     * 未知：-1
     * 正常：0
     * 警告：1
     * 拒绝服务：2
     */
    @JsonProperty("memory_level")
    public Integer memoryLevel;
    /**
     * 并发单元数量
     */
    @JsonProperty("concurrent_unit_num")
    public Integer concurrentUnitNum;
    /**
     * 排队URL数量
     */
    @JsonProperty("queuing_record_num")
    public Integer queuingRecordNum;
    /**
     * 抓取URL数量
     */
    @JsonProperty("fetching_record_num")
    public Integer fetchingRecordNum;
    /**
     * 抓取过期URL数量
     */
    @JsonProperty("fetching_expired_record_num")
    public Integer fetchingExpiredRecordNum;
}
