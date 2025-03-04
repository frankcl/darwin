package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.weapon.base.util.DomainUtil;

import java.io.Serial;
import java.io.Serializable;
import java.net.URL;

/**
 * 规则请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(RuleRequest.class);
    @Serial
    private static final long serialVersionUID = 1174994354945230603L;

    /**
     * 规则名称
     */
    @JsonProperty("name")
    public String name;

    /**
     * 规则domain
     */
    @JsonProperty("domain")
    public String domain;

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
     * 应用ID
     */
    @JsonProperty("app_id")
    public Integer appId;

    /**
     * 检测有效性，无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(name)) throw new BadRequestException("规则名为空");
        if (StringUtils.isEmpty(regex)) throw new BadRequestException("规则正则表达式为空");
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) throw new BadRequestException("不支持的脚本类型");
        if (StringUtils.isEmpty(script)) throw new BadRequestException("脚本为空");
        if (StringUtils.isEmpty(planId)) throw new BadRequestException("所属计划ID为空");
        if (appId == null) throw new BadRequestException("所属应用ID为空");
        if (StringUtils.isEmpty(domain)) {
            try {
                String host = new URL(regex).getHost();
                domain = DomainUtil.getDomain(host);
            } catch (Exception e) {
                logger.warn("invalid url regex[{}]", regex);
            }
        }
        if (StringUtils.isEmpty(domain)) throw new BadRequestException("规则域名为空");
    }
}
