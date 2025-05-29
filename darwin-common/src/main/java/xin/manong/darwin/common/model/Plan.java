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

/**
 * 任务计划：生成爬虫任务
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

    /**
     * 计划状态
     */
    @TableField(value = "status")
    @JSONField(name = "status")
    @JsonProperty("status")
    public Boolean status;

    /**
     * 最大抓取深度
     */
    @TableField(value = "max_depth")
    @JSONField(name = "max_depth")
    @JsonProperty("max_depth")
    public Integer maxDepth;

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
     * 检测计划有效性
     * 1. 计划类型不能为空；如果是周期性计划，crontabExpression必须合法
     * 2. 应用ID和应用名不能为空
     * 3. 计划ID和计划名不能为空
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
        if (StringUtils.isEmpty(appName)) {
            logger.error("App name is empty");
            return false;
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("Plan name is empty");
            return false;
        }
        if (!Constants.SUPPORT_PLAN_CATEGORIES.containsKey(category)) {
            logger.error("Not supported plan category:{}", category);
            return false;
        }
        if (category == Constants.PLAN_CATEGORY_PERIOD && (StringUtils.isEmpty(crontabExpression) ||
                !CronExpression.isValidExpression(crontabExpression))) {
            logger.error("Crontab expression:{} is invalid", crontabExpression);
            return false;
        }
        if (status == null) status = false;
        if (maxDepth == null) maxDepth = 3;
        return true;
    }
}
