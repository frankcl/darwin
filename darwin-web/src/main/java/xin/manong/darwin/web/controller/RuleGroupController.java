package xin.manong.darwin.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RuleGroup;
import xin.manong.darwin.service.iface.RuleGroupService;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.RuleGroupRequest;
import xin.manong.darwin.web.request.RuleGroupUpdateRequest;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 规则分组控制器
 *
 * @author frankcl
 * @date 2023-04-24 10:29:52
 */
@RestController
@Controller
@Path("/rule_group")
@RequestMapping("/rule_group")
public class RuleGroupController {

    private static final Logger logger = LoggerFactory.getLogger(RuleGroupController.class);

    @Resource
    protected RuleGroupService ruleGroupService;

    /**
     * 根据名称搜索规则分组
     *
     * @param name 规则分组名
     * @param current 页码，从1开始
     * @param size 分页大小，默认20
     * @return 规则分组分页数据
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    @EnableWebLogAspect
    public Pager<RuleGroup> search(@QueryParam("name") String name,
                                   @QueryParam("current") Integer current,
                                   @QueryParam("size") Integer size) {
        if (current == null || current < 1) current = Constants.DEFAULT_CURRENT;
        if (size == null || size <= 0) size = Constants.DEFAULT_PAGE_SIZE;
        if (StringUtils.isEmpty(name)) {
            logger.error("search rule group name is empty");
            throw new BadRequestException("搜索规则分组名为空");
        }
        return ruleGroupService.search(name, current, size);
    }

    /**
     * 列表规则分组
     *
     * @param current 页码，从1开始
     * @param size 分页大小，默认20
     * @return 规则分组分页列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("list")
    @GetMapping("list")
    @EnableWebLogAspect
    public Pager<RuleGroup> list(@QueryParam("current") Integer current,
                                 @QueryParam("size") Integer size) {
        if (current == null || current < 1) current = Constants.DEFAULT_CURRENT;
        if (size == null || size <= 0) size = Constants.DEFAULT_PAGE_SIZE;
        return ruleGroupService.getList(current, size);
    }

    /**
     * 根据ID获取规则分组
     *
     * @param id 规则分组ID
     * @return 规则分组信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    @EnableWebLogAspect
    public RuleGroup get(@QueryParam("id") Integer id) {
        if (id == null) {
            logger.error("missing param[id]");
            throw new BadRequestException("规则分组ID缺失");
        }
        return ruleGroupService.get(id);
    }

    /**
     * 添加规则分组信息
     *
     * @param request 规则分组信息
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    @EnableWebLogAspect
    public Boolean add(RuleGroupRequest request) {
        if (request == null) {
            logger.error("rule group is null");
            throw new BadRequestException("规则分组信息为空");
        }
        request.check();
        RuleGroup ruleGroup = Converter.convert(request);
        return ruleGroupService.add(ruleGroup);
    }

    /**
     * 更新规则分组信息
     *
     * @param request 规则分组信息
     * @return 更新成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    public Boolean update(RuleGroupUpdateRequest request) {
        if (request == null) {
            logger.error("rule group is null");
            throw new BadRequestException("规则分组更新信息为空");
        }
        request.check();
        if (ruleGroupService.get(request.id) == null) {
            logger.error("rule group is not found for id[{}]", request.id);
            throw new NotFoundException(String.format("规则分组[%d]不存在", request.id));
        }
        RuleGroup ruleGroup = Converter.convert(request);
        return ruleGroupService.update(ruleGroup);
    }

    /**
     * 删除规则分组信息
     *
     * @param id 规则分组ID
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
            throw new BadRequestException("规则分组ID缺失");
        }
        if (ruleGroupService.get(id) == null) {
            logger.error("rule group is not found for id[{}]", id);
            throw new NotFoundException(String.format("规则分组[%d]不存在", id));
        }
        return ruleGroupService.delete(id);
    }
}
