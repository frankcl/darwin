package xin.manong.darwin.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.service.iface.*;
import xin.manong.darwin.service.request.PlanSearchRequest;
import xin.manong.darwin.web.request.ExecuteRequest;
import xin.manong.weapon.base.util.RandomID;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * 计划控制器
 *
 * @author frankcl
 * @date 2023-04-24 11:42:13
 */
@RestController
@Controller
@Path("/plan")
@RequestMapping("/plan")
public class PlanController {

    private static final Logger logger = LoggerFactory.getLogger(PlanController.class);

    @Resource
    protected AppService appService;
    @Resource
    protected PlanService planService;
    @Resource
    protected RuleService ruleService;

    /**
     * 根据ID获取计划
     *
     * @param id 计划ID
     * @return 计划
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    public Plan get(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("plan id is empty");
            throw new BadRequestException("计划ID缺失");
        }
        return planService.get(id);
    }

    /**
     * 搜索计划
     *
     * @param request 搜索请求
     * @return 计划分页列表
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("search")
    @PostMapping("search")
    public Pager<Plan> search(PlanSearchRequest request) {
        if (request == null) request = new PlanSearchRequest();
        if (request.current == null || request.current < 1) request.current = 1;
        if (request.size == null || request.size <= 0) request.size = 20;
        return planService.search(request);
    }

    /**
     * 添加计划
     *
     * @param plan 计划
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    public Boolean add(Plan plan) {
        if (plan == null || plan.appId == null) {
            logger.error("plan is null or app id is null");
            throw new BadRequestException("计划或所属应用ID为空");
        }
        App app = appService.get(plan.appId.longValue());
        if (app == null) {
            logger.error("app[{}] is not found", plan.appId);
            throw new NotFoundException(String.format("所属应用[%d]不存在", plan.appId));
        }
        plan.appName = app.name;
        if (plan.seedURLs != null && !plan.seedURLs.isEmpty()) checkSeedURLs(plan.seedURLs, plan);
        plan.planId = RandomID.build();
        if (!plan.check()) {
            logger.error("plan is not valid");
            throw new BadRequestException("计划非法");
        }
        plan.createTime = null;
        plan.updateTime = null;
        plan.nextTime = null;
        return planService.add(plan);
    }

    /**
     * 更新计划
     *
     * @param plan 计划
     * @return 更新成功返回true，否则返回false
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    public Boolean update(Plan plan) {
        if (plan == null || StringUtils.isEmpty(plan.planId)) {
            logger.error("plan is null or plan id is empty");
            throw new BadRequestException("计划或计划ID为空");
        }
        Plan previous = planService.get(plan.planId);
        if (previous == null) {
            logger.error("plan[{}] is not found", plan.planId);
            throw new NotFoundException(String.format("计划[%s]不存在", plan.planId));
        }
        if (plan.seedURLs != null && !plan.seedURLs.isEmpty()) checkSeedURLs(plan.seedURLs, previous);
        /**
         * 不能修改所属应用信息
         */
        plan.appId = null;
        plan.appName = null;
        plan.createTime = null;
        plan.updateTime = null;
        plan.nextTime = null;
        return planService.update(plan);
    }

    /**
     * 执行计划
     *
     * @param request 执行计划请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("execute")
    @PostMapping("execute")
    public Boolean execute(ExecuteRequest request) {
        if (request == null || !request.check()) {
            logger.error("execute request is null or invalid");
            throw new BadRequestException("执行计划请求为空或非法");
        }
        Plan plan = planService.get(request.planId);
        if (plan == null) {
            logger.error("plan[{}] is not found", request.planId);
            throw new NotFoundException(String.format("计划[%s]不存在", request.planId));
        }
        if (plan.status != Constants.PLAN_STATUS_RUNNING) {
            logger.error("plan is not running for status[{}]", Constants.SUPPORT_PLAN_STATUSES.get(plan.status));
            throw new RuntimeException(String.format("计划[%s]非运行状态",
                    Constants.SUPPORT_PLAN_STATUSES.get(plan.status)));
        }
        if (plan.category == Constants.PLAN_CATEGORY_ONCE &&
                request.seedURLs != null && !request.seedURLs.isEmpty()) {
            checkSeedURLs(request.seedURLs, plan);
            plan.seedURLs = request.seedURLs;
        }
        if (!plan.check()) {
            logger.error("plan is not valid");
            throw new RuntimeException("计划检测失败");
        }
        if (planService.execute(plan) == null) {
            logger.error("execute plan[{}] failed", plan.planId);
            throw new RuntimeException(String.format("执行计划[%s]失败", plan.planId));
        }
        return true;
    }

    /**
     * 删除计划
     *
     * @param id 计划ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    public Boolean delete(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("plan id is empty");
            throw new BadRequestException("计划ID为空");
        }
        if (planService.get(id) == null) {
            logger.error("plan[{}] is not found", id);
            throw new NotFoundException(String.format("计划[%s]不存在", id));
        }
        return planService.delete(id);
    }

    /**
     * 检测种子URL是否匹配规则
     * 如果存在不匹配规则种子URL则抛出异常
     *
     * @param seedURLs 种子列表
     * @param plan 计划
     */
    private void checkSeedURLs(List<URLRecord> seedURLs, Plan plan) {
        List<URLRecord> records = new ArrayList<>();
        List<Rule> rules = getRuleList(plan);
        for (URLRecord seedURL : seedURLs) {
            if ((seedURL.category != null && (seedURL.category == Constants.CONTENT_CATEGORY_RESOURCE ||
                    seedURL.category == Constants.CONTENT_CATEGORY_STREAM)) || matchRule(seedURL, rules)) {
                records.add(seedURL);
                continue;
            }
            logger.warn("matched rule is not found for seed url[{}]", seedURL.url);
        }
        if (seedURLs.size() != records.size()) {
            logger.error("seed urls not match rules");
            throw new RuntimeException("种子URL不匹配规则");
        }
    }

    /**
     * 获取计划规则列表
     *
     * @param plan 计划
     * @return 规则列表
     */
    private List<Rule> getRuleList(Plan plan) {
        List<Rule> rules = new ArrayList<>();
        if (plan.ruleIds == null || plan.ruleIds.isEmpty()) return rules;
        for (Integer ruleId : plan.ruleIds) {
            Rule rule = ruleService.get(ruleId.longValue());
            if (rule == null) {
                logger.error("rule[{}] is not found", ruleId);
                throw new RuntimeException(String.format("规则[%d]不存在", ruleId));
            }
            rules.add(rule);
        }
        return rules;
    }

    /**
     * 匹配规则
     *
     * @param record URL记录
     * @param rules 规则列表
     * @return 匹配返回true，否则返回false
     */
    private boolean matchRule(URLRecord record, List<Rule> rules) {
        List<Rule> matchedRules = new ArrayList<>();
        for (Rule rule : rules) {
            if (ruleService.match(record, rule)) matchedRules.add(rule);
        }
        return matchedRules.size() == 1;
    }
}
