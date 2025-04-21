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

/**
 * 消息
 *
 * @author frankcl
 * @date 2025-03-09 11:42:35
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "message", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(Message.class);

    public static final int SOURCE_TYPE_RUNNER = 1;

    /**
     * 执行器ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Integer id;

    /**
     * 消息源key
     */
    @TableField(value = "source_key")
    @JSONField(name = "source_key")
    @JsonProperty("source_key")
    public String sourceKey;

    /**
     * 消息源类型
     */
    @TableField(value = "source_type")
    @JSONField(name = "source_type")
    @JsonProperty("source_type")
    public Integer sourceType;

    /**
     * 消息
     */
    @TableField(value = "message")
    @JSONField(name = "message")
    @JsonProperty("message")
    public String message;

    /**
     * 异常
     */
    @TableField(value = "exception")
    @JSONField(name = "exception")
    @JsonProperty("exception")
    public String exception;

    /**
     * 检测有效性
     *
     * @return 有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(sourceKey)) {
            logger.error("source key is empty");
            return false;
        }
        if (sourceType == null) {
            logger.error("source type is null");
            return false;
        }
        return true;
    }
}
