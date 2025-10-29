package xin.manong.darwin.service.component;

import com.aliyun.openservices.ons.api.SendResult;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.weapon.aliyun.ons.ONSProducer;

/**
 * ONS消息推送
 *
 * @author frankcl
 * @date 2025-10-29 10:04:11
 */
public class ONSPusher implements MessagePusher {

    private static final Logger logger = LoggerFactory.getLogger(ONSPusher.class);

    @Resource
    private ONSProducer producer;

    @Override
    public PushResult pushMessage(Message message) {
        com.aliyun.openservices.ons.api.Message onsMessage =
                new com.aliyun.openservices.ons.api.Message(message.topic, message.tags, message.key, message.body);
        SendResult result = producer.send(onsMessage);
        if (result == null) {
            logger.error("Push ons message failed for key:{}", message.key);
            return null;
        }
        return new PushResult(result.getMessageId(), message.key);
    }
}
