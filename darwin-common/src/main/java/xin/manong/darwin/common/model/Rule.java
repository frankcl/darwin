package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;

/**
 * 规则
 *
 * @author frankcl
 * @date 2023-03-20 14:48:15
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "rule", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Rule extends RuleCommon {

    @Serial
    private static final long serialVersionUID = -5208851620714996259L;

    /**
     * 规则名称
     */
    @TableField(value = "name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 所属计划ID
     */
    @TableField(value = "plan_id")
    @JSONField(name = "plan_id")
    @JsonProperty("plan_id")
    public String planId;

    /**
     * 应用ID
     */
    @TableField(value = "app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public Integer appId;

    /**
     * 检测合法性
     */
    public void check() {
        super.check();
        if (StringUtils.isEmpty(name)) throw new BadRequestException("规则名为空");
        if (StringUtils.isEmpty(planId)) throw new BadRequestException("所属计划ID为空");
        if (appId == null) throw new BadRequestException("所属应用ID为空");
    }
}
