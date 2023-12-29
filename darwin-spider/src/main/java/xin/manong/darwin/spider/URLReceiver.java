package xin.manong.darwin.spider;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.log.JSONLogger;

import javax.annotation.Resource;
import java.nio.charset.Charset;

/**
 * 同步URL接收器
 *
 * @author frankcl
 * @date 2023-03-24 10:28:37
 */
@Component("urlReceiver")
public class URLReceiver implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(URLReceiver.class);

    @Resource
    protected SpiderFactory spiderFactory;
    @Resource(name = "recordAspectLogger")
    protected JSONLogger aspectLogger;

    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
        Context context = new Context();
        context.put(Constants.DARWIN_STAGE, Constants.STAGE_FETCH);
        URLRecord record = null;
        Spider spider;
        try {
            if (!StringUtils.isEmpty(message.getMsgID())) context.put(Constants.DARWIN_MESSAGE_ID, message.getMsgID());
            if (!StringUtils.isEmpty(message.getKey())) context.put(Constants.DARWIN_MESSAGE_KEY, message.getKey());
            if (!StringUtils.isEmpty(message.getTopic())) context.put(Constants.DARWIN_MESSAGE_TOPIC, message.getTopic());
            context.put(Constants.DARWIN_MESSAGE_TIMESTAMP, message.getBornTimestamp());
            byte[] body = message.getBody();
            if (body == null || body.length == 0) {
                logger.error("message body is empty");
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "消息体为空");
                return Action.CommitMessage;
            }
            record = JSON.parseObject(new String(body,
                    Charset.forName("UTF-8")), URLRecord.class);
            if (record == null || !record.check()) {
                logger.error("url record is invalid");
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "URL记录非法");
                return Action.CommitMessage;
            }
            spider = spiderFactory.build(record);
        } catch (Throwable t) {
            context.put(Constants.STATUS, Constants.SUPPORT_URL_STATUSES.get(Constants.URL_STATUS_INVALID));
            context.put(Constants.DARWIN_DEBUG_MESSAGE, t.getMessage());
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(t));
            logger.error(t.getMessage(), t);
            DarwinUtil.putContext(context, record);
            if (aspectLogger != null) aspectLogger.commit(context.getFeatureMap());
            return Action.ReconsumeLater;
        }
        spider.process(record, context);
        return Action.CommitMessage;
    }
}
