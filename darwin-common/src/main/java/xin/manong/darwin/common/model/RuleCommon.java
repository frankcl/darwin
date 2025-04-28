package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.Constants;

/**
 * 规则公共定义
 *
 * @author frankcl
 * @date 2023-03-20 14:48:15
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleCommon extends BaseModel {

    /**
     * 规则历史版本ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Integer id;

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
     * 版本
     */
    @TableField(value = "version")
    @JSONField(name = "version")
    @JsonProperty("version")
    public Integer version;

    /**
     * 修改人
     */
    @TableField(value = "modifier")
    @JSONField(name = "modifier")
    @JsonProperty("modifier")
    public String modifier;

    /**
     * 变更日志
     */
    @TableField(value = "change_log")
    @JSONField(name = "change_log")
    @JsonProperty("change_log")
    public String changeLog;

    /**
     * 检测合法性
     */
    public void check() {
        if (StringUtils.isEmpty(regex)) throw new BadRequestException("正则表达式为空");
        if (!Constants.SUPPORT_SCRIPT_TYPES.containsKey(scriptType)) throw new BadRequestException("不支持脚本类型");
        if (StringUtils.isEmpty(script)) throw new BadRequestException("脚本内容为空");
        if (StringUtils.isEmpty(changeLog)) throw new BadRequestException("变更原因为空");
    }
}
