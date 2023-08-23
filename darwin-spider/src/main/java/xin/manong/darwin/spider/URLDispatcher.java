package xin.manong.darwin.spider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.aliyun.ons.ONSProducer;
import xin.manong.weapon.base.common.Context;

import javax.annotation.Resource;
import java.nio.charset.Charset;

/**
 * URL抓取结果分发
 *
 * @author frankcl
 * @date 2023-08-23 14:27:25
 */
public class URLDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(URLDispatcher.class);

    private boolean enable;
    private String topic;
    @Resource
    protected ONSProducer producer;

    public URLDispatcher(boolean enable, String topic) {
        this.enable = enable;
        this.topic = topic;
    }

    /**
     * 分发抓取结果：列表页不分发
     *
     * @param record URL记录
     * @param context 上下文
     */
    public void dispatch(URLRecord record, Context context) {
        try {
            if (!enable) {
                logger.warn("URL dispatcher is not enable");
                return;
            }
            /**
             * 列表页不分发
             */
            if (record == null || record.category == Constants.CONTENT_CATEGORY_LIST) return;
            String recordString = JSON.toJSONString(record, SerializerFeature.DisableCircularReferenceDetect);
            Message message = new Message(topic, String.format("%d", record.appId), record.key,
                    recordString.getBytes(Charset.forName("UTF-8")));
            SendResult sendResult = producer.send(message);
            if (sendResult == null || StringUtils.isEmpty(sendResult.getMessageId())) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息失败");
                logger.warn("push record finish message failed for key[{}]", record.key);
                return;
            }
            context.put(Constants.DARWIN_MESSAGE_ID, sendResult.getMessageId());
            context.put(Constants.DARWIN_MESSAGE_KEY, record.key);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("push record finish message failed for key[{}]", record.key);
            logger.error(e.getMessage(), e);
        }
    }
}
