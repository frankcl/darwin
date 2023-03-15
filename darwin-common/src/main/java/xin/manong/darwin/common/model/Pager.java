package xin.manong.darwin.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 分页对象
 *
 * @author frankcl
 * @date 2022-09-21 11:26:50
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pager<T> implements Serializable {

    @JsonProperty("current")
    public Long current;
    @JsonProperty("size")
    public Long size;
    @JsonProperty("total")
    public Long total;
    @JsonProperty("records")
    public List<T> records;
}
