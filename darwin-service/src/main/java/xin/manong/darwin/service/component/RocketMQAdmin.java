package xin.manong.darwin.service.component;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;

/**
 * RocketMQ管理
 *
 * @author frankcl
 * @date 2026-02-11 17:47:19
 */
public class RocketMQAdmin implements MQAdmin{

    private final String instanceId;
    @Resource
    private xin.manong.weapon.aliyun.rocketmq.RocketMQAdmin admin;

    public RocketMQAdmin(String instanceId) {
        if (StringUtils.isEmpty(instanceId)) throw new IllegalArgumentException("Instance id is null");
        this.instanceId = instanceId;
    }

    @Override
    public long getConsumeTopicLagCount(String topic, String groupId) {
        return admin.getTopicConsumeLagCount(instanceId, topic, groupId);
    }

}
