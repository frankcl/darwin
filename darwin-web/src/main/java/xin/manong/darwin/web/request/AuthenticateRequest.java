package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * 认证请求
 *
 * @author frankcl
 * @date 2025-10-16 14:49:33
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticateRequest implements Serializable {

    @JsonProperty("access_key")
    public String accessKey;
    @JsonProperty("secret_key")
    public String secretKey;

    public void check() {
        if (StringUtils.isEmpty(accessKey)) throw new BadRequestException("Access key为空");
        if (StringUtils.isEmpty(secretKey)) throw new BadRequestException("Secret key为空");
    }
}
