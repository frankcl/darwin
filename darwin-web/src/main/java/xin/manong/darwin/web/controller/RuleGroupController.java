package xin.manong.darwin.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Rule;
import xin.manong.darwin.common.model.RuleGroup;
import xin.manong.darwin.service.iface.RuleGroupService;
import xin.manong.darwin.service.iface.RuleService;
import xin.manong.darwin.service.request.RuleSearchRequest;

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
    protected RuleService ruleService;
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
    public Pager<RuleGroup> search(@QueryParam("name") String name,
                                   @QueryParam("current") Integer current,
                                   @QueryParam("size") Integer size) {
        if (current == null || current < 1) current = 1;
        if (size == null || size <= 0) size = 20;
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
    public Pager<RuleGroup> list(@QueryParam("current") Integer current,
                                 @QueryParam("size") Integer size) {
        if (current == null || current < 1) current = 1;
        if (size == null || size <= 0) size = 20;
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
    public RuleGroup get(@QueryParam("id") Long id) {
        if (id == null) {
            logger.error("missing param[id]");
            throw new BadRequestException("规则分组ID缺失");
        }
        return ruleGroupService.get(id);
    }

    /**
     * 添加规则分组信息
     *
     * @param ruleGroup 规则分组信息
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    public Boolean add(RuleGroup ruleGroup) {
        if (ruleGroup == null || !ruleGroup.check()) {
            logger.error("rule group is null or not valid");
            throw new BadRequestException("规则分组信息非法");
        }
        ruleGroup.id = null;
        ruleGroup.createTime = null;
        ruleGroup.updateTime = null;
        return ruleGroupService.add(ruleGroup);
    }

    /**
     * 更新规则分组信息
     *
     * @param ruleGroup 规则分组信息
     * @return 更新成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    public Boolean update(RuleGroup ruleGroup) {
        if (ruleGroup == null || ruleGroup.id == null) {
            logger.error("rule group is null or rule group id is null");
            throw new BadRequestException("规则分组信息或ID为空");
        }
        if (ruleGroupService.get(ruleGroup.id) == null) {
            logger.error("rule group is not found for id[{}]", ruleGroup.id);
            throw new NotFoundException(String.format("规则分组[%d]不存在", ruleGroup.id));
        }
        ruleGroup.createTime = null;
        ruleGroup.updateTime = null;
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
    public Boolean delete(@QueryParam("id") Long id) {
        if (id == null) {
            logger.error("missing param[id]");
            throw new BadRequestException("规则分组ID缺失");
        }
        if (ruleGroupService.get(id) == null) {
            logger.error("rule group is not found for id[{}]", id);
            throw new NotFoundException(String.format("规则分组[%d]不存在", id));
        }
        RuleSearchRequest searchRequest = new RuleSearchRequest();
        searchRequest.current = 1;
        searchRequest.size = 1;
        searchRequest.ruleGroup = id;
        Pager<Rule> pager = ruleService.search(searchRequest);
        if (pager.total > 0) {
            logger.error("rule exists for group[{}]", id);
            throw new RuntimeException(String.format("规则分组[%d]下存在规则", id));
        }
        return ruleGroupService.delete(id);
    }
}
