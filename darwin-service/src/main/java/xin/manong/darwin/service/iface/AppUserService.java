package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.AppUser;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.request.AppUserSearchRequest;

import java.util.List;

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
    boolean add(AppUser appUser);

    /**
     * 根据ID删除应用用户关系
     *
     * @param id 应用用户关系ID
     * @return 删除成功返回true，否则返回false
     */
    boolean delete(Integer id);

    /**
     * 删除应用关联所有关系
     *
     * @param appId 应用ID
     * @return 成功返回true，否则返回false
     */
    boolean deleteByApp(Integer appId);

    /**
     * 根据ID获取应用用户关系
     *
     * @param id 应用用户关系ID
     * @return 成功返回应用用户关系，否则返回null
     */
    AppUser get(Integer id);

    /**
     * 用户是否有应用权限
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 如果用户具备应用权限返回true，否则返回false
     */
    boolean hasAppPermission(String userId, Integer appId);

    /**
     * 获取应用相关应用用户关系列表
     *
     * @param appId 应用ID
     * @return 应用用户列表
     */
    List<AppUser> getAppUsers(Integer appId);

    /**
     * 获取用户相关应用用户关系列表
     *
     * @param userId 用户ID
     * @return 应用用户列表
     */
    List<AppUser> getAppUsers(String userId);

    /**
     * 批量更新
     *
     * @param addAppUsers 添加应用用户关系
     * @param removeAppUsers 移除应用用户关系
     */
    void batchUpdate(List<AppUser> addAppUsers, List<Integer> removeAppUsers);

    /**
     * 搜索应用用户关系
     *
     * @param searchRequest 搜索请求
     * @return 搜索列表
     */
    Pager<AppUser> search(AppUserSearchRequest searchRequest);
}
