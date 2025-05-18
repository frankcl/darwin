package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * URL数据分组计数
 *
 * @author frankcl
 * @date 2025-04-17 20:15:19
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "url", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class URLGroupCount extends BaseModel {

    /**
     * URL状态
     */
    @TableField(value = "status")
    @JSONField(name = "status")
    @JsonProperty("status")
    public Integer status;

    /**
     * 类型
     */
    @TableField(value = "content_type")
    @JSONField(name = "content_type")
    @JsonProperty("content_type")
    public Integer contentType;

    /**
     * 优先级
     */
    @TableField(value = "priority")
    @JSONField(name = "priority")
    @JsonProperty("priority")
    public Integer priority;

    /**
     * 并发单元
     */
    @TableField(value = "concurrency_unit")
    @JSONField(name = "concurrency_unit")
    @JsonProperty("concurrency_unit")
    public String concurrencyUnit;

    /**
     * host
     */
    @TableField(value = "host")
    @JSONField(name = "host")
    @JsonProperty("host")
    public String host;

    /**
     * 任务ID
     */
    @TableField(value = "job_id")
    @JSONField(name = "job_id")
    @JsonProperty("job_id")
    public String jobId;

    /**
     * 计数统计
     */
    @TableField(value = "count(*)",insertStrategy = FieldStrategy.NEVER,updateStrategy = FieldStrategy.NEVER)
    @JSONField(name = "count")
    @JsonProperty("count")
    public Integer count;
}
