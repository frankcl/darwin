package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用更新请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 8762461642422307260L;

    /**
     * 应用ID
     */
    @JsonProperty("id")
    public Integer id;
    /**
     * 应用名
     */
    @JsonProperty("name")
    public String name;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (id == null) throw new BadRequestException("应用ID为空");
        if (StringUtils.isEmpty(name)) throw new BadRequestException("更新应用信息为空");
    }
}
