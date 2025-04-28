package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * 代理搜索请求
 *
 * @author frankcl
 * @date 2023-12-11 11:49:21
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxySearchRequest extends SearchRequest {

    /**
     * 是否过期
     * true：过期
     * false：未过期
     */
    @JsonProperty("expired")
    @QueryParam("expired")
    public Boolean expired;

    /**
     * 代理类型
     * 长效代理：1
     * 短效代理：2
     */
    @JsonProperty("category")
    @QueryParam("category")
    public Integer category;
}
