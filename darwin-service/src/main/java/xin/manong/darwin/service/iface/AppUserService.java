package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.AppUser;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.request.AppUserSearchRequest;

/**
 * 应用用户关系服务接口定义
 *
 * @author frankcl
 * @date 2023-10-20 20:13:54
 */
public interface AppUserService {

    /**
     * 添加应用用户关系
     *
     * @param appUser 应用用户关系
     * @return 添加成功返回true，否则返回false
     */
    Boolean add(AppUser appUser);

    /**
     * 根据ID删除应用用户关系
     *
     * @param id 应用用户关系ID
     * @return 删除成功返回true，否则返回false
     */
    Boolean delete(Long id);

    /**
     * 根据ID获取应用用户关系
     *
     * @param id 应用用户关系ID
     * @return 成功返回应用用户关系，否则返回null
     */
    AppUser get(Long id);

    /**
     * 用户是否有应用权限
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 如果用户具备应用权限返回true，否则返回false
     */
    Boolean hasAppPermission(String userId, Long appId);

    /**
     * 搜索应用用户关系
     *
     * @param searchRequest 搜索请求
     * @return 搜索列表
     */
    Pager<AppUser> search(AppUserSearchRequest searchRequest);
}
