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
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.component.RecordDispatcher;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.request.JobSearchRequest;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.darwin.web.component.PermissionSupport;

import java.util.ArrayList;

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
    private JobService jobService;
    @Resource
    private URLService urlService;
    @Resource
    private RecordDispatcher dispatcher;
    @Resource
    private PermissionSupport permissionSupport;

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
    public Double progress(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("任务ID缺失");
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.jobId = id;
        long total = urlService.selectCount(searchRequest);
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_QUEUING);
        searchRequest.statusList.add(Constants.URL_STATUS_FETCHING);
        long notCompletedCount = urlService.selectCount(searchRequest);
        double rate = total == 0d ? 0d : (total - notCompletedCount) * 1.0d / total;
        return Double.parseDouble(String.format("%.2f", rate));
    }

    /**
     * 删除任务
     *
     * @param id 任务ID
     * @return 成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @GetMapping("delete")
    public Boolean delete(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("任务ID缺失");
        Job job = jobService.get(id);
        if (job == null) throw new NotFoundException("任务不存在");
        permissionSupport.checkAppPermission(job.appId);
        return jobService.delete(id);
    }

    /**
     * 获取任务抓取成功率
     *
     * @param id 任务ID
     * @return 抓取成功率
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("successRate")
    @GetMapping("successRate")
    public Double successRate(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("任务ID缺失");
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.jobId = id;
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_FETCH_SUCCESS);
        long successCount = urlService.selectCount(searchRequest);
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_FETCH_SUCCESS);
        searchRequest.statusList.add(Constants.URL_STATUS_FETCH_FAIL);
        searchRequest.statusList.add(Constants.URL_STATUS_ERROR);
        long total = urlService.selectCount(searchRequest);
        double rate = total == 0d ? 0d : successCount * 1.0d / total;
        return Double.parseDouble(String.format("%.2f", rate));
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
    public Pager<Job> search(@BeanParam JobSearchRequest request) {
        return jobService.search(request);
    }

    /**
     * 分发任务抓取数据
     *
     * @param id 任务ID
     * @return 成功返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("dispatch")
    @GetMapping("dispatch")
    public boolean dispatch(@QueryParam("id") String id) {
        if (StringUtils.isEmpty(id)) throw new BadRequestException("任务ID为空");
        Job job = jobService.get(id);
        if (job == null) throw new NotFoundException("任务不存在");
        if (job.status == null || job.status) throw new IllegalStateException("任务未完成");
        permissionSupport.checkAppPermission(job.appId);
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.jobId = id;
        searchRequest.statusList = new ArrayList<>();
        searchRequest.statusList.add(Constants.URL_STATUS_FETCH_SUCCESS);
        searchRequest.pageNum = 1;
        searchRequest.pageSize = 100;
        while (true) {
            Pager<URLRecord> pager = urlService.search(searchRequest);
            if (pager.records == null || pager.records.size() < searchRequest.pageSize) break;
            pager.records.forEach(record -> dispatcher.push(record));
        }
        return true;
    }
}
