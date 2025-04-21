package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.manong.darwin.common.model.Message;
import xin.manong.darwin.runner.manage.ExecuteRunnerMeta;
import xin.manong.darwin.runner.manage.ExecuteRunnerRegistry;
import xin.manong.darwin.service.iface.MessageService;
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
@Path("/api/runner")
@RequestMapping("/api/runner")
public class RunnerController {

    @Resource
    protected MessageService messageService;
    @Resource
    protected ExecuteRunnerRegistry executeRunnerRegistry;

    /**
     * 获取执行器列表
     *
     * @param type 执行器类型
     * @return 执行器列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getList")
    @GetMapping("getList")
    @EnableWebLogAspect
    public List<ExecuteRunnerMeta> getList(@QueryParam("type") int type) {
        return executeRunnerRegistry.getList(type);
    }

    /**
     * 判断执行器是否运行
     *
     * @param key 执行器key
     * @return 运行返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("isRunning")
    @GetMapping("isRunning")
    @EnableWebLogAspect
    public boolean isRunning(@QueryParam("key") String key) {
        return executeRunnerRegistry.isRunning(key);
    }

    /**
     * 弹出执行器消息
     *
     * @param key 执行器key
     * @return 消息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("popMessage")
    @GetMapping("popMessage")
    @EnableWebLogAspect
    public Message popMessage(@QueryParam("key") String key) {
        Message message = messageService.pop(key, Message.SOURCE_TYPE_RUNNER);
        if (message == null) throw new NotFoundException("未发现消息");
        return message;
    }

    /**
     * 获取执行器消息数量
     *
     * @param key 执行器key
     * @return 消息数量
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("messageCount")
    @GetMapping("messageCount")
    @EnableWebLogAspect
    public Long messageCount(@QueryParam("key") String key) {
        return messageService.messageCount(key, Message.SOURCE_TYPE_RUNNER);
    }

    /**
     * 启动执行器
     *
     * @param key 执行器key
     * @return 成功返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("start")
    @GetMapping("start")
    @EnableWebLogAspect
    public boolean start(@QueryParam("key") String key) {
        return executeRunnerRegistry.start(key);
    }

    /**
     * 停止执行器
     *
     * @param key 执行器key
     * @return 成功返回true，否则返回false
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("stop")
    @GetMapping("stop")
    @EnableWebLogAspect
    public boolean stop(@QueryParam("key") String key) {
        return executeRunnerRegistry.stop(key);
    }
}
