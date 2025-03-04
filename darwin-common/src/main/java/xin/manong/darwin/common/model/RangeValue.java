package xin.manong.darwin.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 范围数据
 *
 * @author frankcl
 * @date 2023-03-21 19:46:31
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RangeValue<T extends Number> {

    /**
     * 是否包含下界，默认不包含
     */
    @JsonProperty("include_lower")
    public boolean includeLower = false;
    /**
     * 是否包含上界，默认不包含
     */
    @JsonProperty("include_upper")
    public boolean includeUpper = false;
    /**
     * 左区间
     */
    @JsonProperty("start")
    public T start;
    /**
     * 右区间
     */
    @JsonProperty("end")
    public T end;
}
