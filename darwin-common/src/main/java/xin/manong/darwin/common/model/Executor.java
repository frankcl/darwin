package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;

import java.io.Serial;

/**
 * 执行器
 *
 * @author frankcl
 * @date 2025-03-09 11:42:35
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "executor", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Executor extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(Executor.class);
    @Serial
    private static final long serialVersionUID = 4271252307930784404L;

    /**
     * 执行器ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Integer id;

    /**
     * 执行器名称
     */
    @TableField(value = "name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 执行器名称
     */
    @TableField(value = "chinese_name")
    @JSONField(name = "chinese_name")
    @JsonProperty("chinese_name")
    public String chineseName;

    /**
     * 执行器状态
     * 停止：0
     * 启动：1
     * 错误：2
     */
    @TableField(value = "status")
    @JSONField(name = "status")
    @JsonProperty("status")
    public Integer status;


    /**
     * 错误原因
     */
    @TableField(value = "cause")
    @JSONField(name = "cause")
    @JsonProperty("cause")
    public String cause;

    /**
     * 说明
     */
    @TableField(value = "comment")
    @JSONField(name = "comment")
    @JsonProperty("comment")
    public String comment;

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(name)) {
            logger.error("executor name is empty");
            return false;
        }
        if (status != null && !Constants.SUPPORT_EXECUTOR_STATUSES.containsKey(status)) {
            logger.error("executor status[{}] not support", status);
            return false;
        }
        return true;
    }
}
