package xin.manong.darwin.service.component;

import jakarta.annotation.Resource;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.weapon.base.kafka.KafkaProducer;

/**
 * kafka消息推送
 *
 * @author frankcl
 * @date 2025-10-29 10:01:23
 */
public class KafkaPusher implements MessagePusher {

    private static final Logger logger = LoggerFactory.getLogger(KafkaPusher.class);

    @Resource
    private KafkaProducer producer;

    @Override
    public PushResult pushMessage(Message message) {
        RecordMetadata recordMetadata = producer.send(message.key, message.body, message.topic);
        if (recordMetadata == null) {
            logger.error("Push kafka message failed for key:{}", message.key);
            return null;
        }
        return new PushResult(null, message.key);
    }
}
