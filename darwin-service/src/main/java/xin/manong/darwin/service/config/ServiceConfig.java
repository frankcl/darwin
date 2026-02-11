package xin.manong.darwin.service.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.manong.darwin.service.component.*;

/**
 * 服务层配置
 *
 * @author frankcl
 * @date 2022-08-24 13:04:15
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.service")
public class ServiceConfig {

    public boolean dispatch = false;
    public MQConfig mq;
    public OTSConfig ots;
    public OSSConfig oss;

    /**
     * 构建kafka消息推送
     *
     * @return kafka消息推送
     */
    @Bean
    @ConditionalOnProperty(name = "app.service.mq.enable", havingValue = "kafka", matchIfMissing = true)
    public KafkaPusher buildKafkaPusher() {
        return new KafkaPusher();
    }

    /**
     * 构建kafka admin
     *
     * @return kafka admin
     */
    @Bean
    @ConditionalOnProperty(name = "app.service.mq.enable", havingValue = "kafka", matchIfMissing = true)
    public KafkaAdmin buildKafkaAdmin() {
        return new KafkaAdmin();
    }

    /**
     * 构建RocketMQ消息推送
     *
     * @return RocketMQ消息推送
     */
    @Bean
    @ConditionalOnProperty(name = "app.service.mq.enable", havingValue = "rocketmq")
    public RocketMQPusher buildRocketMQPusher() {
        return new RocketMQPusher();
    }

    /**
     * 构建RocketMQ消息推送
     *
     * @return RocketMQ消息推送
     */
    @Bean
    @ConditionalOnProperty(name = "app.service.mq.enable", havingValue = "rocketmq")
    public RocketMQAdmin buildRocketMQAdmin() {
        return new RocketMQAdmin(mq.instanceId);
    }
}
