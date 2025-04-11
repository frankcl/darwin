package xin.manong.darwin.parser.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.Constants;

import java.io.Serial;
import java.io.Serializable;

/**
 * 脚本编译请求
 *
 * @author frankcl
 * @date 2025-04-07 13:56:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompileRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 8283438202976747711L;
    /**
     * 调试脚本
     */
    @JsonProperty("script")
    public String script;

    /**
     * 调试脚本类型
     * 1：Groovy脚本
     * 2：JavaScript脚本
     */
    @JsonProperty("script_type")
    public Integer scriptType;

    /**
     * 检测有效性，无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(script)) throw new BadRequestException("脚本代码为空");
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) {
            throw new BadRequestException("不支持的脚本类型");
        }
    }
}
