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
    Boolean refreshCache(int category);

    /**
     * 删除过期代理
     *
     * @return 删除代理数量
     */
    int deleteExpired();

    /**
     * 随机获取代理
     * 代理分类：长效代理1；短效代理2
     *
     * @param category 代理分类
     * @return 成功返回代理，否则返回null
     */
    Proxy randomGet(int category);

    /**
     * 根据ID获取代理
     *
     * @param id 代理ID
     * @return 成功返回代理，否则返回null
     */
    Proxy get(int id);

    /**
     * 根据地址和端口获取代理
     *
     * @param address 地址
     * @param port 端口
     * @return 成功返回代理，否则返回null
     */
    Proxy get(String address, int port);

    /**
     * 搜素代理
     *
     * @param searchRequest 搜索请求
     * @return 代理分页列表
     */
    Pager<Proxy> search(ProxySearchRequest searchRequest);
}
