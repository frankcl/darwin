package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.io.Serializable;

/**
 * 排队等待信息
 *
 * @author frankcl
 * @date 2025-05-09 10:45:13
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueueWait implements Serializable {

    /**
     * 排队水位
     */
    @JsonProperty("water_level")
    public String waterLevel;
    /**
     * 排队数据量
     */
    @JsonProperty("wait_count")
    public Long waitCount;
    /**
     * 等待时间，单位：秒
     */
    @JsonProperty("wait_time")
    public Long waitTime;
    /**
     * 排队比例
     */
    @JsonProperty("queue_ratio")
    public Double queueRatio;
}
