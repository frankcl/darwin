package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Trend;

import java.util.List;

/**
 * 数据趋势统计服务
 *
 * @author frankcl
 * @date 2025-04-23 15:52:20
 */
public interface TrendService {

    /**
     * 获取指定key和分类趋势统计
     *
     * @param key 维度key
     * @param category 分类
     * @return 统计结果
     */
    Trend get(String key, int category);

    /**
     * 获取范围内趋势列表
     *
     * @param startKey 起始key
     * @param endKey 结束key
     * @param category 分类
     * @return 趋势列表
     */
    List<Trend> between(String startKey, String endKey, int category);

    /**
     * 如果趋势数据存在则更新，否则添加
     *
     * @param trend 趋势统计
     * @return 成功返回true，否则返回false
     */
    boolean upsert(Trend trend);

    /**
     * 删除小于maxKey所有趋势数据
     *
     * @param maxKey 最大key
     */
    void delete(String maxKey);

    /**
     * 删除过期数据：创建时间小于expiredTime
     *
     * @param expiredTime 过期时间
     * @return 删除数量
     */
    int delete(long expiredTime);
}
