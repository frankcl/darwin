package xin.manong.darwin.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.model.App;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.iface.AppService;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 应用控制器
 *
 * @author frankcl
 * @date 2023-04-24 10:29:52
 */
@RestController
@Controller
@Path("/app")
@RequestMapping("/app")
public class AppController {

    private static final Logger logger = LoggerFactory.getLogger(AppController.class);

    @Resource
    protected AppService appService;

    /**
     * 根据应用名搜索应用
     * 应用名为空返回所有应用列表
     *
     * @param name 应用名
     * @param current 页码，从1开始
     * @param size 分页大小，默认20
     * @return 应用分页数据
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    public Pager<App> search(@QueryParam("name") String name,
                             @QueryParam("current") Integer current,
                             @QueryParam("size") Integer size) {
        if (current == null || current < 1) current = 1;
        if (size == null || size <= 0) size = 20;
        return appService.search(name, current, size);
    }

    /**
     * 根据ID获取应用信息
     *
     * @return 应用信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    public App get(@QueryParam("app_id") Long appId) {
        if (appId == null) {
            logger.error("missing param[app_id]");
            throw new RuntimeException("应用ID缺失");
        }
        return appService.get(appId);
    }

    /**
     * 添加应用信息
     *
     * @param app 应用信息
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    public Boolean add(App app) {
        if (app == null || !app.check()) {
            logger.error("app is null or not valid");
            throw new RuntimeException("应用信息非法");
        }
        return appService.add(app);
    }

    /**
     * 更新应用信息
     *
     * @param app 应用信息
     * @return 更新成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    public Boolean update(App app) {
        if (app == null || app.id == null) {
            logger.error("app is null or app id is null");
            throw new RuntimeException("应用信息或ID为空");
        }
        if (appService.get(app.id) == null) {
            logger.error("app is not found for id[{}]", app.id);
            throw new RuntimeException(String.format("应用[%d]不存在", app.id));
        }
        return appService.update(app);
    }

    /**
     * 删除应用信息
     *
     * @param appId 应用ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    public Boolean delete(@QueryParam("app_id") Long appId) {
        if (appId == null) {
            logger.error("missing param[app_id]");
            throw new RuntimeException("应用ID缺失");
        }
        if (appService.get(appId) == null) {
            logger.error("app is not found for id[{}]", appId);
            throw new RuntimeException(String.format("应用[%d]不存在", appId));
        }
        return appService.delete(appId);
    }
}
