package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xin.manong.hylian.model.User;

import java.io.Serializable;
import java.util.List;

/**
 * 批量应用用户请求
 *
 * @author frankcl
 * @date 2025-03-29 13:51:00
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchAppUserRequest implements Serializable {

    /**
     * 用户列表
     */
    @JsonProperty("users")
    public List<User> users;
    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    public Integer appId;

    /**
     * 检测有效性，无效请求抛出异常
     */
    public void check() {
        if (appId == null) throw new BadRequestException("应用ID为空");
    }
}
