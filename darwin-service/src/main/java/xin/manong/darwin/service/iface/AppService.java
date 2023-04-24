package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.App;
import xin.manong.darwin.common.model.Pager;

/**
 * 应用服务接口定义
 *
 * @author frankcl
 * @date 2023-03-21 20:13:54
 */
public interface AppService {

    /**
     * 添加应用
     *
     * @param app 应用信息
     * @return 添加成功返回true，否则返回false
     */
    Boolean add(App app);

    /**
     * 更新应用
     *
     * @param app 应用
     * @return 更新成功返回true，否则返回false
     */
    Boolean update(App app);

    /**
     * 根据ID删除应用
     *
     * @param id 应用ID
     * @return 删除成功返回true，否则返回false
     */
    Boolean delete(Long id);

    /**
     * 根据ID获取应用
     *
     * @param id 应用ID
     * @return 存在返回规则，否则返回null
     */
    App get(Long id);

    /**
     * 获取应用列表
     *
     * @param current 页码，从1开始
     * @param size 每页数量
     * @return 应用列表
     */
    Pager<App> getList(int current, int size);

    /**
     * 根据名称搜索应用列表
     *
     * @param name 应用名
     * @param current 页码，从1开始
     * @param size 每页数量
     * @return 搜索列表
     */
    Pager<App> search(String name, int current, int size);
}
