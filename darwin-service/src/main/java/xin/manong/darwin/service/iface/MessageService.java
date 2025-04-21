package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Message;

/**
 * 消息服务接口定义
 *
 * @author frankcl
 * @date 2025-03-09 20:13:54
 */
public interface MessageService {

    /**
     * 获取并删除最新一条消息
     *
     * @param sourceKey 消息源key
     * @param sourceType 消息源类型
     * @return 成功返回消息，否则返回null
     */
    Message pop(String sourceKey, int sourceType);

    /**
     * 添加消息
     *
     * @param message 消息
     * @return 添加成功返回true，否则返回false
     */
    boolean push(Message message);

    /**
     * 获取当前消息数量
     *
     * @param sourceKey 消息源key
     * @param sourceType 消息源类型
     * @return 消息数量
     */
    Long messageCount(String sourceKey, int sourceType);
}
