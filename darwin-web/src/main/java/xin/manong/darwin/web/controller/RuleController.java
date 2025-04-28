package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.RuleHistory;
import xin.manong.darwin.parser.service.ParseService;
import xin.manong.darwin.parser.service.request.CompileRequest;
import xin.manong.darwin.parser.service.response.CompileResult;
import xin.manong.darwin.service.iface.PlanService;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.request.RuleSearchRequest;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.RuleRequest;
import xin.manong.darwin.web.request.RuleRollBackRequest;
import xin.manong.darwin.web.request.RuleUpdateRequest;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.model.User;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.List;

/**
 * 规则控制器
 *
 * @author frankcl
 * @date 2023-04-24 11:02:37
 */
@RestController
@Controller
@Path("/api/rule")
@RequestMapping("/api/rule")
public class RuleController {

    @Resource
    private RuleService ruleService;
    @Resource
    private PlanService planService;
    @Resource
    private ParseService parseService;
    @Resource
    private PermissionSupport permissionSupport;

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
     * 获取计划相关规则列表
     *
     * @param planId 计划ID
     * @return 规则列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("planRules")
    @GetMapping("planRules")
    @EnableWebLogAspect
    public List<Rule> getRules(@QueryParam("plan_id") String planId) {
        if (StringUtils.isEmpty(planId)) throw new BadRequestException("计划ID为空");
        return ruleService.getRules(planId);
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
        checkAppPermission(rule);
        checkScript(rule);
        User user = ContextManager.getUser();
        if (user != null) rule.creator = rule.modifier = user.name;
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
        checkAppPermission(rule);
        Rule updateRule = Converter.convert(request);
        if (StringUtils.isNotEmpty(updateRule.script)) checkScript(updateRule);
        User user = ContextManager.getUser();
        if (user != null) updateRule.modifier = user.name;
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
        checkAppPermission(rule);
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
    @Path("history/get")
    @GetMapping("history/get")
    @EnableWebLogAspect
    public RuleHistory getHistory(@QueryParam("id") Integer id) {
        if (id == null) throw new BadRequestException("规则历史ID缺失");
        return ruleService.getHistory(id);
    }

    /**
     * 删除规则历史
     *
     * @param id 规则历史ID
     * @return 成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("history/delete")
    @DeleteMapping("history/delete")
    @EnableWebLogAspect
    public Boolean deleteHistory(@QueryParam("id") Integer id) {
        if (id == null) throw new BadRequestException("规则历史ID为空");
        RuleHistory ruleHistory = ruleService.getHistory(id);
        if (ruleHistory == null) throw new NotFoundException("规则历史不存在");
        Rule rule = ruleService.get(ruleHistory.ruleId);
        if (rule == null) throw new NotFoundException("规则不存在");
        checkAppPermission(rule);
        return ruleService.removeHistory(id);
    }

    /**
     * 搜索规则历史
     *
     * @param ruleId 规则ID
     * @param pageNum 页码，从1开始
     * @param pageSize 分页数量
     * @return 规则历史分页列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("history/search")
    @GetMapping("history/search")
    @EnableWebLogAspect
    public Pager<RuleHistory> searchHistory(@QueryParam("rule_id") Integer ruleId,
                                            @QueryParam("page_num") Integer pageNum,
                                            @QueryParam("page_size") Integer pageSize) {
        if (ruleId == null) throw new BadRequestException("规则ID为空");
        pageNum = pageNum == null || pageNum < 1 ? Constants.DEFAULT_PAGE_NUM : pageNum;
        pageSize = pageSize == null || pageSize <= 0 ? Constants.DEFAULT_PAGE_SIZE : pageSize;
        return ruleService.getHistoryList(ruleId, pageNum, pageSize);
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
    public Boolean rollback(@RequestBody RuleRollBackRequest rollBackRequest) {
        if (rollBackRequest == null) throw new BadRequestException("规则回滚请求为空");
        rollBackRequest.check();
        Rule rule = ruleService.get(rollBackRequest.ruleId);
        if (rule == null) throw new NotFoundException("规则不存在");
        checkAppPermission(rule);
        User user = ContextManager.getUser();
        return ruleService.rollback(rollBackRequest.ruleId,
                rollBackRequest.ruleHistoryId, user != null ? user.name : null);
    }

    /**
     * 检测脚本
     *
     * @param rule 规则
     */
    private void checkScript(Rule rule) {
        CompileRequest compileRequest = new CompileRequest();
        compileRequest.script = rule.script;
        compileRequest.scriptType = rule.scriptType;
        CompileResult compileResult = parseService.compile(compileRequest);
        if (!compileResult.status) throw new BadRequestException(compileResult.message);
    }

    /**
     * 检测应用权限
     *
     * @param rule 规则
     */
    private void checkAppPermission(Rule rule) {
        Plan plan = planService.get(rule.planId);
        if (plan == null) throw new ForbiddenException("计划不存在");
        permissionSupport.checkAppPermission(plan.appId);
    }
}
