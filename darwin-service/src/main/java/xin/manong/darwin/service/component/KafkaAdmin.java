package xin.manong.darwin.service.component;

import jakarta.annotation.Resource;

/**
 * Kafka管理
 *
 * @author frankcl
 * @date 2026-02-11 17:49:59
 */
public class KafkaAdmin implements MQAdmin {

    @Resource
    private xin.manong.weapon.base.kafka.KafkaAdmin admin;

    @Override
    public long getConsumeTopicLagCount(String topic, String groupId) {
        return admin.getTopicConsumeLagCount(topic, groupId);
    }
}
