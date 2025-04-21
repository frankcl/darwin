package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 并发单元信息
 *
 * @author frankcl
 * @date 2023-08-28 10:41:01
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConcurrencyUnit implements Serializable {

    @Serial
    private static final long serialVersionUID = 2085942654869013790L;

    /**
     * 抓取队列容量
     */
    @JsonProperty("fetch_capacity")
    public Integer fetchCapacity;

    /**
     * 闲置数量
     */
    @JsonProperty("spare_records")
    public Integer spareRecords;

    /**
     * 当前排队数量
     */
    @JsonProperty("queuing_records")
    public Integer queuingRecords;

    /**
     * 当前抓取数量
     */
    @JsonProperty("fetching_records")
    public Integer fetchingRecords;

    /**
     * 当前过期数量
     */
    @JsonProperty("expired_records")
    public Integer expiredRecords;
}
