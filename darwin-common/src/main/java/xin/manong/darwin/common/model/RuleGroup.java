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

/**
 * 规则分组
 *
 * @author frankcl
 * @date 2023-03-20 14:48:15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "rule_group", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleGroup extends BasicModel {

    private static final Logger logger = LoggerFactory.getLogger(RuleGroup.class);

    /**
     * 规则ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Integer id;

    /**
     * 规则分组名称
     */
    @TableField(value = "name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 检测合法性
     *
     * @return 合法返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(name)) {
            logger.error("rule group name is empty");
            return false;
        }
        return true;
    }
}
