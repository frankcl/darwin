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

/**
 * 规则用户关系信息
 *
 * @author frankcl
 * @date 2023-10-20 15:40:19
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "rule_user", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleUser extends BasicModel {

    private static final Logger logger = LoggerFactory.getLogger(RuleUser.class);

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Integer id;

    /**
     * 应用ID
     */
    @TableField(value = "rule_id")
    @JSONField(name = "rule_id")
    @JsonProperty("rule_id")
    public Integer ruleId;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    @JSONField(name = "user_id")
    @JsonProperty("user_id")
    public String userId;

    /**
     * 用户真实姓名
     */
    @TableField(value = "user_real_name")
    @JSONField(name = "user_real_name")
    @JsonProperty("user_real_name")
    public String userRealName;

    /**
     * 检测有效性
     *
     * @return 如果有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(userId)) {
            logger.error("user id is empty");
            return false;
        }
        if (StringUtils.isEmpty(userRealName)) {
            logger.error("user real name is empty");
            return false;
        }
        if (ruleId == null) {
            logger.error("rule id is null");
            return false;
        }
        return true;
    }
}
