package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.service.iface.*;
import xin.manong.darwin.service.request.PlanSearchRequest;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.PlanRequest;
import xin.manong.darwin.web.request.PlanUpdateRequest;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

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
    @Resource
    protected PermissionSupport permissionSupport;

    /**
     * 启动计划
     *
     * @param planId 计划ID
     * @return 启动成功返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("start")
    @GetMapping("start")
    @EnableWebLogAspect
    public Boolean start(@QueryParam("plan_id") String planId) {
        checkPeriodPlan(planId, Constants.PLAN_STATUS_STOPPED);
        Plan updatePlan = new Plan();
        updatePlan.planId = planId;
        updatePlan.status = Constants.PLAN_STATUS_RUNNING;
        return planService.update(updatePlan);
    }

    /**
     * 停止计划
     *
     * @param planId 计划ID
     * @return 停止成功返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("stop")
    @GetMapping("stop")
    @EnableWebLogAspect
    public Boolean stop(@QueryParam("plan_id") String planId) {
        checkPeriodPlan(planId, Constants.PLAN_STATUS_RUNNING);
        Plan updatePlan = new Plan();
        updatePlan.planId = planId;
        updatePlan.status = Constants.PLAN_STATUS_STOPPED;
        return planService.update(updatePlan);
    }

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
    @EnableWebLogAspect
    public Plan get(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("计划ID缺失");
        return planService.get(id);
    }

    /**
     * 搜索计划
     *
     * @param request 搜索请求
     * @return 计划分页列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    @EnableWebLogAspect
    public Pager<Plan> search(@BeanParam PlanSearchRequest request) {
        return planService.search(request);
    }

    /**
     * 添加计划
     *
     * @param request 计划请求
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    @EnableWebLogAspect
    public Boolean add(@RequestBody PlanRequest request) {
        if (request == null) throw new BadRequestException("计划请求信息为空");
        request.check();
        permissionSupport.checkAppPermission(request.appId);
        Plan plan = Converter.convert(request);
        fillAppName(plan);
        checkSeeds(plan.seedURLs, plan.ruleIds);
        plan.planId = RandomID.build();
        if (!plan.check()) throw new BadRequestException("计划非法");
        return planService.add(plan);
    }

    /**
     * 更新计划
     *
     * @param request 计划更新信息
     * @return 更新成功返回true，否则返回false
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    public Boolean update(@RequestBody PlanUpdateRequest request) {
        if (request == null) throw new BadRequestException("更新计划信息为空");
        request.check();
        Plan previous = planService.get(request.planId);
        if (previous == null) throw new NotFoundException("计划不存在");
        permissionSupport.checkAppPermission(previous.appId);
        Plan plan = Converter.convert(request);
        if (plan.ruleIds == null || plan.ruleIds.isEmpty()) plan.ruleIds = previous.ruleIds;
        checkSeeds(plan.seedURLs, plan.ruleIds);
        return planService.update(plan);
    }

    /**
     * 执行计划
     *
     * @param id 计划ID
     * @return 成功返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("execute")
    @GetMapping("execute")
    @EnableWebLogAspect
    public Boolean execute(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("计划ID为空");
        Plan plan = planService.get(id);
        if (plan == null) throw new NotFoundException("计划不存在");
        permissionSupport.checkAppPermission(plan.appId);
        if (plan.status != Constants.PLAN_STATUS_RUNNING) throw new IllegalStateException("计划处于非运行状态");
        if (!planService.execute(plan)) throw new InternalServerErrorException("执行计划失败");
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
    @EnableWebLogAspect
    public Boolean delete(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("计划ID为空");
        Plan plan = planService.get(id);
        if (plan == null) throw new NotFoundException("计划不存在");
        permissionSupport.checkAppPermission(plan.appId);
        return planService.delete(id);
    }

    /**
     * 检测种子URL是否匹配规则
     * 如果存在不匹配规则的种子URL则抛出异常
     *
     * @param seedURLs 种子列表
     * @param ruleIds 规则ID列表
     */
    private void checkSeeds(List<URLRecord> seedURLs, List<Integer> ruleIds) {
        if (seedURLs == null || seedURLs.isEmpty()) return;
        int passCount = 0;
        List<Rule> rules = ruleIds == null ? new ArrayList<>() : ruleService.batchGet(ruleIds);
        for (URLRecord seedURL : seedURLs) {
            if ((seedURL.category != null && (seedURL.category == Constants.CONTENT_CATEGORY_RESOURCE ||
                    seedURL.category == Constants.CONTENT_CATEGORY_STREAM)) ||
                    ruleService.matchRuleCount(seedURL, rules) == 1) {
                passCount++;
                continue;
            }
            logger.warn("matched rule is not found for seed url[{}]", seedURL.url);
        }
        if (seedURLs.size() != passCount) throw new BadRequestException("种子URL不匹配规则");
    }

    /**
     * 检测周期性计划
     *
     * @param planId 计划ID
     * @param status 当前状态
     */
    private void checkPeriodPlan(String planId, int status) {
        if (StringUtils.isEmpty(planId)) throw new BadRequestException("计划ID为空");
        Plan plan = planService.get(planId);
        if (plan == null) throw new NotFoundException("计划未找到");
        if (plan.category != Constants.PLAN_CATEGORY_PERIOD) throw new IllegalStateException("非周期性计划");
        if (plan.status != status) {
            throw new IllegalStateException(String.format("不是期望的计划状态：%s",
                    Constants.SUPPORT_PLAN_STATUSES.get(status)));
        }
        permissionSupport.checkAppPermission(plan.appId);
    }

    /**
     * 填充应用名
     *
     * @param plan 计划
     */
    private void fillAppName(Plan plan) {
        if (!StringUtils.isEmpty(plan.appName)) return;
        App app = appService.get(plan.appId);
        if (app == null) throw new NotFoundException("所属应用不存在");
        plan.appName = app.name;
    }
}
