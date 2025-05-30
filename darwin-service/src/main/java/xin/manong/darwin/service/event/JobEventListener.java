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
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.log.core.AspectLogSupport;
import xin.manong.darwin.service.config.ServiceConfig;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.kafka.KafkaProducer;

import java.nio.charset.StandardCharsets;

/**
 * 任务事件监听器
 *
 * @author frankcl
 * @date 2023-12-09 16:12:08
 */
@Component
public class JobEventListener implements EventListener<String> {

    private static final Logger logger = LoggerFactory.getLogger(JobEventListener.class);

    @Resource
    private ServiceConfig serviceConfig;
    @Resource
    private JobService jobService;
    @Resource
    private KafkaProducer producer;
    @Resource
    private AspectLogSupport aspectLogSupport;

    @Override
    public void onComplete(String jobId, Context context) {
        if (!jobService.isComplete(jobId)) return;
        Job job = null;
        context = context == null ? new Context() : context;
        try {
            jobService.complete(jobId);
            job = jobService.get(jobId);
            pushMessage(job, context);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "处理完成任务异常");
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("Exception occurred when handling completed job:{}", jobId);
            logger.error(e.getMessage(), e);
        } finally {
            aspectLogSupport.commitAspectLog(context, job);
        }
    }

    /**
     * 推送任务完成消息
     *
     * @param job 任务
     * @param context 上下文
     */
    private void pushMessage(Job job, Context context) {
        if (!serviceConfig.dispatch) return;
        String jobString = JSON.toJSONString(job, SerializerFeature.DisableCircularReferenceDetect);
        RecordMetadata metadata = producer.send(job.jobId,
                jobString.getBytes(StandardCharsets.UTF_8), serviceConfig.mq.topicJob);
        if (metadata == null) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息失败");
            logger.warn("Push completed job message failed for id:{}", job.jobId);
            return;
        }
        context.put(Constants.DARWIN_MESSAGE_KEY, job.jobId);
    }
}
