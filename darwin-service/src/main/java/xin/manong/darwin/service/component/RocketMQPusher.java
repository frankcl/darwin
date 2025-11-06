package xin.manong.darwin.service.component;

import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.weapon.base.rocketmq.RocketMQProducer;

/**
 * RocketMQ消息推送
 *
 * @author frankcl
 * @date 2025-10-29 10:04:11
 */
public class RocketMQPusher implements MessagePusher {

    private static final Logger logger = LoggerFactory.getLogger(RocketMQPusher.class);

    @Resource
    private RocketMQProducer producer;

    @Override
    public PushResult pushMessage(Message message) {
        org.apache.rocketmq.common.message.Message mqMessage =
                new org.apache.rocketmq.common.message.Message(message.topic, message.tags, message.key, message.body);
        SendResult result = producer.send(mqMessage);
        if (result == null) {
            logger.error("Push RocketMQ message failed for key:{}", message.key);
            return null;
        }
        return new PushResult(result.getMsgId(), message.key);
    }
}
