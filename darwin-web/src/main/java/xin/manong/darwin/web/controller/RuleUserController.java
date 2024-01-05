package xin.manong.darwin.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RuleUser;
import xin.manong.darwin.service.iface.RuleUserService;
import xin.manong.darwin.service.request.RuleUserSearchRequest;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.RuleUserRequest;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 规则用户关系控制器
 *
 * @author frankcl
 * @date 2023-10-20 10:29:52
 */
@RestController
@Controller
@Path("/rule_user")
@RequestMapping("/rule_user")
public class RuleUserController {

    private static final Logger logger = LoggerFactory.getLogger(RuleUserController.class);

    @Resource
    protected RuleUserService ruleUserService;
    @Resource
    protected PermissionSupport permissionSupport;

    /**
     * 获取规则用户列表
     *
     * @param ruleId 规则ID
     * @param current 页码，从1开始
     * @param size 分页大小，默认20
     * @return 应用用户关系分页数据
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getRuleUsers")
    @GetMapping("getRuleUsers")
    @EnableWebLogAspect
    public Pager<RuleUser> getRuleUsers(@QueryParam("rule_id") Integer ruleId,
                                        @QueryParam("current") Integer current,
                                        @QueryParam("size") Integer size) {
        if (current == null || current < 1) current = Constants.DEFAULT_CURRENT;
        if (size == null || size <= 0) size = Constants.DEFAULT_PAGE_SIZE;
        if (ruleId == null) {
            logger.error("rule id is null");
            throw new BadRequestException("规则ID为空");
        }
        RuleUserSearchRequest searchRequest = new RuleUserSearchRequest();
        searchRequest.current = current;
        searchRequest.size = size;
        searchRequest.ruleId = ruleId;
        return ruleUserService.search(searchRequest);
    }


    /**
     * 添加规则用户关系
     *
     * @param request 规则用户关系
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    @EnableWebLogAspect
    public Boolean add(RuleUserRequest request) {
        if (request == null) {
            logger.error("rule user relation is null");
            throw new BadRequestException("规则用户关系为空");
        }
        request.check();
        permissionSupport.checkRulePermission(request.ruleId);
        RuleUser ruleUser = Converter.convert(request);
        return ruleUserService.add(ruleUser);
    }

    /**
     * 删除规则用户关系
     *
     * @param id 规则用户关系ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    @EnableWebLogAspect
    public Boolean delete(@QueryParam("id") Integer id) {
        if (id == null) {
            logger.error("missing param[id]");
            throw new BadRequestException("规则用户关系ID缺失");
        }
        RuleUser ruleUser = ruleUserService.get(id);
        if (ruleUser == null) {
            logger.error("rule user relation is not found for id[{}]", id);
            throw new NotFoundException(String.format("规则用户关系[%d]不存在", id));
        }
        permissionSupport.checkRulePermission(ruleUser.ruleId);
        return ruleUserService.delete(id);
    }
}
