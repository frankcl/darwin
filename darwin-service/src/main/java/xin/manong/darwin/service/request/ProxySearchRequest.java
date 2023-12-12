package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 代理搜索请求
 *
 * @author frankcl
 * @date 2023-12-11 11:49:21
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxySearchRequest extends SearchRequest {

    /**
     * 是否过期
     * true：过期
     * false：未过期
     */
    @JsonProperty("expired")
    public Boolean expired;

    /**
     * 代理类型
     * 长效代理：1
     * 短效代理：2
     */
    @JsonProperty("category")
    public Integer category;
}
