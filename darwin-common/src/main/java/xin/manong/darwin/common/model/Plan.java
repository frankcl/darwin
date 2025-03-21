package xin.manong.darwin.common.model;

import com.alibaba.fastjson.annotation.JSONField;
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
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.handler.JSONListIntegerTypeHandler;
import xin.manong.darwin.common.model.handler.JSONListURLRecordTypeHandler;
import xin.manong.weapon.base.util.CommonUtil;
import xin.manong.weapon.base.util.RandomID;

import java.io.Serial;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务计划：用于生成爬虫任务
 *
 * @author frankcl
 * @date 2023-03-06 15:08:20
 */
@Getter
@Setter
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
@TableName(value = "plan", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Plan extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(Plan.class);

    private static final String DATE_TIME_FORMAT = "yyyy_MM_dd_HH_mm_ss";
    @Serial
    private static final long serialVersionUID = -2706430539910683801L;

    /**
     * 避免重复抓取
     */
    @TableField(value = "avoid_repeated_fetch")
    @JSONField(name = "avoid_repeated_fetch")
    @JsonProperty("avoid_repeated_fetch")
    public Boolean avoidRepeatedFetch;

    /**
     * 计划状态
     */
    @TableField(value = "status")
    @JSONField(name = "status")
    @JsonProperty("status")
    public Integer status;

    /**
     * 任务优先级
     */
    @TableField(value = "priority")
    @JSONField(name = "priority")
    @JsonProperty("priority")
    public Integer priority;

    /**
     * 应用ID
     */
    @TableField(value = "app_id")
    @JSONField(name = "app_id")
    @JsonProperty("app_id")
    public Integer appId;

    /**
     * 下次调度时间，针对周期性任务生效
     */
    @TableField(value = "next_time")
    @JSONField(name = "next_time")
    @JsonProperty("next_time")
    public Long nextTime;

    /**
     * 应用名
     */
    @TableField(value = "app_name")
    @JSONField(name = "app_name")
    @JsonProperty("app_name")
    public String appName;

    /**
     * 计划ID
     */
    @TableId(value = "plan_id")
    @JSONField(name = "plan_id")
    @JsonProperty("plan_id")
    public String planId;

    /**
     * 计划名称
     */
    @TableField(value = "name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    /**
     * 周期性任务crontab表达式
     * 针对周期性任务有效
     */
    @TableField(value = "crontab_expression")
    @JSONField(name = "crontab_expression")
    @JsonProperty("crontab_expression")
    public String crontabExpression;

    /**
     * 计划类型
     */
    @TableField(value = "category")
    @JSONField(name = "category")
    @JsonProperty("category")
    public Integer category;

    /**
     * 抓取方式
     */
    @TableField(value = "fetch_method")
    @JSONField(name = "fetch_method")
    @JsonProperty("fetch_method")
    public Integer fetchMethod;

    /**
     * 规则ID列表
     */
    @TableField(value = "rule_ids", typeHandler = JSONListIntegerTypeHandler.class)
    @JSONField(name = "rule_ids")
    @JsonProperty("rule_ids")
    public List<Integer> ruleIds;

    /**
     * 种子列表
     */
    @TableField(value = "seed_urls", typeHandler = JSONListURLRecordTypeHandler.class)
    @JSONField(name = "seed_urls")
    @JsonProperty("seed_urls")
    public List<URLRecord> seedURLs;

    /**
     * 根据当前计划生成任务
     *
     * @return 任务实例
     */
    public Job buildJob() {
        Job job = new Job();
        job.createTime = System.currentTimeMillis();
        job.planId = planId;
        job.appId = appId;
        job.avoidRepeatedFetch = avoidRepeatedFetch == null || avoidRepeatedFetch;
        job.priority = priority == null ? Constants.PRIORITY_NORMAL : priority;
        job.status = Constants.JOB_STATUS_RUNNING;
        job.fetchMethod = fetchMethod;
        job.jobId = RandomID.build();
        job.name = String.format("%s_%s", name, CommonUtil.timeToString(System.currentTimeMillis(), DATE_TIME_FORMAT));
        job.ruleIds = ruleIds;
        job.seedURLs = seedURLs == null ? null : seedURLs.stream().map(record -> {
            URLRecord seedRecord = new URLRecord(record);
            seedRecord.rebuildKey();
            seedRecord.appId = job.appId;
            seedRecord.jobId = job.jobId;
            seedRecord.planId = job.planId;
            seedRecord.status = Constants.URL_STATUS_CREATED;
            if (seedRecord.fetchMethod == null) seedRecord.fetchMethod = job.fetchMethod;
            if (seedRecord.category == null) seedRecord.category = Constants.CONTENT_CATEGORY_CONTENT;
            if (seedRecord.concurrentLevel == null) seedRecord.concurrentLevel = Constants.CONCURRENT_LEVEL_DOMAIN;
            if (seedRecord.priority == null) {
                seedRecord.priority = job.priority == null ? Constants.PRIORITY_NORMAL : job.priority;
            }
            return seedRecord;
        }).collect(Collectors.toList());
        return job;
    }

    /**
     * 检测计划有效性
     * 1. 计划类型不能为空；如果是周期性计划，crontabExpression必须合法
     * 2. 应用ID和应用名不能为空
     * 3. 计划ID和计划名不能为空
     * 4. 种子列表不能为空
     *
     * @return 如果有效返回true，否则返回false
     */
    public boolean check() {
        if (appId == null) {
            logger.error("app id is null");
            return false;
        }
        if (StringUtils.isEmpty(planId)) {
            logger.error("plan id is empty");
            return false;
        }
        if (StringUtils.isEmpty(appName)) {
            logger.error("app name is empty");
            return false;
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("plan name is empty");
            return false;
        }
        if (seedURLs == null || seedURLs.isEmpty()) {
            logger.error("seed url list are empty");
            return false;
        }
        if (!Constants.SUPPORT_PLAN_CATEGORIES.containsKey(category)) {
            logger.error("not supported plan category[{}]", category);
            return false;
        }
        if (!Constants.SUPPORT_PLAN_STATUSES.containsKey(status)) {
            logger.error("not supported plan status[{}]", status);
            return false;
        }
        if (fetchMethod != null && !Constants.SUPPORT_FETCH_METHODS.containsKey(fetchMethod)) {
            logger.error("not supported fetch method[{}]", fetchMethod);
            return false;
        }
        if (category == Constants.PLAN_CATEGORY_PERIOD && (StringUtils.isEmpty(crontabExpression) ||
                !CronExpression.isValidExpression(crontabExpression))) {
            logger.error("crontab expression[{}] is invalid", crontabExpression);
            return false;
        }
        if (avoidRepeatedFetch == null) avoidRepeatedFetch = true;
        if (priority == null) priority = Constants.PRIORITY_NORMAL;
        if (status == null) status = Constants.PLAN_STATUS_RUNNING;
        return true;
    }
}
