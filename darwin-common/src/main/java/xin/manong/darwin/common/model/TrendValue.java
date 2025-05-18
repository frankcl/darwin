package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;

/**
 * 趋势统计项
 *
 * @author frankcl
 * @date 2025-04-23 19:59:10
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrendValue<T> {

    /**
     * 统计名
     */
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;
    /**
     * 统计值
     */
    @JSONField(name = "value")
    @JsonProperty("value")
    public T value;

    public TrendValue() {}
    public TrendValue(String name, T value) {
        this.name = name;
        this.value = value;
    }
}
