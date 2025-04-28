package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.io.Serializable;

/**
 * 多级队列快照
 *
 * @author frankcl
 * @date 2023-08-28 11:09:57
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConcurrencyQueueSnapshot implements Serializable {

    /**
     * 内存水位
     * 未知：-1
     * 正常：0
     * 警告：1
     * 危险：2
     */
    @JsonProperty("memory_water_level")
    public Integer memoryWaterLevel;
    /**
     * 并发单元数量
     */
    @JsonProperty("concurrent_units")
    public Integer concurrentUnits;
    /**
     * 排队URL数量
     */
    @JsonProperty("queuing_records")
    public Integer queuingRecords;
    /**
     * 抓取URL数量
     */
    @JsonProperty("fetching_records")
    public Integer fetchingRecords;
    /**
     * 抓取过期URL数量
     */
    @JsonProperty("expired_records")
    public Integer expiredRecords;
}
