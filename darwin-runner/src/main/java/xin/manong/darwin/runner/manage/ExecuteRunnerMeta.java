package xin.manong.darwin.runner.manage;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 执行器元信息
 *
 * @author frankcl
 * @date 2025-03-09 11:42:35
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecuteRunnerMeta implements Serializable {

    /**
     * 执行器key
     */
    @JSONField(name = "key")
    @JsonProperty("key")
    public String key;

    /**
     * 执行器名称
     */
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 说明描述
     */
    @JSONField(name = "description")
    @JsonProperty("description")
    public String description;

    /**
     * 状态
     * 运行：true
     * 停止：false
     */
    @JSONField(name = "status")
    @JsonProperty("status")
    public Boolean status;

    /**
     * 错误事件数量
     */
    @JSONField(name = "message_num")
    @JsonProperty("message_num")
    public Long messageNum;
}
