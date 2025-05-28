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
import xin.manong.darwin.common.Constants;
import xin.manong.weapon.aliyun.ots.annotation.Column;
import xin.manong.weapon.aliyun.ots.annotation.PrimaryKey;

/**
 * 抓取任务
 *
 * @author frankcl
 * @date 2023-03-06 14:55:37
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "job", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Job extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(Job.class);

    /**
     * 允许重复抓取
     */
    @TableField(value = "allow_repeat")
    @Column(name = "allow_repeat")
    @JSONField(name = "allow_repeat")
    @JsonProperty("allow_repeat")
    public Boolean allowRepeat = false;

    /**
     * 应用ID
     */
    @TableField(value = "app_id")
    @Column(name = "app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public Integer appId;

    /**
     * 任务状态
     */
    @TableField(value = "status")
    @Column(name = "status")
    @JSONField(name = "status")
    @JsonProperty("status")
    public Boolean status;

    /**
     * 抓取方式
     */
    @TableField(value = "fetch_method")
    @Column(name = "fetch_method")
    @JSONField(name = "fetch_method")
    @JsonProperty("fetch_method")
    public Integer fetchMethod;

    /**
     * 任务优先级
     */
    @TableField(value = "priority")
    @Column(name = "priority")
    @JSONField(name = "priority")
    @JsonProperty("priority")
    public Integer priority;

    /**
     * 最大抓取深度
     */
    @TableField(value = "max_depth")
    @Column(name = "max_depth")
    @JSONField(name = "max_depth")
    @JsonProperty("max_depth")
    public Integer maxDepth;

    /**
     * 任务ID
     */
    @TableId(value = "job_id")
    @PrimaryKey(name = "job_id")
    @JSONField(name = "job_id")
    @JsonProperty("job_id")
    public String jobId;

    /**
     * 计划ID
     */
    @TableField(value = "plan_id")
    @Column(name = "plan_id")
    @JSONField(name = "plan_id")
    @JsonProperty("plan_id")
    public String planId;

    /**
     * 任务名称
     */
    @TableField(value = "name")
    @Column(name = "name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 执行人
     */
    @TableField(value = "executor")
    @Column(name = "executor")
    @JSONField(name = "executor")
    @JsonProperty("executor")
    public String executor;

    /**
     * 检测任务有效性
     * 1. 计划ID不能为空
     * 2. 任务ID不能为空
     * 3. 任务名不能为空
     *
     * @return 如果有效返回true，否则返回false
     */
    public boolean check() {
        if (appId == null) {
            logger.error("App id is null");
            return false;
        }
        if (StringUtils.isEmpty(planId)) {
            logger.error("Plan id is empty");
            return false;
        }
        if (StringUtils.isEmpty(jobId)) {
            logger.error("Job id is empty");
            return false;
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("Job name is empty");
            return false;
        }
        if (fetchMethod != null && !Constants.SUPPORT_FETCH_METHODS.containsKey(fetchMethod)) {
            logger.error("Not supported fetch method:{}", fetchMethod);
            return false;
        }
        if (status == null) status = true;
        if (maxDepth == null) maxDepth = 3;
        if (priority == null) priority = Constants.PRIORITY_NORMAL;
        return true;
    }
}
