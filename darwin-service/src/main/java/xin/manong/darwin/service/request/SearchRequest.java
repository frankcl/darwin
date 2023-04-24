package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 搜索请求
 *
 * @author frankcl
 * @date 2023-04-24 11:07:03
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequest implements Serializable {

    /**
     * 页码，从1开始
     */
    @JsonProperty("current")
    public Integer current;
    /**
     * 分页大小，默认20
     */
    @JsonProperty("size")
    public Integer size;
}
