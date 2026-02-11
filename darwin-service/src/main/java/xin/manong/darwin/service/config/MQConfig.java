package xin.manong.darwin.service.config;

import lombok.Data;

/**
 * 消息队列配置
 *
 * @author frankcl
 * @date 2025-04-14 13:04:15
 */
@Data
public class MQConfig {

    public String enable = "kafka";
    public String instanceId;
    public String topicJob;
    public String topicURL;
}
