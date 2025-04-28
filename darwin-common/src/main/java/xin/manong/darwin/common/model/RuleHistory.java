package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 规则历史
 *
 * @author frankcl
 * @date 2023-03-20 14:48:15
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "rule_history", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleHistory extends RuleCommon {

    /**
     * 规则ID
     */
    @TableField(value = "rule_id")
    @JSONField(name = "rule_id")
    @JsonProperty("rule_id")
    public Integer ruleId;

    public RuleHistory() {
        super();
    }

    public RuleHistory(Rule rule) {
        ruleId = rule.id;
        script = rule.script;
        scriptType = rule.scriptType;
        regex = rule.regex;
        modifier = rule.modifier;
        changeLog = rule.changeLog;
        createTime = rule.updateTime;
        updateTime = rule.updateTime;
        version = rule.version;
    }

    /**
     * 检测合法性
     */
    public void check() {
        super.check();
        if (ruleId == null) throw new BadRequestException("所属规则ID为空");
    }
}
