package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.request.JobSearchRequest;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

/**
 * 任务控制器
 *
 * @author frankcl
 * @date 2023-04-24 14:44:36
 */
@RestController
@Controller
@Path("/api/job")
@RequestMapping("/api/job")
public class JobController {

    @Resource
    protected JobService jobService;

    /**
     * 根据ID获取任务
     *
     * @param id 任务ID
     * @return 任务
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    @EnableWebLogAspect
    public Job get(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("任务ID缺失");
        return jobService.get(id);
    }

    /**
     * 搜索任务
     *
     * @param request 搜索请求
     * @return 任务分页列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    @EnableWebLogAspect
    public Pager<Job> search(@BeanParam JobSearchRequest request) {
        return jobService.search(request);
    }
}
