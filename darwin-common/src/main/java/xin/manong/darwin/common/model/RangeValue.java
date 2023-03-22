package xin.manong.darwin.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 范围数据
 *
 * @author frankcl
 * @date 2023-03-21 19:46:31
 */
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RangeValue<T extends Number> {

    @JsonProperty("include_lower")
    public boolean includeLower = false;
    @JsonProperty("include_upper")
    public boolean includeUpper = false;
    @JsonProperty("start")
    public T start;
    @JsonProperty("end")
    public T end;
}
