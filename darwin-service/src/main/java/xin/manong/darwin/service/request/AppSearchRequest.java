package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * 应用搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppSearchRequest extends SearchRequest {

    /**
     * 应用名称
     */
    @JsonProperty("name")
    @QueryParam("name")
    public String name;
}
