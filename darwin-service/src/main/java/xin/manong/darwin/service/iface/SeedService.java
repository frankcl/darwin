package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.SeedRecord;
import xin.manong.darwin.service.request.SeedSearchRequest;

import java.util.List;

/**
 * Seed种子服务接口定义
 *
 * @author frankcl
 * @date 2025-04-01 20:00:56
 */
public interface SeedService {

    /**
     * 添加种子记录
     *
     * @param record 种子记录
     * @return 添加成功返回true，否则返回false
     */
    boolean add(SeedRecord record);

    /**
     * 更新种子信息
     *
     * @param record 种子记录
     * @return 更新成功返回true，否则返回false
     */
    boolean update(SeedRecord record);

    /**
     * 根据key获取种子记录
     *
     * @param key 唯一key
     * @return 种子记录，无记录返回null
     */
    SeedRecord get(String key);

    /**
     * 根据key删除种子记录
     *
     * @param key 唯一key
     * @return 成功返回true，否则返回false
     */
    boolean delete(String key);

    /**
     * 获取计划相关种子列表
     *
     * @param planId 计划ID
     * @return 种子列表
     */
    List<SeedRecord> getList(String planId);

    /**
     * 搜索种子URL列表
     *
     * @param searchRequest 搜索请求
     * @return 搜索列表
     */
    Pager<SeedRecord> search(SeedSearchRequest searchRequest);
}
