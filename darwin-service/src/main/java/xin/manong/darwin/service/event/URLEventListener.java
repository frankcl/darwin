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
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.service.config.ServiceConfig;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.kafka.KafkaProducer;
import xin.manong.weapon.base.log.JSONLogger;

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
    protected ServiceConfig config;
    @Resource
    protected URLService urlService;
    @Resource
    protected KafkaProducer producer;
    @Resource(name = "urlAspectLogger")
    protected JSONLogger aspectLogger;

    @Override
    public void onComplete(String key, Context context) {
        URLRecord record = urlService.get(key);
        try {
            pushMessage(record, context);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "处理完成链接异常");
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("Exception occurred when handling completed record:{}", key);
            logger.error(e.getMessage(), e);
        } finally {
            DarwinUtil.putContext(context, record);
            if (aspectLogger != null) aspectLogger.commit(context.getFeatureMap());
        }
    }

    /**
     * 推送抓取结束消息
     *
     * @param record URL记录
     * @param context 上下文对象
     */
    private void pushMessage(URLRecord record, Context context) {
        if (record == null || record.category == Constants.CONTENT_CATEGORY_LIST) return;
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
