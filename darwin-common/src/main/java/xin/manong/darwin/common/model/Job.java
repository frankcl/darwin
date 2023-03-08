package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;

import java.io.Serializable;
import java.util.List;

/**
 * 抓取任务
 *
 * @author frankcl
 * @date 2023-03-06 14:55:37
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Job implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(Job.class);

    /**
     * 任务优先级
     */
    @JSONField(name = "priority")
    @JsonProperty("priority")
    public Integer priority = Constants.PRIORITY_NORMAL;

    /**
     * 创建时间
     */
    @JSONField(name = "create_time")
    @JsonProperty("create_time")
    public Long createTime;
    /**
     * 更新时间
     */
    @JSONField(name = "update_time")
    @JsonProperty("update_time")
    public Long updateTime;

    /**
     * 任务ID
     */
    @JSONField(name = "job_id")
    @JsonProperty("job_id")
    public String jobId;

    /**
     * 计划ID
     */
    @JSONField(name = "plan_id")
    @JsonProperty("plan_id")
    public String planId;

    /**
     * 任务名称
     */
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 规则ID列表
     */
    @JSONField(name = "rule_ids")
    @JsonProperty("rule_ids")
    public List<Integer> ruleIds;

    /**
     * 种子列表
     */
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
        if (priority == null) priority = Constants.PRIORITY_NORMAL;
        return true;
    }
}
