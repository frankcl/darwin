package xin.manong.darwin.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.service.iface.AppService;
import xin.manong.darwin.service.iface.MultiQueueService;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.TransactionService;
import xin.manong.darwin.service.request.PlanSearchRequest;
import xin.manong.darwin.web.request.ConsumedPlanSeedURL;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
    protected TransactionService transactionService;
    @Resource
    protected MultiQueueService multiQueueService;

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
            throw new RuntimeException("计划ID缺失");
        }
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
            throw new RuntimeException("计划或所属应用ID为空");
        }
        App app = appService.get(plan.appId.longValue());
        if (app == null) {
            logger.error("app[{}] is not found", plan.appId);
            throw new RuntimeException(String.format("所属应用[%d]不存在", plan.appId));
        }
        plan.appName = app.name;
        if (!plan.check()) {
            logger.error("plan is not valid");
            throw new RuntimeException("计划非法");
        }
        boolean success = planService.add(plan);
        if (!success) return false;
        if (plan.category == Constants.PLAN_CATEGORY_REPEAT) return true;
        Job job = transactionService.buildJob(plan);
        if (job == null) {
            planService.delete(plan.planId);
            logger.error("build job failed for plan[{}]", plan.planId);
            throw new RuntimeException(String.format("%s[%s]构建任务失败",
                    Constants.SUPPORT_PLAN_CATEGORIES.get(plan.category), plan.planId));
        }
        for (URLRecord seedURL : job.seedURLs) {
            URLRecord record = multiQueueService.pushQueue(seedURL);
            logger.info("push record[{}] into queue, status[{}]", record.url,
                    Constants.SUPPORT_URL_STATUSES.get(record.status));
        }
        return true;
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
            throw new RuntimeException("计划或计划ID为空");
        }
        if (planService.get(plan.planId) == null) {
            logger.error("plan[{}] is not found", plan.planId);
            throw new RuntimeException(String.format("计划[%s]不存在", plan.planId));
        }
        //非周期性计划不允许更新种子列表
        if (plan.category != Constants.PLAN_CATEGORY_REPEAT) plan.seedURLs = null;
        return planService.update(plan);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("addConsumeSeeds")
    @PostMapping("addConsumeSeedURLs")
    public Boolean addConsumedSeedURLs(ConsumedPlanSeedURL request) {
        if (request == null) {
            logger.error("consumed plan seed request is null");
            throw new RuntimeException("补充种子请求为空");
        }
        if (StringUtils.isEmpty(request.planId)) {
            logger.error("plan id is empty");
            throw new RuntimeException("计划ID为空");
        }
        if (request.seedURLs == null || request.seedURLs.isEmpty()) {
            logger.error("seed urls are empty");
            throw new RuntimeException("种子URL列表为空");
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
        if (plan.category != Constants.PLAN_CATEGORY_CONSUME) {
            logger.error("plan[%s] is not a consuming plan", Constants.SUPPORT_PLAN_CATEGORIES.get(plan.category));
            throw new RuntimeException(String.format("非消费型计划类型[%s]",
                    Constants.SUPPORT_PLAN_CATEGORIES.get(plan.category)));
        }
        plan.seedURLs = request.seedURLs;
        if (!plan.check()) {
            logger.error("plan is not valid");
            throw new RuntimeException("计划检测失败");
        }
        Job job = transactionService.buildJob(plan);
        if (job == null) {
            logger.error("build job failed for consuming plan[{}]", plan.planId);
            throw new RuntimeException(String.format("消费型计划[%s]构建任务失败", plan.planId));
        }
        for (URLRecord seedURL : job.seedURLs) {
            URLRecord record = multiQueueService.pushQueue(seedURL);
            logger.info("push record[{}] into queue, status[{}]", record.url,
                    Constants.SUPPORT_URL_STATUSES.get(record.status));
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
            throw new RuntimeException("计划ID为空");
        }
        if (planService.get(id) == null) {
            logger.error("plan[{}] is not found", id);
            throw new RuntimeException(String.format("计划[%s]不存在", id));
        }
        return planService.delete(id);
    }
}
