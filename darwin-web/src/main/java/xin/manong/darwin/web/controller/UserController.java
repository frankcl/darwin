package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.darwin.service.iface.OSSService;
import xin.manong.hylian.client.component.UserServiceSupport;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.model.User;
import xin.manong.weapon.spring.boot.aspect.EnableWebLogAspect;

import java.util.List;

/**
 * 用户控制器
 *
 * @author frankcl
 * @date 2025-03-27 10:29:52
 */
@RestController
@Controller
@Path("/api/user")
@RequestMapping("/api/user")
public class UserController {

    @Resource
    protected OSSService ossService;
    @Resource
    protected UserServiceSupport userServiceSupport;

    /**
     * 获取当前用户
     *
     * @return 当前用户信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("currentUser")
    @GetMapping("currentUser")
    @EnableWebLogAspect
    public User currentUser() {
        User user = ContextManager.getUser();
        if (user != null && StringUtils.isNotEmpty(user.avatar)) {
            user.avatar = ossService.signURL(user.avatar);
        }
        return user;
    }

    /**
     * 获取所有用户列表
     *
     * @return 用户列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("allUsers")
    @GetMapping("allUsers")
    @EnableWebLogAspect
    public List<User> allUsers() {
        return userServiceSupport.getAllUsers();
    }

}
