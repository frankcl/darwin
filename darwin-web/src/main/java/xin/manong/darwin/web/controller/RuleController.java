package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.RuleHistory;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.request.RuleSearchRequest;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.RuleRequest;
import xin.manong.darwin.web.request.RuleRollBackRequest;
import xin.manong.darwin.web.request.RuleUpdateRequest;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

/**
 * 规则控制器
 *
 * @author frankcl
 * @date 2023-04-24 11:02:37
 */
@RestController
@Controller
@Path("/rule")
@RequestMapping("/rule")
public class RuleController {

    @Resource
    protected RuleService ruleService;
    @Resource
    protected PermissionSupport permissionSupport;

    /**
     * 根据ID获取规则
     *
     * @param id 规则ID
     * @return 规则信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    @EnableWebLogAspect
    public Rule get(@QueryParam("id") Integer id) {
        if (id == null) throw new BadRequestException("规则ID缺失");
        return ruleService.get(id);
    }

    /**
     * 搜索规则
     *
     * @param request 规则搜索请求
     * @return 规则分页列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    @EnableWebLogAspect
    public Pager<Rule> search(@BeanParam RuleSearchRequest request) {
        return ruleService.search(request);
    }

    /**
     * 添加规则
     *
     * @param request 规则
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    @EnableWebLogAspect
    public Boolean add(@RequestBody RuleRequest request) {
        if (request == null) throw new BadRequestException("规则请求信息为空");
        request.check();
        Rule rule = Converter.convert(request);
        rule.check();
        permissionSupport.checkAppPermission(rule.appId);
        return ruleService.add(rule);
    }

    /**
     * 更新规则
     *
     * @param request 规则更新信息
     * @return 更新成功返回true，否则返回false
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    public Boolean update(@RequestBody RuleUpdateRequest request) {
        if (request == null) throw new BadRequestException("规则更新信息为空");
        request.check();
        Rule rule = ruleService.get(request.id);
        if (rule == null) throw new NotFoundException("规则不存在");
        permissionSupport.checkAppPermission(rule.appId);
        Rule updateRule = Converter.convert(request);
        return ruleService.update(updateRule);
    }

    /**
     * 删除规则
     *
     * @param id 规则ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    @EnableWebLogAspect
    public Boolean delete(@QueryParam("id") Integer id) {
        if (id == null) throw new BadRequestException("规则ID为空");
        Rule rule = ruleService.get(id);
        if (rule == null) throw new NotFoundException("规则不存在");
        permissionSupport.checkAppPermission(rule.appId);
        return ruleService.delete(id);
    }

    /**
     * 获取规则历史
     *
     * @param id 规则历史ID
     * @return 成功返回规则历史，否则返回null
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getHistory")
    @GetMapping("getHistory")
    @EnableWebLogAspect
    public RuleHistory getHistory(@QueryParam("id") Integer id) {
        if (id == null) throw new BadRequestException("规则历史ID缺失");
        return ruleService.getRuleHistory(id);
    }

    /**
     * 删除规则历史
     *
     * @param id 规则历史ID
     * @return 成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("deleteHistory")
    @DeleteMapping("deleteHistory")
    @EnableWebLogAspect
    public Boolean deleteHistory(@QueryParam("id") Integer id) {
        if (id == null) throw new BadRequestException("规则历史ID为空");
        RuleHistory ruleHistory = ruleService.getRuleHistory(id);
        if (ruleHistory == null) throw new NotFoundException("规则历史不存在");
        Rule rule = ruleService.get(id);
        if (rule == null) throw new NotFoundException("规则不存在");
        permissionSupport.checkAppPermission(rule.appId);
        return ruleService.removeHistory(id);
    }

    /**
     * 列表规则历史
     *
     * @param ruleId 规则ID
     * @param current 页码，从1开始
     * @param size 分页数量
     * @return 规则历史分页列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("listHistory")
    @GetMapping("listHistory")
    @EnableWebLogAspect
    public Pager<RuleHistory> listHistory(@QueryParam("rule_id") Integer ruleId,
                                          @QueryParam("current") Integer current,
                                          @QueryParam("size") Integer size) {
        if (ruleId == null) throw new BadRequestException("规则ID为空");
        current = current == null || current < 1 ? Constants.DEFAULT_CURRENT : current;
        size = size == null || size <= 0 ? Constants.DEFAULT_PAGE_SIZE : size;
        return ruleService.listHistory(ruleId, current, size);
    }

    /**
     * 回滚规则
     *
     * @param rollBackRequest 回滚请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("rollback")
    @PostMapping("rollback")
    @EnableWebLogAspect
    public Boolean rollBack(@RequestBody RuleRollBackRequest rollBackRequest) {
        if (rollBackRequest == null) throw new BadRequestException("规则回滚请求为空");
        rollBackRequest.check();
        Rule rule = ruleService.get(rollBackRequest.ruleId);
        if (rule == null) throw new NotFoundException("规则不存在");
        permissionSupport.checkAppPermission(rule.appId);
        return ruleService.rollBack(rollBackRequest.ruleId, rollBackRequest.ruleHistoryId);
    }
}
