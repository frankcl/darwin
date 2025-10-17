package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * 应用秘钥请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppSecretRequest implements Serializable {

    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    public Integer appId;
    /**
     * 名称
     */
    @JsonProperty("name")
    public String name;
    /**
     * AccessKey
     */
    @JsonProperty("access_key")
    public String accessKey;
    /**
     * SecretKey
     */
    @JsonProperty("secret_key")
    public String secretKey;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (appId == null) throw new BadRequestException("应用ID为空");
        if (StringUtils.isEmpty(name)) throw new BadRequestException("名称为空");
        if (StringUtils.isEmpty(accessKey)) throw new BadRequestException("AccessKey为空");
        if (StringUtils.isEmpty(secretKey)) throw new BadRequestException("SecretKey为空");
    }
}
