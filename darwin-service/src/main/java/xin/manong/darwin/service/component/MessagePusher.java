package xin.manong.darwin.service.component;

/**
 * 消息队列消息生产
 *
 * @author frankcl
 * @date 2025-10-29 09:53:14
 */
public interface MessagePusher {

    /**
     * 推送消息
     *
     * @param message 消息
     * @return 推送结果
     */
    PushResult pushMessage(Message message);
}
