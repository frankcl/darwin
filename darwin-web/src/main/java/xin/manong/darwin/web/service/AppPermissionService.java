package xin.manong.darwin.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.service.iface.AppUserService;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.sso.client.core.ContextManager;

import javax.annotation.Resource;
import javax.ws.rs.ForbiddenException;

/**
 * 应用权限服务
 *
 * @author frankcl
 * @date 2023-10-20 16:16:20
 */
@Component
public class AppPermissionService {

    private static final Logger logger = LoggerFactory.getLogger(AppPermissionService.class);

    @Resource
    protected AppUserService appUserService;

    /**
     * 当前用户是否具备操作应用权限
     * 无权限抛出异常
     *
     * @param appId 应用ID
     */
    public void checkAppPermission(Integer appId) {
        if (appId == null) {
            logger.warn("app id is null");
            throw new ForbiddenException("应用ID为空");
        }
        User user = ContextManager.getUser();
        if (user == null) {
            logger.error("user is not found");
            throw new ForbiddenException("用户未登录");
        }
        if (!appUserService.hasAppPermission(user.id, appId)) {
            logger.error("not allow to operate app[{}] for user[{}]", appId, user.id);
            throw new ForbiddenException("无权访问");
        }
    }
}
