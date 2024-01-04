package xin.manong.darwin.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xin.manong.darwin.service.iface.AppUserService;
import xin.manong.darwin.web.config.WebConfig;
import xin.manong.security.keeper.model.Role;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.sso.client.component.UserRolePermissionService;
import xin.manong.security.keeper.sso.client.core.ContextManager;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ForbiddenException;
import java.util.List;

/**
 * 应用权限服务
 *
 * @author frankcl
 * @date 2023-10-20 16:16:20
 */
@Component
public class AppPermissionService {

    private static final Logger logger = LoggerFactory.getLogger(AppPermissionService.class);

    private static final String SUPER_ADMIN = "超级管理员";

    @Resource
    protected WebConfig webConfig;
    @Resource
    protected AppUserService appUserService;
    @Autowired(required = false)
    protected UserRolePermissionService userRolePermissionService;

    /**
     * 当前用户是否具备操作应用权限
     * 无权限抛出异常
     *
     * @param appId 应用ID
     */
    public void checkAppPermission(Integer appId) {
        if (webConfig.ignoreCheckPermission) return;
        if (appId == null) {
            logger.warn("app id is null");
            throw new ForbiddenException("应用ID为空");
        }
        User user = ContextManager.getUser();
        if (user == null) {
            logger.error("user is not found");
            throw new ForbiddenException("用户未登录");
        }
        if (isAdmin(user)) return;
        if (!appUserService.hasAppPermission(user.id, appId)) {
            logger.error("not allow to operate app[{}] for user[{}]", appId, user.id);
            throw new ForbiddenException("无权访问");
        }
    }

    /**
     * 用户是否为超级管理员
     *
     * @param user 用户信息
     * @return 超级管理员返回true，否则返回false
     */
    private boolean isAdmin(User user) {
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.
                currentRequestAttributes()).getRequest();
        List<Role> roles = httpRequest == null ? userRolePermissionService.getUserRoles(user) :
                userRolePermissionService.getUserRoles(user, httpRequest);
        if (roles == null || roles.isEmpty()) return false;
        for (Role role : roles) {
            if (role.name.equals(SUPER_ADMIN)) return true;
        }
        return false;
    }
}
