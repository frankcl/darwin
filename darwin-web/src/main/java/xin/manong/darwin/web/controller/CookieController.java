package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.common.request.SetCookieRequest;
import xin.manong.darwin.service.iface.CookieService;
import xin.manong.darwin.web.component.PermissionSupport;

import java.util.Map;

/**
 * Cookie控制器
 *
 * @author frankcl
 * @date 2026-01-29 09:45:51
 */
@RestController
@Controller
@Path("/api/cookie")
@RequestMapping("/api/cookie")
public class CookieController {

    @Resource
    private CookieService cookieService;
    @Resource
    private PermissionSupport permissionSupport;

    /**
     * 获取Cookie配置
     *
     * @return Cookie配置
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getCookieMap")
    @GetMapping("getCookieMap")
    public Map<String, String> getCookieMap() {
        return cookieService.cookieMap();
    }

    /**
     * 更新Cookie配置
     *
     * @param cookieMap Cookie配置
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("updateCookieMap")
    @PostMapping("updateCookieMap")
    public boolean updateCookieMap(@RequestBody Map<String, String> cookieMap) {
        if (cookieMap == null) throw new BadRequestException("Cookie配置为空");
        permissionSupport.checkAdmin();
        cookieService.cookieMap(cookieMap);
        return true;
    }

    /**
     * 设置Cookie
     *
     * @param request 请求
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("setCookie")
    @PostMapping("setCookie")
    public boolean setCookie(@RequestBody SetCookieRequest request) {
        if (request == null) throw new BadRequestException("请求为空");
        request.check();
        permissionSupport.checkAdmin();
        cookieService.setCookie(request.key, request.cookie);
        return true;
    }
}
