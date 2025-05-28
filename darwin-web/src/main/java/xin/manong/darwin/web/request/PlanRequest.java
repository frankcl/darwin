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
 * 计划请求信息
 *
 * @author frankcl
 * @date 2023-10-20 15:08:20
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanRequest implements Serializable {

    /**
     * 避免重复抓取
     */
    @JsonProperty("allow_repeat")
    public Boolean allowRepeat;

    /**
     * 任务优先级
     * 高优先级0，正常优先级1，低优先级2
     */
    @JsonProperty("priority")
    public Integer priority;

    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    public Integer appId;

    /**
     * 最大抓取深度
     */
    @JsonProperty("max_depth")
    public Integer maxDepth;

    /**
     * 应用名
     */
    @JsonProperty("app_name")
    public String appName;

    /**
     * 计划名称
     */
    @JsonProperty("name")
    public String name;

    /**
     * 周期性任务crontab表达式
     * 针对周期性任务有效
     */
    @JsonProperty("crontab_expression")
    public String crontabExpression;

    /**
     * 计划类型
     * 一次性计划：0；周期性计划：1
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
        if (appId == null) throw new BadRequestException("应用ID为空");
        if (StringUtils.isEmpty(name)) throw new BadRequestException("计划名为空");
        if (!Constants.SUPPORT_PLAN_CATEGORIES.containsKey(category)) throw new BadRequestException("不支持的计划类型");
        if (fetchMethod != null && !Constants.SUPPORT_FETCH_METHODS.containsKey(fetchMethod)) {
            throw new BadRequestException("不支持的抓取方式");
        }
        if (category == Constants.PLAN_CATEGORY_PERIOD && (StringUtils.isEmpty(crontabExpression) ||
                !CronExpression.isValidExpression(crontabExpression))) {
            throw new BadRequestException("非法crontab表达式");
        }
    }
}
