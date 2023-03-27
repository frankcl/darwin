package xin.manong.darwin.spider.sync;

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
import xin.manong.darwin.common.parser.LinkURL;
import xin.manong.darwin.common.parser.ParseResponse;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.spider.function.FetchResponse;
import xin.manong.darwin.spider.function.Spider;
import xin.manong.darwin.spider.function.SpiderFactory;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.log.JSONLogger;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 同步URL接收器
 *
 * @author frankcl
 * @date 2023-03-24 10:28:37
 */
@Component("syncURLReceiver")
public class URLReceiver implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(URLReceiver.class);

    @Resource
    protected SpiderFactory spiderFactory;
    @Resource(name = "spiderAspectLogger")
    protected JSONLogger aspectLogger;

    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
        Context context = new Context();
        URLRecord record = null;
        try {
            if (!StringUtils.isEmpty(message.getMsgID())) context.put(Constants.DARWIN_MESSAGE_ID, message.getMsgID());
            if (!StringUtils.isEmpty(message.getKey())) context.put(Constants.DARWIN_MESSAGE_KEY, message.getKey());
            if (!StringUtils.isEmpty(message.getTopic()))
                context.put(Constants.DARWIN_MESSAGE_TOPIC, message.getTopic());
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
            Spider spider = spiderFactory.build(record);
            FetchResponse fetchResponse = spider.fetch(record);
            if (!fetchResponse.status) return Action.CommitMessage;
            if (spider.supportParse()) {
                ParseResponse parseResponse = spider.parse(record, fetchResponse.html);
                if (!parseResponse.status) return Action.CommitMessage;
                handleFollowLinks(record, parseResponse);
            }
            return Action.CommitMessage;
        } catch (Throwable t) {
            context.put(Constants.STATUS, Constants.SUPPORT_URL_STATUSES.get(Constants.URL_STATUS_INVALID));
            context.put(Constants.DARWIN_DEBUG_MESSAGE, t.getMessage());
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(t));
            if (record != null) DarwinUtil.putContext(context, record);
            if (aspectLogger != null) aspectLogger.commit(context.getFeatureMap());
            logger.error(t.getMessage(), t);
            return Action.ReconsumeLater;
        }
    }

    private void handleFollowLinks(URLRecord parentRecord, ParseResponse parseResponse) {
        List<LinkURL> followLinks = parseResponse.followLinks;
        for (LinkURL followLink : followLinks) {
            URLRecord record = new URLRecord(followLink.url);
            record.priority = Constants.PRIORITY_NORMAL;
            record.concurrentLevel = Constants.CONCURRENT_LEVEL_DOMAIN;
            record.parentURL = parentRecord.url;
            record.jobId = parentRecord.jobId;
            record.depth = parentRecord.depth + 1;
            if (followLink.priority != null) record.priority = followLink.priority;
            if (followLink.category != null) record.category = followLink.category;
            if (followLink.fetchMethod != null) record.fetchMethod = followLink.fetchMethod;
            if (followLink.concurrentLevel != null) record.concurrentLevel = followLink.concurrentLevel;
            if (followLink.timeout != null) record.timeout = followLink.timeout;
            if (followLink.headers != null) record.headers = followLink.headers;
            if (followLink.userDefinedMap != null) record.userDefinedMap = followLink.userDefinedMap;
            if (!record.check()) {
                logger.warn("follow link[{}] is invalid", followLink.url);
                continue;
            }
        }
    }
}
