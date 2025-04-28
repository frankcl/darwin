package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * 应用用户关系请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUserRequest implements Serializable {

    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    public Integer appId;
    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    public String userId;
    /**
     * 用户昵称
     */
    @JsonProperty("nick_name")
    public String nickName;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (appId == null) throw new BadRequestException("应用ID为空");
        if (StringUtils.isEmpty(userId)) throw new BadRequestException("用户名为空");
        if (StringUtils.isEmpty(nickName)) throw new BadRequestException("用户昵称为空");
    }
}
