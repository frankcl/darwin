package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.model.App;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.iface.AppService;
import xin.manong.darwin.service.request.AppSearchRequest;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.AppRequest;
import xin.manong.darwin.web.request.AppUpdateRequest;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.model.User;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

/**
 * 应用控制器
 *
 * @author frankcl
 * @date 2023-04-24 10:29:52
 */
@RestController
@Controller
@Path("/api/app")
@RequestMapping("/api/app")
public class AppController {

    @Resource
    private AppService appService;
    @Resource
    private PermissionSupport permissionSupport;

    /**
     * 根据应用名搜索应用
     *
     * @param searchRequest 搜索请求
     * @return 应用分页数据
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @GetMapping("search")
    public Pager<App> search(@BeanParam AppSearchRequest searchRequest) {
        return appService.search(searchRequest);
    }

    /**
     * 根据ID获取应用信息
     *
     * @param id 应用ID
     * @return 应用信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    public App get(@QueryParam("id") Integer id) {
        if (id == null) throw new BadRequestException("应用ID缺失");
        return appService.get(id);
    }

    /**
     * 添加应用信息
     *
     * @param request 应用信息
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    @EnableWebLogAspect
    public Boolean add(@RequestBody AppRequest request) {
        if (request == null) throw new BadRequestException("应用信息为空");
        request.check();
        App app = Converter.convert(request);
        User user = ContextManager.getUser();
        if (user != null) {
            app.creatorId = user.id;
            app.creator = user.name;
            app.modifier = user.name;
        }
        return appService.add(app);
    }

    /**
     * 更新应用信息
     *
     * @param request 更新应用信息
     * @return 更新成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    public Boolean update(@RequestBody AppUpdateRequest request) {
        if (request == null) throw new BadRequestException("更新应用信息为空");
        request.check();
        permissionSupport.checkAppPermission(request.id);
        User user = ContextManager.getUser();
        App app = Converter.convert(request);
        if (user != null) app.modifier = user.name;
        return appService.update(app);
    }

    /**
     * 删除应用信息
     *
     * @param id 应用ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    @EnableWebLogAspect
    public Boolean delete(@QueryParam("id") Integer id) {
        if (id == null) throw new BadRequestException("应用ID缺失");
        permissionSupport.checkAppPermission(id);
        return appService.delete(id);
    }
}
