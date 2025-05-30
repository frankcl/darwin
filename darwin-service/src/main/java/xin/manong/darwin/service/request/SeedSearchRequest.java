package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import xin.manong.darwin.common.model.HTTPRequest;

/**
 * 种子URL搜索请求
 *
 * @author frankcl
 * @date 2025-04-03 17:46:57
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeedSearchRequest extends SearchRequest {

    /**
     * URL地址
     */
    @JsonProperty("url")
    @QueryParam("url")
    public String url;
    /**
     * HTTP请求
     */
    @JsonProperty("http_request")
    @QueryParam("http_request")
    public HTTPRequest httpRequest;
    /**
     * 计划ID
     */
    @JsonProperty("plan_id")
    @QueryParam("plan_id")
    public String planId;
}
