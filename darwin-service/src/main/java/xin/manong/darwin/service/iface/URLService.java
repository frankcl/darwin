package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.FetchRecord;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.request.URLSearchRequest;

/**
 * URL服务接口定义
 *
 * @author frankcl
 * @date 2023-03-20 20:00:56
 */
public interface URLService {

    /**
     * 添加URL记录
     *
     * @param record url记录
     * @return 添加成功返回true，否则返回false
     */
    Boolean add(URLRecord record);

    /**
     * 更新抓取结果
     *
     * @param fetchRecord 抓取结果
     * @return 更新成功返回true，否则返回false
     */
    Boolean updateWithFetchRecord(FetchRecord fetchRecord);

    /**
     * 更新入队出队时间
     *
     * @param record URL记录
     * @return 更新成功返回true，否则返回false
     */
    Boolean updateQueueTime(URLRecord record);

    /**
     * 更新URL状态
     *
     * @param key URL key
     * @param status 状态
     * @return 更新成功返回true，否则返回false
     */
    Boolean updateStatus(String key, int status);

    /**
     * 根据key获取URL记录
     *
     * @param key 唯一key
     * @return URL记录，无记录返回null
     */
    URLRecord get(String key);

    /**
     * 根据key删除URL记录
     *
     * @param key 唯一key
     * @return 成功返回true，否则返回false
     */
    Boolean delete(String key);

    /**
     * 搜索URL列表
     *
     * @param searchRequest 搜索请求
     * @param current 页码，从1开始
     * @param size 每页数量
     * @return 搜索列表
     */
    Pager<URLRecord> search(URLSearchRequest searchRequest, int current, int size);
}
