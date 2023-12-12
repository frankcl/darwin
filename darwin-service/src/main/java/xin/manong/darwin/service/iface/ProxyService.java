package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.Proxy;
import xin.manong.darwin.service.request.ProxySearchRequest;

/**
 * 代理服务接口定义
 *
 * @author frankcl
 * @date 2023-12-11 11:41:37
 */
public interface ProxyService {

    /**
     * 添加代理
     *
     * @param proxy 代理信息
     * @return 成功返回true，否则返回false
     */
    Boolean add(Proxy proxy);

    /**
     * 更新代理
     *
     * @param proxy 代理信息
     * @return 成功返回true，否则返回false
     */
    Boolean update(Proxy proxy);

    /**
     * 删除代理
     *
     * @param id 代理ID
     * @return 成功返回true，否则返回false
     */
    Boolean delete(int id);

    /**
     * 刷新代理
     * 代理分类：长效代理1；短效代理2
     *
     * @param category 代理分类
     * @return 成功返回true，否则返回false
     */
    Boolean refresh(int category);

    /**
     * 随机获取代理
     * 代理分类：长效代理1；短效代理2
     *
     * @param category 代理分类
     * @return 成功返回代理，否则返回null
     */
    Proxy randomGet(int category);

    Proxy get(int id);

    /**
     * 搜素代理
     *
     * @param searchRequest 搜索请求
     * @return 代理分页列表
     */
    Pager<Proxy> search(ProxySearchRequest searchRequest);
}
