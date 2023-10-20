package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.weapon.base.util.DomainUtil;

import java.net.URL;

/**
 * 规则
 *
 * @author frankcl
 * @date 2023-03-20 14:48:15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "rule", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Rule extends BasicModel {

    private static final Logger logger = LoggerFactory.getLogger(Rule.class);

    /**
     * 规则ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Long id;

    /**
     * 规则分组ID
     */
    @TableField(value = "rule_group")
    @JSONField(name = "rule_group")
    @JsonProperty("rule_group")
    public Long ruleGroup;

    /**
     * 规则名称
     */
    @TableField(value = "name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 规则domain
     */
    @TableField(value = "domain")
    @JSONField(name = "domain")
    @JsonProperty("domain")
    public String domain;

    /**
     * 规则正则表达式
     */
    @TableField(value = "regex")
    @JSONField(name = "regex")
    @JsonProperty("regex")
    public String regex;

    /**
     * 规则脚本
     */
    @TableField(value = "script")
    @JSONField(name = "script")
    @JsonProperty("script")
    public String script;

    /**
     * 脚本类型
     * 1：Groovy脚本
     * 2：JavaScript脚本
     */
    @TableField(value = "script_type")
    @JSONField(name = "script_type")
    @JsonProperty("script_type")
    public Integer scriptType;

    /**
     * 规则分类
     * 1：抽链规则
     * 2：结构化规则
     * 3：通用抽链规则
     */
    @TableField(value = "category")
    @JSONField(name = "category")
    @JsonProperty("category")
    public Integer category;

    /**
     * 全局抽链范围
     * 全局抽链：0
     * 域内抽链：1
     * 站点内抽链：2
     */
    @TableField(value = "link_scope")
    @JSONField(name = "link_scope")
    @JsonProperty("link_scope")
    public Integer linkScope;

    /**
     * 检测合法性
     *
     * @return 合法返回true，否则返回false
     */
    public boolean check() {
        if (ruleGroup == null) {
            logger.error("rule group is null");
            return false;
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("rule name is empty");
            return false;
        }
        if (StringUtils.isEmpty(regex)) {
            logger.error("rule regex is empty");
            return false;
        }
        if (!Constants.SUPPORT_RULE_CATEGORIES.containsKey(category)) {
            logger.error("not support rule category[{}]", category);
            return false;
        }
        if (category != Constants.RULE_CATEGORY_GLOBAL_LINK) {
            if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) {
                logger.error("not support script type[{}]", scriptType);
                return false;
            }
            if (StringUtils.isEmpty(script)) {
                logger.error("script content is empty");
                return false;
            }
        }
        if (category == Constants.RULE_CATEGORY_GLOBAL_LINK) {
            if (linkScope == null) linkScope = Constants.LINK_SCOPE_ALL;
            if (!Constants.SUPPORT_LINK_SCOPES.containsKey(linkScope)) {
                logger.error("unsupported link follow scope[{}]", linkScope);
                return false;
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
            return false;
        }
        return true;
    }
}
