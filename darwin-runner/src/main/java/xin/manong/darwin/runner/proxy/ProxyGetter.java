package xin.manong.darwin.runner.proxy;

import xin.manong.darwin.common.model.Proxy;

import java.util.List;

/**
 * 代理获取
 *
 * @author frankcl
 * @date 2026-02-06 14:33:47
 */
public interface ProxyGetter {

    /**
     * 初始化
     *
     * @param config 配置
     * @return 成功返回true，否则返回false
     */
    default boolean init(ProxyGetConfig config) {
        return true;
    }

    /**
     * 销毁
     */
    default void destroy() {}

    /**
     * 批量获取代理
     *
     * @return 代理列表
     */
    List<Proxy> batchGet();
}
