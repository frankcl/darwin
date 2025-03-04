package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.io.Serial;

/**
 * 应用用户关系搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUserSearchRequest extends SearchRequest {

    @Serial
    private static final long serialVersionUID = 1742507625468143611L;
    /**
     * 计划ID
     */
    @JsonProperty("app_id")
    @QueryParam("app_id")
    public Integer appId;
    /**
     * 用户昵称
     */
    @JsonProperty("nick_name")
    @QueryParam("nick_name")
    public String nickName;
    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    @QueryParam("user_id")
    public String userId;
}
