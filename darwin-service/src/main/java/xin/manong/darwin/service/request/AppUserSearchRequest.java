package xin.manong.darwin.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 应用用户关系搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUserSearchRequest extends SearchRequest {

    /**
     * 计划ID
     */
    @JsonProperty("app_id")
    public Integer appId;
    /**
     * 用户真实姓名
     */
    @JsonProperty("real_name")
    public String realName;
    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    public String userId;
}
