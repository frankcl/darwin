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

    /**
     * 页码：从1开始
     */
    @JsonProperty("current")
    public Long current;
    /**
     * 分页数量
     */
    @JsonProperty("size")
    public Long size;
    /**
     * 总数
     */
    @JsonProperty("total")
    public Long total;
    /**
     * 数据列表
     */
    @JsonProperty("records")
    public List<T> records;
}
