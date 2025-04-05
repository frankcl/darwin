package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.Constants;

import java.io.Serial;

/**
 * 规则更新请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleUpdateRequest extends RuleRequest {

    @Serial
    private static final long serialVersionUID = -7108663098373497661L;

    /**
     * 规则ID
     */
    @JsonProperty("id")
    public Integer id;

    /**
     * 检测有效性，无效抛出异常
     */
    public void check() {
        if (id == null) throw new BadRequestException("规则ID为空");
        if (scriptType == null && StringUtils.isEmpty(script) &&
                StringUtils.isEmpty(regex) && StringUtils.isEmpty(name)) {
            throw new BadRequestException("规则更新信息为空");
        }
        if (scriptType != null && !Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) {
            throw new BadRequestException("不支持的脚本类型");
        }
    }
}
