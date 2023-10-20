package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.weapon.base.util.DomainUtil;

import javax.ws.rs.BadRequestException;
import java.io.Serializable;
import java.net.URL;

/**
 * 规则请求
 *
 * @author frankcl
 * @date 2023-10-20 13:56:23
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(RuleRequest.class);

    /**
     * 规则分组ID
     */
    @JsonProperty("rule_group")
    public Integer ruleGroup;

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
     * 规则分类
     * 1：抽链规则
     * 2：结构化规则
     * 3：通用抽链规则
     */
    @JsonProperty("category")
    public Integer category;

    /**
     * 全局抽链范围
     * 全局抽链：0
     * 域内抽链：1
     * 站点内抽链：2
     */
    @JsonProperty("link_scope")
    public Integer linkScope;

    /**
     * 检测有效性，无效抛出异常
     */
    public void check() {
        if (ruleGroup == null) {
            logger.error("rule group is null");
            throw new BadRequestException("规则分组ID为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("rule name is empty");
            throw new BadRequestException("规则名为空");
        }
        if (StringUtils.isEmpty(regex)) {
            logger.error("rule regex is empty");
            throw new BadRequestException("规则正则表达式为空");
        }
        if (!Constants.SUPPORT_RULE_CATEGORIES.containsKey(category)) {
            logger.error("not support rule category[{}]", category);
            throw new BadRequestException(String.format("不支持的规则类型[%d]", category));
        }
        if (category != Constants.RULE_CATEGORY_GLOBAL_LINK) {
            if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) {
                logger.error("not support script type[{}]", scriptType);
                throw new BadRequestException(String.format("不支持的脚本类型[%d]", scriptType));
            }
            if (StringUtils.isEmpty(script)) {
                logger.error("script content is empty");
                throw new BadRequestException("脚本为空");
            }
        }
        if (category == Constants.RULE_CATEGORY_GLOBAL_LINK) {
            if (linkScope == null) linkScope = Constants.LINK_SCOPE_ALL;
            if (!Constants.SUPPORT_LINK_SCOPES.containsKey(linkScope)) {
                logger.error("unsupported link follow scope[{}]", linkScope);
                throw new BadRequestException(String.format("不支持的全局抽链类型[%d]", linkScope));
            }
        }
        if (StringUtils.isEmpty(domain)) {
            try {
                String host = new URL(regex).getHost();
                domain = DomainUtil.getDomain(host);
            } catch (Exception e) {
            }
        }
        if (StringUtils.isEmpty(domain)) {
            logger.error("domain is empty, can not extract domain from regex[{}]", regex);
            throw new BadRequestException("规则域名为空");
        }
    }
}
