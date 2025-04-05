package xin.manong.darwin.web.controller;

import com.google.common.collect.Sets;
import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.model.AppUser;
import xin.manong.darwin.service.iface.AppUserService;
import xin.manong.darwin.web.component.PermissionSupport;
import xin.manong.darwin.web.convert.Converter;
import xin.manong.darwin.web.request.AppUserRequest;
import xin.manong.darwin.web.request.BatchAppUserRequest;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.model.User;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
     * @return 应用用户关系列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getAppUsers")
    @GetMapping("getAppUsers")
    @EnableWebLogAspect
    public List<AppUser> getAppUsers(@QueryParam("app_id") Integer appId) {
        if (appId == null) throw new BadRequestException("应用ID为空");
        return appUserService.getAppUsers(appId);
    }

    /**
     * 获取当前用户相关应用列表
     *
     * @return 当前用户相关应用列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getOwnApps")
    @GetMapping("getOwnApps")
    @EnableWebLogAspect
    public List<Integer> getOwnApps() {
        User user = ContextManager.getUser();
        List<AppUser> appUsers = appUserService.getAppUsers(user.getId());
        return appUsers == null ? new ArrayList<>() : appUsers.stream().map(
                AppUser::getAppId).collect(Collectors.toList());
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

    /**
     * 批量更新应用用户关系
     * 1. 删除请求中不存在关系
     * 2. 添加请求中关系
     *
     * @param request 请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("batchUpdateAppUser")
    @PostMapping("batchUpdateAppUser")
    @EnableWebLogAspect
    public boolean batchUpdateAppUser(@RequestBody BatchAppUserRequest request) {
        if (request == null) throw new BadRequestException("批量更新请求为空");
        request.check();
        permissionSupport.checkAppPermission(request.appId);
        Set<AppUser> prevAppUsers = new HashSet<>(appUserService.getAppUsers(request.appId));
        Set<AppUser> currentAppUsers = new HashSet<>(Converter.convert(request));
        List<Integer> removeAppUsers = new ArrayList<>(Sets.difference(
                prevAppUsers, currentAppUsers)).stream().map(r -> r.id).collect(Collectors.toList());
        List<AppUser> addAppUsers = new ArrayList<>(Sets.difference(
                currentAppUsers, prevAppUsers));
        appUserService.batchUpdate(addAppUsers, removeAppUsers);
        return true;
    }
}
