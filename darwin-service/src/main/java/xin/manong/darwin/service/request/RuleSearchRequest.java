package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.io.Serial;

/**
 * 规则搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 17:56:29
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleSearchRequest extends SearchRequest {

    @Serial
    private static final long serialVersionUID = 8912289493345350010L;
    /**
     * 脚本类型：Groovy脚本1，JavaScript脚本2
     */
    @JsonProperty("script_type")
    @QueryParam("script_type")
    public Integer scriptType;
    /**
     * 规则名称
     */
    @JsonProperty("name")
    @QueryParam("name")
    public String name;
}
