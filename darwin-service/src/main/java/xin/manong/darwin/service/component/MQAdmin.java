package xin.manong.darwin.service.component;

/**
 * 消息队列管理
 *
 * @author frankcl
 * @date 2026-02-11 17:45:59
 */
public interface MQAdmin {

    /**
     * 获取消息队列数量
     *
     * @param topic 主题
     * @param groupId 分组ID
     * @return 成功返回消息堆积数量，否则返回-1
     */
    long getConsumeTopicLagCount(String topic, String groupId);
}
