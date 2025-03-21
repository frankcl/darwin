package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.model.AppUser;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.iface.AppUserService;
import xin.manong.darwin.service.request.AppUserSearchRequest;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.AppUserRequest;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

/**
 * 应用用户关系控制器
 *
 * @author frankcl
 * @date 2023-10-20 10:29:52
 */
@RestController
@Controller
@Path("/api/app_user")
@RequestMapping("/api/app_user")
public class AppUserController {

    @Resource
    protected AppUserService appUserService;
    @Resource
    protected PermissionSupport permissionSupport;

    /**
     * 获取应用用户列表
     *
     * @param appId 应用ID
     * @param current 页码，从1开始
     * @param size 分页大小，默认20
     * @return 应用用户关系分页数据
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getAppUsers")
    @GetMapping("getAppUsers")
    @EnableWebLogAspect
    public Pager<AppUser> getAppUsers(@QueryParam("app_id") Integer appId,
                                      @QueryParam("current") Integer current,
                                      @QueryParam("size") Integer size) {
        if (appId == null) throw new BadRequestException("应用ID为空");
        AppUserSearchRequest searchRequest = new AppUserSearchRequest();
        searchRequest.current = current;
        searchRequest.size = size;
        searchRequest.appId = appId;
        return appUserService.search(searchRequest);
    }


    /**
     * 添加应用用户关系
     *
     * @param request 应用用户关系
     * @return 添加成功返回true，否则返回false
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PutMapping("add")
    @EnableWebLogAspect
    public Boolean add(@RequestBody AppUserRequest request) {
        if (request == null) throw new BadRequestException("应用用户关系为空");
        request.check();
        permissionSupport.checkAppPermission(request.appId);
        AppUser appUser = Converter.convert(request);
        return appUserService.add(appUser);
    }

    /**
     * 删除应用用户关系
     *
     * @param id 应用用户关系ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    @EnableWebLogAspect
    public Boolean delete(@QueryParam("id") Integer id) {
        if (id == null) throw new BadRequestException("应用用户关系ID缺失");
        AppUser appUser = appUserService.get(id);
        if (appUser == null) throw new NotFoundException("应用用户关系不存在");
        permissionSupport.checkAppPermission(appUser.appId);
        return appUserService.delete(id);
    }
}
