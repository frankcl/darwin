package xin.manong.darwin.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.service.iface.RuleGroupService;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.request.RuleSearchRequest;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.RuleRequest;
import xin.manong.darwin.web.request.RuleUpdateRequest;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

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
    @EnableWebLogAspect
    public Rule get(@QueryParam("id") Integer id) {
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
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("search")
    @PostMapping("search")
    @EnableWebLogAspect
    public Pager<Rule> search(RuleSearchRequest request) {
        if (request == null) request = new RuleSearchRequest();
        if (request.current == null || request.current < 1) request.current = Constants.DEFAULT_CURRENT;
        if (request.size == null || request.size <= 0) request.size = Constants.DEFAULT_PAGE_SIZE;
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
    public Boolean add(RuleRequest request) {
        if (request == null) {
            logger.error("rule request is null");
            throw new BadRequestException("规则请求信息为空");
        }
        request.check();
        if (ruleGroupService.get(request.ruleGroup) == null) {
            logger.error("rule group[{}] is not found", request.ruleGroup);
            throw new NotFoundException(String.format("规则分组[%d]不存在", request.ruleGroup));
        }
        Rule rule = Converter.convert(request);
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
    public Boolean update(RuleUpdateRequest request) {
        if (request == null) {
            logger.error("rule update info is null");
            throw new BadRequestException("规则更新信息为空");
        }
        request.check();
        if (ruleService.get(request.id) == null) {
            logger.error("rule[{}] is not found", request.id);
            throw new NotFoundException(String.format("规则[%d]不存在", request.id));
        }
        Rule rule = Converter.convert(request);
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
    @EnableWebLogAspect
    public Boolean delete(@QueryParam("id") Integer id) {
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
