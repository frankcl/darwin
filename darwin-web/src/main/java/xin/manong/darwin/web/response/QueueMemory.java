package xin.manong.darwin.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.io.Serializable;

/**
 * 队列内存
 *
 * @author frankcl
 * @date 2025-05-09 10:45:13
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueueMemory implements Serializable {

    /**
     * 内存水位
     */
    @JsonProperty("water_level")
    public String waterLevel;
    /**
     * 使用内存
     */
    @JsonProperty("use_memory")
    public Long useMemory;
    /**
     * 最大内存
     */
    @JsonProperty("max_memory")
    public Long maxMemory;
    /**
     * 内存使用率
     */
    @JsonProperty("use_ratio")
    public Double useRatio;
}
