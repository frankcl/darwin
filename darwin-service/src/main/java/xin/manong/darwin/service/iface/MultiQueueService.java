package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.URLRecord;

import java.util.List;

/**
 * 多级队列服务接口定义
 *
 * @author frankcl
 * @date 2023-03-28 14:40:33
 */
public interface MultiQueueService {

    /**
     * 推送数据进入多级队列
     *
     * @param record URL记录
     * @return URL记录
     */
    URLRecord pushQueue(URLRecord record);

    /**
     * 推送数据进入多级队列
     *
     * @param records URL记录列表
     * @return URL记录列表
     */
    List<URLRecord> pushQueue(List<URLRecord> records);
}
