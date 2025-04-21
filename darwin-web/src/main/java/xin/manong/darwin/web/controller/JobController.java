package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.URLGroupCount;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.request.JobSearchRequest;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.ArrayList;
import java.util.List;

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
    @Resource
    protected URLService urlService;

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
     * 获取任务进度
     *
     * @param id 任务ID
     * @return 任务进度
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("progress")
    @GetMapping("progress")
    @EnableWebLogAspect
    public Double progress(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("任务ID缺失");
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.jobId = id;
        long total = urlService.selectCount(searchRequest);
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_CREATED);
        searchRequest.statusList.add(Constants.URL_STATUS_QUEUING);
        searchRequest.statusList.add(Constants.URL_STATUS_FETCHING);
        long notCompletedCount = urlService.selectCount(searchRequest);
        double rate = total == 0d ? 0d : (total - notCompletedCount) * 1.0d / total;
        return Double.parseDouble(String.format("%.2f", rate));
    }

    /**
     * 根据分组统计任务数据状态
     *
     * @param id 任务ID
     * @return 统计结果
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("bucketCountGroupByStatus")
    @GetMapping("bucketCountGroupByStatus")
    @EnableWebLogAspect
    public List<URLGroupCount> bucketCountGroupByStatus(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("任务ID缺失");
        return urlService.bucketCountGroupByStatus(id);
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
