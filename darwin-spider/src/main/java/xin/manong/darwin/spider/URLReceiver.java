package xin.manong.darwin.spider;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.kafka.KafkaRecordProcessor;
import xin.manong.weapon.base.log.JSONLogger;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * URL接收器
 *
 * @author frankcl
 * @date 2023-03-24 10:28:37
 */
public class URLReceiver implements MessageListener, KafkaRecordProcessor {

    private static final Logger logger = LoggerFactory.getLogger(URLReceiver.class);

    protected final Set<String> supportedCategory;
    protected final SpiderFactory spiderFactory;
    protected final JSONLogger aspectLogger;

    public URLReceiver(SpiderFactory spiderFactory, JSONLogger aspectLogger, Set<String> supportedCategory) {
        this.spiderFactory = spiderFactory;
        this.aspectLogger = aspectLogger;
        this.supportedCategory = supportedCategory;
    }

    private void handle(byte[] body, Context context) {
        URLRecord record = null;
        Spider spider;
        try {
            if (body == null || body.length == 0) {
                logger.error("message body is empty");
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "消息体为空");
                return;
            }
            record = JSON.parseObject(new String(body,
                    StandardCharsets.UTF_8), URLRecord.class);
            if (record == null || !record.check()) {
                logger.error("url record is invalid");
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "URL记录非法");
                return;
            }
            spider = spiderFactory.build(record);
        } catch (Throwable t) {
            context.put(Constants.STATUS, Constants.SUPPORT_URL_STATUSES.get(Constants.URL_STATUS_INVALID));
            context.put(Constants.DARWIN_DEBUG_MESSAGE, t.getMessage());
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(t));
            logger.error(t.getMessage(), t);
            DarwinUtil.putContext(context, record);
            if (aspectLogger != null) aspectLogger.commit(context.getFeatureMap());
            throw new RuntimeException(t);
        }
        spider.process(record, context);
    }

    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
        Context context = new Context();
        context.put(Constants.DARWIN_STAGE, Constants.STAGE_FETCH);
        if (!StringUtils.isEmpty(message.getMsgID())) context.put(Constants.DARWIN_MESSAGE_ID, message.getMsgID());
        if (!StringUtils.isEmpty(message.getKey())) context.put(Constants.DARWIN_MESSAGE_KEY, message.getKey());
        if (!StringUtils.isEmpty(message.getTopic())) context.put(Constants.DARWIN_MESSAGE_TOPIC, message.getTopic());
        context.put(Constants.DARWIN_MESSAGE_TIMESTAMP, message.getBornTimestamp());
        byte[] body = message.getBody();
        try {
            handle(body, context);
            return Action.CommitMessage;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }

    @Override
    public void process(ConsumerRecord<byte[], byte[]> consumerRecord) throws Exception {
        Headers headers = consumerRecord.headers();
        if (headers == null) return;
        Header header = headers.lastHeader(Constants.CATEGORY);
        if (header == null || supportedCategory == null ||
                !supportedCategory.contains(new String(header.value(), StandardCharsets.UTF_8))) return;
        Context context = new Context();
        context.put(Constants.DARWIN_STAGE, Constants.STAGE_FETCH);
        if (consumerRecord.key() != null) {
            context.put(Constants.DARWIN_MESSAGE_KEY, new String(consumerRecord.key(), StandardCharsets.UTF_8));
        }
        if (!StringUtils.isEmpty(consumerRecord.topic())) {
            context.put(Constants.DARWIN_MESSAGE_TOPIC, consumerRecord.topic());
        }
        context.put(Constants.DARWIN_MESSAGE_TIMESTAMP, consumerRecord.timestamp());
        handle(consumerRecord.value(), context);
    }
}
