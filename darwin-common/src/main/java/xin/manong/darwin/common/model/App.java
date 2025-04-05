package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
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

import java.io.Serial;

/**
 * 应用信息
 *
 * @author frankcl
 * @date 2023-03-06 15:40:19
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "app", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class App extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(App.class);
    @Serial
    private static final long serialVersionUID = 6333115720821294547L;

    /**
     * 应用ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JSONField(name = "id")
    @JsonProperty("id")
    public Integer id;

    /**
     * 应用名称
     */
    @TableField(value = "name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 创建人
     */
    @TableField(value = "creator")
    @JSONField(name = "creator")
    @JsonProperty("creator")
    public String creator;

    /**
     * 修改人
     */
    @TableField(value = "modifier")
    @JSONField(name = "modifier")
    @JsonProperty("modifier")
    public String modifier;

    /**
     * 应用说明
     */
    @TableField(value = "comment")
    @JSONField(name = "comment")
    @JsonProperty("comment")
    public String comment;

    /**
     * 创建人ID
     */
    @TableField(exist = false)
    @JSONField(name = "creator_id")
    @JsonProperty("creator_id")
    public String creatorId;

    /**
     * 检测应用有效性
     * 1. 应用ID和名称不能为空
     *
     * @return 如果有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(name)) {
            logger.error("app name is empty");
            return false;
        }
        return true;
    }
}
