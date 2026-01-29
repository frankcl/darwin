package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.SeedRecord;
import xin.manong.darwin.common.request.AuthenticateRequest;
import xin.manong.darwin.common.request.PlanExecuteRequest;
import xin.manong.darwin.common.request.SeedRequest;
import xin.manong.darwin.common.request.SetCookieRequest;
import xin.manong.darwin.service.component.PlanExecutor;
import xin.manong.darwin.service.iface.CookieService;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.SeedService;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.List;

/**
 * AK/SK认证服务接口
 *
 * @author frankcl
 * @date 2025-12-29 13:39:05
 */
@RestController
@Controller
@Path("/api/auth")
@RequestMapping("/api/auth")
public class AuthenticateController {

    @Resource
    private CookieService cookieService;
    @Resource
    private SeedService seedService;
    @Resource
    private PlanService planService;
    @Resource
    private PlanExecutor planExecutor;
    @Resource
    private PermissionSupport permissionSupport;

    /**
     * 添加种子URL
     *
     * @param request 种子URL
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("seed/add")
    @PutMapping("seed/add")
    @EnableWebLogAspect
    public Boolean addSeed(@RequestBody SeedRequest request) {
        if (request == null) throw new BadRequestException("种子添加请求为空");
        ((AuthenticateRequest) request).check();
        request.check();
        Plan plan = planService.get(request.planId);
        if (plan == null) throw new NotFoundException("计划不存在");
        permissionSupport.checkAuthPermission(plan.appId, request);
        SeedRecord record = Converter.convert(request);
        record.check();
        return seedService.add(record);
    }

    /**
     * 设置Cookie
     *
     * @param request 请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("cookie/set")
    @PostMapping("cookie/set")
    @EnableWebLogAspect
    public boolean setCookie(@RequestBody SetCookieRequest request) {
        if (request == null) throw new BadRequestException("Cookie设置请求为空");
        ((AuthenticateRequest) request).check();
        request.check();
        permissionSupport.checkAuthPermission(-1, request);
        cookieService.setCookie(request.key, request.cookie);
        return true;
    }

    /**
     * 提交计划执行请求
     *
     * @param request 计划执行请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("plan/submit")
    @PostMapping("plan/submit")
    @EnableWebLogAspect
    public Boolean submitPlan(@RequestBody PlanExecuteRequest request) {
        if (request == null) throw new BadRequestException("执行计划请求为空");
        request.check();
        Plan plan = planService.get(request.planId);
        if (plan == null) throw new NotFoundException("计划不存在");
        permissionSupport.checkAuthPermission(plan.appId, request);
        if (plan.status == null || !plan.status) throw new IllegalStateException("计划处于关闭状态");
        List<SeedRecord> seedRecords = request.seeds.stream().map(Converter::convert).toList();
        planService.beforeOpenExecute(request.planId, seedRecords);
        if (!planExecutor.checkBeforeExecute()) throw new IllegalStateException("并发队列内存处于危险状态");
        if (!planExecutor.execute(plan, seedRecords)) throw new InternalServerErrorException("执行计划失败");
        return true;
    }
}
