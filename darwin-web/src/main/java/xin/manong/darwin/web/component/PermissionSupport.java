package xin.manong.darwin.web.component;

import jakarta.annotation.Resource;
import jakarta.ws.rs.ForbiddenException;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.AppSecret;
import xin.manong.darwin.service.iface.AppSecretService;
import xin.manong.darwin.service.iface.AppUserService;
import xin.manong.darwin.web.config.WebConfig;
import xin.manong.darwin.common.request.AuthenticateRequest;
import xin.manong.hylian.client.component.UserServiceSupport;
import xin.manong.hylian.client.core.ContextManager;
import xin.manong.hylian.model.User;

/**
 * 权限服务支持
 *
 * @author frankcl
 * @date 2023-10-20 16:16:20
 */
@Component
public class PermissionSupport {

    @Resource
    private WebConfig webConfig;
    @Resource
    private AppSecretService appSecretService;
    @Resource
    private AppUserService appUserService;
    @Resource
    private UserServiceSupport userServiceSupport;

    /**
     * 当前用户是否具备操作应用权限
     * 无权限抛出异常
     *
     * @param appId 应用ID
     */
    public void checkAppPermission(int appId) {
        if (webConfig.ignoreCheckPermission) return;
        User user = ContextManager.getUser();
        if (user == null) throw new ForbiddenException("用户未登录");
        if (userServiceSupport.isAppAdmin(user)) return;
        if (!appUserService.hasAppPermission(user.id, appId)) throw new ForbiddenException("无权操作");
    }

    /**
     * 授权认证检测
     * 无权限抛出异常
     *
     * @param appId 应用ID
     * @param request 认证请求
     */
    public void checkAuthPermission(int appId, AuthenticateRequest request) {
        if (webConfig.ignoreCheckPermission) return;
        AppSecret appSecret = appSecretService.get(request.accessKey, request.secretKey);
        if (appSecret == null || appSecret.appId != appId) throw new ForbiddenException("认证失败");
        ContextManager.setValue(Constants.CONTEXT_APP_SECRET, appSecret);
    }

    /**
     * 检测是否具备管理员权限
     * 无权抛出异常
     */
    public void checkAdmin() {
        if (webConfig.ignoreCheckPermission) return;
        User user = ContextManager.getUser();
        if (user == null) throw new ForbiddenException("用户未登录");
        if (!userServiceSupport.isAppAdmin(user)) throw new ForbiddenException("无管理员权限");
    }
}
