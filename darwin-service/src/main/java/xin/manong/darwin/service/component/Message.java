package xin.manong.darwin.service.component;

/**
 * 消息定义
 *
 * @author frankcl
 * @date 2025-10-29 09:55:35
 */
public class Message {

    public String topic;
    public String tags;
    public String key;
    public byte[] body;

    public Message(String topic, String tags, String key, byte[] body) {
        this(topic, key, body);
        this.tags = tags;
    }

    public Message(String topic, String key, byte[] body) {
        this.topic = topic;
        this.key = key;
        this.body = body;
    }
}
