package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;

/**
 * 规则历史
 *
 * @author frankcl
 * @date 2023-03-20 14:48:15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "rule_history", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleHistory extends BasicModel {

    private static final Logger logger = LoggerFactory.getLogger(RuleHistory.class);

    /**
     * 规则历史版本ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Integer id;

    /**
     * 规则ID
     */
    @TableField(value = "rule_id")
    @JSONField(name = "rule_id")
    @JsonProperty("rule_id")
    public Integer ruleId;

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

    public RuleHistory() {
    }

    public RuleHistory(Rule rule) {
        ruleId = rule.id;
        domain = rule.domain;
        script = rule.script;
        scriptType = rule.scriptType;
        regex = rule.regex;
    }

    /**
     * 检测合法性
     *
     * @return 合法返回true，否则返回false
     */
    public boolean check() {
        if (ruleId == null) {
            logger.error("rule id is null");
            return false;
        }
        if (StringUtils.isEmpty(regex)) {
            logger.error("rule regex is empty");
            return false;
        }
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) {
            logger.error("not support script type[{}]", scriptType);
            return false;
        }
        if (StringUtils.isEmpty(script)) {
            logger.error("script content is empty");
            return false;
        }
        if (StringUtils.isEmpty(domain)) {
            logger.error("domain is empty");
            return false;
        }
        return true;
    }
}
