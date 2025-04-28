package xin.manong.darwin.service.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.config.ServiceConfig;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.kafka.KafkaProducer;

import java.nio.charset.StandardCharsets;

/**
 * URL抓取结束通知
 *
 * @author frankcl
 * @date 2023-12-09 16:23:17
 */
@Component
public class URLEventListener implements EventListener<String> {

    private static final Logger logger = LoggerFactory.getLogger(URLEventListener.class);

    @Resource
    private ServiceConfig config;
    @Resource
    private URLService urlService;
    @Resource
    private KafkaProducer producer;

    @Override
    public void onComplete(String key, Context context) {
        try {
            if (!config.dispatch) return;
            URLRecord record = urlService.get(key);
            pushMessage(record, context);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "分发链接异常");
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("Exception occurred when pushing completed record:{}", key);
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 推送抓取结束消息
     *
     * @param record URL记录
     * @param context 上下文对象
     */
    private void pushMessage(URLRecord record, Context context) {
        if (record == null || !record.allowDispatch || record.status != Constants.URL_STATUS_FETCH_SUCCESS) return;
        String recordString = JSON.toJSONString(record, SerializerFeature.DisableCircularReferenceDetect);
        RecordMetadata metadata = producer.send(record.key,
                recordString.getBytes(StandardCharsets.UTF_8), config.mq.topicURL);
        if (metadata == null) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息失败");
            logger.warn("Push completed record message failed for key:{}", record.key);
            return;
        }
        context.put(Constants.DARWIN_MESSAGE_KEY, record.key);
    }
}
