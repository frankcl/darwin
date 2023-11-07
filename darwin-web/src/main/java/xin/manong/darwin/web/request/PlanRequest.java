package xin.manong.darwin.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;

import javax.ws.rs.BadRequestException;
import java.io.Serializable;
import java.util.List;

/**
 * 计划请求信息
 *
 * @author frankcl
 * @date 2023-10-20 15:08:20
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanRequest implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(PlanRequest.class);

    /**
     * 避免重复抓取
     */
    @JsonProperty("avoid_repeated_fetch")
    public Boolean avoidRepeatedFetch;

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
     * 规则ID列表
     */
    @JsonProperty("rule_ids")
    public List<Integer> ruleIds;

    /**
     * 种子列表
     */
    @JsonProperty("seed_urls")
    public List<URLRequest> seedURLs;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (appId == null) {
            logger.error("app id is null");
            throw new BadRequestException("应用ID为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("plan name is empty");
            throw new BadRequestException("计划名为空");
        }
        if (!Constants.SUPPORT_PLAN_CATEGORIES.containsKey(category)) {
            logger.error("not support plan category[{}]", category);
            throw new BadRequestException(String.format("不支持的计划类型[%d]", category));
        }
        if (category == Constants.PLAN_CATEGORY_PERIOD && (StringUtils.isEmpty(crontabExpression) ||
                !CronExpression.isValidExpression(crontabExpression))) {
            logger.error("crontab expression[{}] is invalid", crontabExpression);
            throw new BadRequestException("非法crontab表达式");
        }
        if (seedURLs != null) for (URLRequest seedURL : seedURLs) seedURL.check();
    }
}
