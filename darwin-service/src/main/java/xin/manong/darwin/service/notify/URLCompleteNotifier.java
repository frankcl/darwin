package xin.manong.darwin.service.notify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.computer.ConcurrentUnitComputer;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.queue.concurrent.ConcurrentManager;
import xin.manong.darwin.queue.multi.MultiQueue;
import xin.manong.darwin.service.config.ServiceConfig;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.weapon.aliyun.ons.ONSProducer;
import xin.manong.weapon.base.common.Context;

import javax.annotation.Resource;
import java.nio.charset.Charset;

/**
 * URL抓取结束通知
 *
 * @author frankcl
 * @date 2023-12-09 16:23:17
 */
@Component
public class URLCompleteNotifier implements CompleteNotifier<URLRecord> {

    private static final Logger logger = LoggerFactory.getLogger(URLCompleteNotifier.class);

    @Resource
    protected ServiceConfig config;
    @Resource
    protected URLService urlService;
    @Resource
    protected MultiQueue multiQueue;
    @Resource
    protected ConcurrentManager concurrentManager;
    @Resource
    protected ONSProducer producer;

    @Override
    public void onComplete(URLRecord record, Context context) {
        try {
            if (!urlService.updateContent(record)) logger.warn("update content failed for url[{}]", record.url);
            pushMessage(record, context);
            multiQueue.removeFromJobRecordMap(record);
            if (record.status == Constants.URL_STATUS_OVERFLOW) return;
            String concurrentUnit = ConcurrentUnitComputer.compute(record);
            concurrentManager.removeConnectionRecord(concurrentUnit, record.key);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "完成URL处理异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("exception occurred when finishing record[{}]", record.key);
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
        if (record == null || record.category == Constants.CONTENT_CATEGORY_LIST) return;
        String recordString = JSON.toJSONString(record, SerializerFeature.DisableCircularReferenceDetect);
        Message message = new Message(config.recordTopic, String.format("%d", record.appId), record.key,
                recordString.getBytes(Charset.forName("UTF-8")));
        SendResult sendResult = producer.send(message);
        if (sendResult == null || StringUtils.isEmpty(sendResult.getMessageId())) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息失败");
            logger.warn("push record finish message failed for key[{}]", record.key);
            return;
        }
        context.put(Constants.DARWIN_MESSAGE_ID, sendResult.getMessageId());
        context.put(Constants.DARWIN_MESSAGE_KEY, record.key);
    }
}
