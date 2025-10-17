package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * 应用秘钥更新请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppSecretUpdateRequest extends AppSecretRequest {

    /**
     * ID
     */
    @JsonProperty("id")
    public Integer id;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        super.check();
        if (id == null) throw new BadRequestException("ID为空");
    }
}
