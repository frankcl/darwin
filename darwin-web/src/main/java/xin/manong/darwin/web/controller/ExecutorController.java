package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.common.model.Executor;
import xin.manong.darwin.executor.GlobalExecutorRegistry;
import xin.manong.darwin.service.iface.ExecutorService;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.List;

/**
 * 执行器控制器
 *
 * @author frankcl
 * @date 2025-03-13 14:44:36
 */
@RestController
@Controller
@Path("/api/executor")
@RequestMapping("/api/executor")
public class ExecutorController {

    @Resource
    protected ExecutorService executorService;
    @Resource
    protected GlobalExecutorRegistry globalExecutorRegistry;

    /**
     * 获取执行器列表
     *
     * @return 执行器列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("getList")
    @GetMapping("getList")
    @EnableWebLogAspect
    public List<Executor> getList() {
        return executorService.getList();
    }

    /**
     * 启动执行器
     *
     * @param name 名称
     * @return 成功返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("start")
    @GetMapping("start")
    @EnableWebLogAspect
    public boolean start(@QueryParam("name") String name) {
        return globalExecutorRegistry.start(name);
    }

    /**
     * 停止执行器
     *
     * @param name 名称
     * @return 成功返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("stop")
    @GetMapping("stop")
    @EnableWebLogAspect
    public boolean stop(@QueryParam("name") String name) {
        return globalExecutorRegistry.stop(name);
    }
}
