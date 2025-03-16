package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Executor;

import java.util.List;

/**
 * 执行器服务接口定义
 *
 * @author frankcl
 * @date 2025-03-09 20:13:54
 */
public interface ExecutorService {

    /**
     * 根据名称获取执行器
     *
     * @param name 名称
     * @return 成功返回执行器，否则返回null
     */
    Executor get(String name);

    /**
     * 添加执行器
     *
     * @param executor 执行器
     * @return 添加成功返回true，否则返回false
     */
    boolean add(Executor executor);

    /**
     * 更新执行器
     *
     * @param executor 执行器
     * @return 更新成功返回true，否则返回false
     */
    boolean update(Executor executor);

    /**
     * 根据指定名称执行器
     *
     * @param name 执行器名称
     * @param executor 更新信息
     * @return 更新成功返回true，否则返回false
     */
    boolean updateByName(String name, Executor executor);

    /**
     * 获取执行器列表
     *
     * @return 执行器列表
     */
    List<Executor> getList();
}
