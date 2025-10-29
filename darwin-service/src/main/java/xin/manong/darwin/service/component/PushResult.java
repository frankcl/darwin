package xin.manong.darwin.service.component;

/**
 * 推送结果
 *
 * @author frankcl
 * @date 2025-10-29 10:06:01
 */
public class PushResult {

    public String messageId;
    public String messageKey;

    public PushResult(String messageId) {
        this.messageId = messageId;
    }

    public PushResult(String messageId, String messageKey) {
        this.messageId = messageId;
        this.messageKey = messageKey;
    }
}
