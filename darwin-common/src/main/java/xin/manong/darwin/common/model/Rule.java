package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
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
public class Rule extends Model {

    private static final Logger logger = LoggerFactory.getLogger(Rule.class);

    /**
     * 规则ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Long id;

    /**
     * 应用ID
     */
    @TableField(value = "app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public Long appId;

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
     * 抽链范围
     * 0：全局抽链
     * 1：domain抽链
     * 2：host抽链
     */
    @TableField(value = "link_follow_scope")
    @JSONField(name = "link_follow_scope")
    @JsonProperty("link_follow_scope")
    public Integer linkFollowScope = Constants.LINK_FOLLOW_SCOPE_ALL;

    public boolean check() {
        if (appId == null || appId < 0) {
            logger.error("app id[{}] is empty or invalid", appId);
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
        if (category == null || !Constants.SUPPORT_RULE_CATEGORIES.contains(category)) {
            logger.error("not support rule category[{}]", category);
            return false;
        }
        if (category != Constants.RULE_CATEGORY_GLOBAL_LINK_FOLLOW) {
            if (scriptType == null || !Constants.SUPPORT_SCRIPT_TYPES.contains(scriptType)) {
                logger.error("not support script type[{}]", scriptType);
                return false;
            }
            if (StringUtils.isEmpty(script)) {
                logger.error("script content is empty");
                return false;
            }
        }
        if (linkFollowScope == null) linkFollowScope = Constants.LINK_FOLLOW_SCOPE_ALL;
        if (!Constants.SUPPORT_LINK_FOLLOW_SCOPES.contains(linkFollowScope)) {
            logger.error("not support link follow scope[{}]", linkFollowScope);
            return false;
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
