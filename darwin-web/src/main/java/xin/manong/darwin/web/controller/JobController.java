package xin.manong.darwin.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.request.JobSearchRequest;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 任务控制器
 *
 * @author frankcl
 * @date 2023-04-24 14:44:36
 */
@RestController
@Controller
@Path("/job")
@RequestMapping("/job")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

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
    public Job get(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("job id is empty");
            throw new BadRequestException("任务ID缺失");
        }
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
    public Pager<Job> search(JobSearchRequest request) {
        if (request == null) request = new JobSearchRequest();
        if (request.current == null || request.current < 1) request.current = 1;
        if (request.size == null || request.size <= 0) request.size = 20;
        return jobService.search(request);
    }
}
