package xin.manong.darwin.spider.receiver;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;

/**
 * URL接收器
 *
 * @author frankcl
 * @date 2023-03-24 10:28:37
 */
public class URLReceiver implements MessageListener {
    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
        return null;
    }
}
