package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.service.component.PlanExecutor;
import xin.manong.darwin.service.iface.*;
import xin.manong.darwin.service.request.PlanSearchRequest;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.PlanRequest;
import xin.manong.darwin.web.request.PlanUpdateRequest;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.model.User;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.List;

/**
 * 计划控制器
 *
 * @author frankcl
 * @date 2023-04-24 11:42:13
 */
@RestController
@Controller
@Path("/api/plan")
@RequestMapping("/api/plan")
public class PlanController {

    @Resource
    private AppService appService;
    @Resource
    private PlanService planService;
    @Resource
    private SeedService seedService;
    @Resource
    private PlanExecutor planExecutor;
    @Resource
    private PermissionSupport permissionSupport;

    /**
     * 开启计划
     *
     * @param id 计划ID
     * @return 开启成功返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("open")
    @GetMapping("open")
    @EnableWebLogAspect
    public Boolean open(@QueryParam("id") String id) {
        checkPermission(id);
        List<SeedRecord> seedRecords = seedService.getList(id);
        planService.beforeOpenExecute(id, seedRecords);
        Plan plan = new Plan();
        plan.planId = id;
        plan.status = true;
        return planService.update(plan);
    }

    /**
     * 关闭计划
     *
     * @param id 计划ID
     * @return 关闭成功返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("close")
    @GetMapping("close")
    @EnableWebLogAspect
    public Boolean close(@QueryParam("id") String id) {
        checkPermission(id);
        Plan plan = new Plan();
        plan.planId = id;
        plan.status = false;
        return planService.update(plan);
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
        User user = ContextManager.getUser();
        if (user != null) plan.creator = plan.modifier = user.name;
        fillAppName(plan);
        plan.planId = RandomID.build();
        if (plan.category == Constants.PLAN_CATEGORY_PERIOD) plan.nextTime = System.currentTimeMillis();
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
        if (request.appId != null) permissionSupport.checkAppPermission(request.appId);
        Plan plan = Converter.convert(request);
        User user = ContextManager.getUser();
        if (user != null) plan.modifier = user.name;
        if (previous.category == Constants.PLAN_CATEGORY_ONCE &&
                plan.category == Constants.PLAN_CATEGORY_PERIOD) {
            plan.nextTime = System.currentTimeMillis();
        }
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
        if (plan.status == null || !plan.status) throw new IllegalStateException("计划处于关闭状态");
        List<SeedRecord> seedRecords = seedService.getList(plan.planId);
        planService.beforeOpenExecute(plan.planId, seedRecords);
        if (!planExecutor.checkBeforeExecute()) throw new IllegalStateException("并发队列内存处于危险状态");
        if (!planExecutor.execute(plan)) throw new InternalServerErrorException("执行计划失败");
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
     * 检测是否有操作计划权限
     *
     * @param planId 计划ID
     */
    private void checkPermission(String planId) {
        if (StringUtils.isEmpty(planId)) throw new BadRequestException("计划ID为空");
        Plan plan = planService.get(planId);
        if (plan == null) throw new NotFoundException("计划不存在");
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
