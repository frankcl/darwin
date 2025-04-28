package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.parser.service.request.CompileRequest;

/**
 * 脚本调试请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DebugRequest extends CompileRequest {

    /**
     * 调试URL
     */
    @JsonProperty("url")
    public String url;

    /**
     * 检测有效性，无效抛出异常
     */
    public void check() {
        super.check();
        if (StringUtils.isEmpty(url)) throw new BadRequestException("URL为空");
    }
}
