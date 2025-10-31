package xin.manong.darwin.spider.receiver;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.log.core.AspectLogSupport;
import xin.manong.darwin.spider.core.Router;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.kafka.KafkaRecordProcessor;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * URL接收器：支持kafka和阿里云ONS
 *
 * @author frankcl
 * @date 2023-03-24 10:28:37
 */
public class URLReceiver implements MessageListenerConcurrently, KafkaRecordProcessor {

    private static final Logger logger = LoggerFactory.getLogger(URLReceiver.class);

    private final Router router;
    private final AspectLogSupport aspectLogSupport;

    public URLReceiver(Router router, AspectLogSupport aspectLogSupport) {
        this.router = router;
        this.aspectLogSupport = aspectLogSupport;
    }

    private void handle(byte[] body, Context context) {
        URLRecord record = null;
        try {
            if (body == null || body.length == 0) {
                logger.error("Message is empty");
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "消息体为空");
                return;
            }
            record = JSON.parseObject(new String(body,
                    StandardCharsets.UTF_8), URLRecord.class);
            if (record == null || !record.check()) {
                logger.error("Record is invalid");
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "URL非法");
                return;
            }
            router.route(record, context);
        } catch (Throwable t) {
            context.put(Constants.STATUS, Constants.SUPPORT_URL_STATUSES.get(Constants.URL_STATUS_ERROR));
            context.put(Constants.DARWIN_DEBUG_MESSAGE, t.getMessage());
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(t));
            logger.error(t.getMessage(), t);
            aspectLogSupport.commitAspectLog(context, record);
            throw new IllegalStateException(t);
        }
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages,
                                                    ConsumeConcurrentlyContext consumeContext) {
        for (MessageExt message : messages) {
            Context context = new Context();
            context.put(Constants.DARWIN_STAGE, Constants.PROCESS_STAGE_FETCH);
            if (!StringUtils.isEmpty(message.getMsgId())) context.put(Constants.DARWIN_MESSAGE_ID, message.getMsgId());
            if (!StringUtils.isEmpty(message.getKeys())) context.put(Constants.DARWIN_MESSAGE_KEY, message.getKeys());
            if (!StringUtils.isEmpty(message.getTopic()))
                context.put(Constants.DARWIN_MESSAGE_TOPIC, message.getTopic());
            context.put(Constants.DARWIN_MESSAGE_TIMESTAMP, message.getBornTimestamp());
            byte[] body = message.getBody();
            try {
                handle(body, context);
            } catch (Exception e) {
                if (message.getReconsumeTimes() > 3) {
                    logger.warn("Message:{} consume exceeds {} times", message.getMsgId(), message.getReconsumeTimes());
                    continue;
                }
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    @Override
    public void process(ConsumerRecord<byte[], byte[]> consumerRecord) throws Exception {
        Context context = new Context();
        context.put(Constants.DARWIN_STAGE, Constants.PROCESS_STAGE_FETCH);
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
