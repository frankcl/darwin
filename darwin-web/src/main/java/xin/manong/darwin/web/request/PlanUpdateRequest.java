package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import xin.manong.darwin.common.Constants;

import java.io.Serializable;

/**
 * 计划更新请求信息
 *
 * @author frankcl
 * @date 2023-10-20 15:08:20
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanUpdateRequest implements Serializable {

    /**
     * 避免重复抓取
     */
    @JsonProperty("allow_repeat")
    public Boolean allowRepeat;

    /**
     * 任务优先级
     */
    @JsonProperty("priority")
    public Integer priority;

    /**
     * 计划名称
     */
    @JsonProperty("name")
    public String name;

    /**
     * 计划ID
     */
    @JsonProperty("plan_id")
    public String planId;

    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    public Integer appId;

    /**
     * 应用名
     */
    @JsonProperty("app_name")
    public String appName;

    /**
     * 周期性任务crontab表达式
     * 针对周期性任务有效
     */
    @JsonProperty("crontab_expression")
    public String crontabExpression;

    /**
     * 计划类型
     */
    @JsonProperty("category")
    public Integer category;

    /**
     * 抓取方式
     */
    @JsonProperty("fetch_method")
    public Integer fetchMethod;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(planId)) throw new BadRequestException("计划ID为空");
        if (allowRepeat == null && StringUtils.isEmpty(name) &&
                priority == null && category == null && fetchMethod == null) {
            throw new BadRequestException("更新计划信息为空");
        }
        if (fetchMethod != null && !Constants.SUPPORT_FETCH_METHODS.containsKey(fetchMethod)) {
            throw new BadRequestException("不支持的抓取方式");
        }
        if (category != null && !Constants.SUPPORT_PLAN_CATEGORIES.containsKey(category)) {
            throw new BadRequestException("不支持的计划类型");
        }
        if (!StringUtils.isEmpty(crontabExpression) && !CronExpression.isValidExpression(crontabExpression)) {
            throw new BadRequestException("非法crontab表达式");
        }
    }
}
