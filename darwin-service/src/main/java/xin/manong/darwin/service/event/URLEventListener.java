package xin.manong.darwin.service.event;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.component.PushResult;
import xin.manong.darwin.service.config.ServiceConfig;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.weapon.base.common.Context;

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
        if (record == null || !record.allowDispatch) return;
        if ((record.allowDispatchFail == null || !record.allowDispatchFail) &&
                record.status != Constants.URL_STATUS_FETCH_SUCCESS) return;
        PushResult pushResult = urlService.dispatch(record);
        if (pushResult == null) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息失败");
            return;
        }
        if (StringUtils.isNotEmpty(pushResult.messageKey)) context.put(Constants.DARWIN_MESSAGE_KEY, pushResult.messageKey);
        if (StringUtils.isNotEmpty(pushResult.messageId)) context.put(Constants.DARWIN_MESSAGE_ID, pushResult.messageId);
    }
}
