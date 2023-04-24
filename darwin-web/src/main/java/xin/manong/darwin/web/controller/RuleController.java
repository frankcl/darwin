package xin.manong.darwin.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.service.iface.RuleGroupService;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.request.RuleSearchRequest;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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

    private static final Logger logger = LoggerFactory.getLogger(RuleController.class);

    @Resource
    protected RuleService ruleService;
    @Resource
    protected RuleGroupService ruleGroupService;

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
    public Rule get(@QueryParam("id") Long id) {
        if (id == null) {
            logger.error("missing param[id]");
            throw new BadRequestException("规则ID缺失");
        }
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
    public Pager<Rule> search(RuleSearchRequest request) {
        if (request == null) request = new RuleSearchRequest();
        if (request.current == null || request.current < 1) request.current = 1;
        if (request.size == null || request.size <= 0) request.size = 20;
        return ruleService.search(request);
    }

    /**
     * 添加规则
     *
     * @param rule 规则
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    public Boolean add(Rule rule) {
        if (rule == null || !rule.check()) {
            logger.error("rule is null or is not valid");
            throw new BadRequestException("规则为空或非法");
        }
        if (ruleGroupService.get(rule.ruleGroup) == null) {
            logger.error("rule group[{}] is not found", rule.ruleGroup);
            throw new NotFoundException(String.format("规则分组[%d]不存在", rule.ruleGroup));
        }
        rule.id = null;
        return ruleService.add(rule);
    }

    /**
     * 更新规则
     *
     * @param rule 规则
     * @return 更新成功返回true，否则返回false
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    public Boolean update(Rule rule) {
        if (rule == null || rule.id == null) {
            logger.error("rule is null or rule id is null");
            throw new BadRequestException("规则为空或规则ID为空");
        }
        if (ruleService.get(rule.id) == null) {
            logger.error("rule[{}] is not found", rule.id);
            throw new NotFoundException(String.format("规则[%d]不存在", rule.id));
        }
        return ruleService.update(rule);
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
    public Boolean delete(@QueryParam("id") Long id) {
        if (id == null) {
            logger.error("rule id is null");
            throw new BadRequestException("规则ID为空");
        }
        if (ruleService.get(id) == null) {
            logger.error("rule[{}] is not found", id);
            throw new NotFoundException(String.format("规则[%d]不存在", id));
        }
        return ruleService.delete(id);
    }
}
