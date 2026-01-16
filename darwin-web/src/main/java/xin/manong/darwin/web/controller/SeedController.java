package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.SeedRecord;
import xin.manong.darwin.service.component.PlanExecutor;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.SeedService;
import xin.manong.darwin.service.request.SeedSearchRequest;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.common.request.SeedRequest;
import xin.manong.darwin.web.request.SeedUpdateRequest;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.ArrayList;
import java.util.List;

/**
 * 种子URL控制器
 *
 * @author frankcl
 * @date 2025-04-01 14:44:36
 */
@RestController
@Controller
@Path("/api/seed")
@RequestMapping("/api/seed")
public class SeedController {

    @Resource
    private SeedService seedService;
    @Resource
    private PlanService planService;
    @Resource
    private PlanExecutor planExecutor;
    @Resource
    private PermissionSupport permissionSupport;

    /**
     * 抓取种子
     *
     * @param key 种子key
     * @return 成功返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("fetch")
    @GetMapping("fetch")
    public Boolean fetch(@QueryParam("key") String key) {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("种子key缺失");
        SeedRecord seedRecord = seedService.get(key);
        if (seedRecord == null) throw new NotFoundException("种子不存在");
        Plan plan = planService.get(seedRecord.planId);
        if (plan == null) throw new NotFoundException("计划不存在");
        permissionSupport.checkAppPermission(plan.appId);
        if (plan.status == null || !plan.status) throw new IllegalStateException("计划处于关闭状态");
        List<SeedRecord> seedRecords = new ArrayList<>();
        seedRecords.add(seedRecord);
        planService.beforeOpenExecute(seedRecord.planId, seedRecords);
        if (!planExecutor.checkBeforeExecute()) throw new IllegalStateException("并发队列内存处于危险状态");
        if (!planExecutor.execute(plan, seedRecords)) throw new InternalServerErrorException("执行种子抓取计划失败");
        return true;
    }

    /**
     * 根据key获取种子URL记录
     *
     * @param key key
     * @return 种子URL记录
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    public SeedRecord get(@QueryParam("key") String key) {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("种子key缺失");
        return seedService.get(key);
    }

    /**
     * 获取计划种子URL列表
     *
     * @param planId 计划ID
     * @return 种子URL列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("getList")
    @GetMapping("getList")
    public List<SeedRecord> getList(@QueryParam("plan_id") String planId) {
        if (StringUtils.isEmpty(planId)) throw new BadRequestException("计划ID缺失");
        return seedService.getList(planId);
    }

    /**
     * 搜索种子URL列表
     *
     * @param request 搜索请求
     * @return 种子URL列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    public Pager<SeedRecord> search(@BeanParam SeedSearchRequest request) {
        return seedService.search(request);
    }

    /**
     * 添加种子URL
     *
     * @param request 种子URL
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    @EnableWebLogAspect
    public Boolean add(@RequestBody SeedRequest request) {
        if (request == null) throw new BadRequestException("种子添加请求为空");
        request.check();
        SeedRecord record = Converter.convert(request);
        record.check();
        checkAppPermission(record);
        return seedService.add(record);
    }

    /**
     * 更新种子URL
     *
     * @param request 更新种子URL
     * @return 更新成功返回true，否则返回false
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update")
    @PutMapping("update")
    @EnableWebLogAspect
    public Boolean update(@RequestBody SeedUpdateRequest request) {
        if (request == null) throw new BadRequestException("种子更新请求为空");
        request.check();
        SeedRecord prevRecord = seedService.get(request.key);
        if (prevRecord == null) throw new NotFoundException("种子URL不存在");
        checkAppPermission(prevRecord);
        SeedRecord record = Converter.convert(request);
        return seedService.update(record);
    }

    /**
     * 删除种子URL
     *
     * @param key 种子URL key
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    @EnableWebLogAspect
    public Boolean delete(@QueryParam("key") String key) {
        if (StringUtils.isEmpty(key)) throw new BadRequestException("key为空");
        SeedRecord record = seedService.get(key);
        if (record == null) throw new NotFoundException("种子URL不存在");
        checkAppPermission(record);
        return seedService.delete(key);
    }

    /**
     * 删除计划相关种子
     *
     * @param planId 计划ID
     * @return 成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("deleteByPlan")
    @DeleteMapping("deleteByPlan")
    @EnableWebLogAspect
    public Boolean deleteByPlan(@QueryParam("plan_id") String planId) {
        if (StringUtils.isEmpty(planId)) throw new BadRequestException("计划ID缺失");
        Plan plan = planService.get(planId);
        if (plan == null) throw new NotFoundException("计划不存在");
        permissionSupport.checkAppPermission(plan.appId);
        return seedService.deleteByPlan(planId);
    }

    /**
     * 检测应用权限
     *
     * @param record 种子
     */
    private void checkAppPermission(SeedRecord record) {
        Plan plan = planService.get(record.planId);
        if (plan == null) throw new ForbiddenException("计划不存在");
        permissionSupport.checkAppPermission(plan.appId);
    }
}
