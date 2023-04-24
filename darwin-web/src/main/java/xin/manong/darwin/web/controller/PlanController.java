package xin.manong.darwin.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.model.App;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.service.iface.AppService;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.request.PlanSearchRequest;

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
            throw new RuntimeException("计划或计划ID为空");
        }
        if (planService.get(plan.planId) == null) {
            logger.error("plan[{}] is not found", plan.planId);
            throw new RuntimeException(String.format("计划[%s]不存在", plan.planId));
        }
        return planService.update(plan);
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
