package xin.manong.darwin.common.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;

/**
 * 设置Cookie请求
 *
 * @author frankcl
 * @date 2026-01-29 10:27:41
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SetCookieRequest extends AuthenticateRequest {

    /**
     * Cookie host/domain
     */
    @JsonProperty("key")
    public String key;

    /**
     * Cookie值
     */
    @JsonProperty("cookie")
    public String cookie;

    /**
     * 检测有效性
     */
    public void check() {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("Cookie key为空");
        if (StringUtils.isEmpty(cookie)) throw new BadRequestException("Cookie为空");
    }
}
