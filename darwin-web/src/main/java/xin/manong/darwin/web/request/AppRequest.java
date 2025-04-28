package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * 应用请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppRequest implements Serializable {

    /**
     * 应用名
     */
    @JsonProperty("name")
    public String name;
    /**
     * 应用说明
     */
    @JsonProperty("comment")
    public String comment;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(name)) throw new BadRequestException("应用名为空");
        if (StringUtils.isEmpty(comment)) throw new BadRequestException("应用说明为空");
    }
}
