package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.handler.JSONListIntegerTypeHandler;
import xin.manong.darwin.common.model.handler.JSONListURLRecordTypeHandler;
import xin.manong.weapon.aliyun.ots.annotation.Column;
import xin.manong.weapon.aliyun.ots.annotation.PrimaryKey;

import java.util.List;

/**
 * 抓取任务
 *
 * @author frankcl
 * @date 2023-03-06 14:55:37
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "job", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Job extends Model {

    private static final Logger logger = LoggerFactory.getLogger(Job.class);

    /**
     * 任务状态
     */
    @TableField(value = "status")
    @Column(name = "status")
    @JSONField(name = "status")
    @JsonProperty("status")
    public Integer status = Constants.JOB_STATUS_RUNNING;

    /**
     * 任务优先级
     */
    @TableField(value = "priority")
    @Column(name = "priority")
    @JSONField(name = "priority")
    @JsonProperty("priority")
    public Integer priority = Constants.PRIORITY_NORMAL;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Column(name = "create_time")
    @JSONField(name = "create_time")
    @JsonProperty("create_time")
    public Long createTime;
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Column(name = "update_time")
    @JSONField(name = "update_time")
    @JsonProperty("update_time")
    public Long updateTime;

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
     * 规则ID列表
     */
    @TableField(value = "rule_ids", typeHandler = JSONListIntegerTypeHandler.class)
    @Column(name = "rule_ids")
    @JSONField(name = "rule_ids")
    @JsonProperty("rule_ids")
    public List<Integer> ruleIds;

    /**
     * 种子列表
     */
    @TableField(value = "seed_urls", typeHandler = JSONListURLRecordTypeHandler.class)
    @Column(name = "seed_urls")
    @JSONField(name = "seed_urls")
    @JsonProperty("seed_urls")
    public List<URLRecord> seedURLs;

    /**
     * 检测任务有效性
     * 1. 计划ID不能为空
     * 2. 任务ID不能为空
     * 3. 任务名不能为空
     * 4. 种子URL列表不能为空
     *
     * @return 如果有效返回true，否则返回false
     */
    public boolean check() {
        if (StringUtils.isEmpty(planId)) {
            logger.error("plan id is empty");
            return false;
        }
        if (StringUtils.isEmpty(jobId)) {
            logger.error("job id is empty");
            return false;
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("job name is empty");
            return false;
        }
        if (seedURLs == null || seedURLs.isEmpty()) {
            logger.error("seed url list are empty");
            return false;
        }
        if (status == null) status = Constants.JOB_STATUS_RUNNING;
        if (priority == null) priority = Constants.PRIORITY_NORMAL;
        return true;
    }
}
