package xin.manong.darwin.web.request;

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
 * 规则请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1174994354945230603L;

    /**
     * 规则名称
     */
    @JsonProperty("name")
    public String name;

    /**
     * 规则正则表达式
     */
    @JsonProperty("regex")
    public String regex;

    /**
     * 规则脚本
     */
    @JsonProperty("script")
    public String script;

    /**
     * 脚本类型
     * 1：Groovy脚本
     * 2：JavaScript脚本
     */
    @JsonProperty("script_type")
    public Integer scriptType;

    /**
     * 所属计划ID
     */
    @JsonProperty("plan_id")
    public String planId;

    /**
     * 变更日志
     */
    @JsonProperty("change_log")
    public String changeLog;

    /**
     * 检测有效性，无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(name)) throw new BadRequestException("规则名为空");
        if (StringUtils.isEmpty(regex)) throw new BadRequestException("规则正则表达式为空");
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) throw new BadRequestException("不支持的脚本类型");
        if (StringUtils.isEmpty(script)) throw new BadRequestException("脚本为空");
        if (StringUtils.isEmpty(planId)) throw new BadRequestException("所属计划ID为空");
        if (StringUtils.isEmpty(changeLog)) throw new BadRequestException("变更原因为空");
    }
}
