package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;

import javax.ws.rs.BadRequestException;

/**
 * 规则更新请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleUpdateRequest extends RuleRequest {

    private static final Logger logger = LoggerFactory.getLogger(RuleUpdateRequest.class);

    /**
     * 规则ID
     */
    @JsonProperty("id")
    public Integer id;

    /**
     * 检测有效性，无效抛出异常
     */
    public void check() {
        if (id == null) {
            logger.error("rule id is null");
            throw new BadRequestException("规则ID为空");
        }
        if (ruleGroup == null && category == null && scriptType == null &&
                StringUtils.isEmpty(script) && StringUtils.isEmpty(regex) &&
                StringUtils.isEmpty(domain) && StringUtils.isEmpty(name)) {
            logger.error("rule update info is empty");
            throw new BadRequestException("规则更新信息为空");
        }
        if (category != null && !Constants.SUPPORT_RULE_CATEGORIES.containsKey(category)) {
            logger.error("not support rule category[{}]", category);
            throw new BadRequestException(String.format("不支持的规则类型[%d]", category));
        }
        if (scriptType != null && !Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) {
            logger.error("not support script type[{}]", scriptType);
            throw new BadRequestException(String.format("不支持的脚本类型[%d]", scriptType));
        }
        if (linkScope != null && !Constants.SUPPORT_LINK_SCOPES.containsKey(linkScope)) {
            logger.error("unsupported link follow scope[{}]", linkScope);
            throw new BadRequestException(String.format("不支持的全局抽链类型[%d]", linkScope));
        }
    }
}
