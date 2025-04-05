package xin.manong.darwin.web.controller;

import jakarta.annotation.Resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.hylian.client.component.UserServiceSupport;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.model.User;
import xin.manong.weapon.aliyun.oss.OSSClient;
import xin.manong.weapon.aliyun.oss.OSSMeta;
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

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource
    protected OSSClient ossClient;
    @Resource
    protected UserServiceSupport userServiceSupport;

    /**
     * 获取当前用户
     *
     * @return 当前用户信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getCurrentUser")
    @GetMapping("getCurrentUser")
    @EnableWebLogAspect
    public User getCurrentUser() {
        User user = ContextManager.getUser();
        if (user != null && StringUtils.isNotEmpty(user.avatar)) {
            OSSMeta ossMeta = OSSClient.parseURL(user.avatar);
            if (ossMeta == null) {
                logger.warn("avatar[{}] is invalid", user.avatar);
                return user;
            }
            user.avatar = ossClient.sign(ossMeta.bucket, ossMeta.key);
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
    @Path("getAllUsers")
    @GetMapping("getAllUsers")
    @EnableWebLogAspect
    public List<User> getAllUsers() {
        return userServiceSupport.getAllUsers();
    }

}
