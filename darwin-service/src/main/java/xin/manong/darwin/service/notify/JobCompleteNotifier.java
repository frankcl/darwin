package xin.manong.darwin.service.notify;

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
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.service.config.ServiceConfig;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.kafka.KafkaProducer;
import xin.manong.weapon.base.log.JSONLogger;

import java.nio.charset.StandardCharsets;

/**
 * Job完成通知
 *
 * @author frankcl
 * @date 2023-12-09 16:12:08
 */
@Component
public class JobCompleteNotifier implements CompleteNotifier<String> {

    private static final Logger logger = LoggerFactory.getLogger(JobCompleteNotifier.class);

    @Resource
    protected ServiceConfig config;
    @Resource
    protected JobService jobService;
    @Resource
    protected KafkaProducer producer;
    @Resource(name = "jobAspectLogger")
    protected JSONLogger aspectLogger;

    @Override
    public void onComplete(String jobId, Context context) {
        Job finishedJob = null;
        try {
            updateFinishStatus(jobId);
            finishedJob = getFinishedJob(jobId);
            pushMessage(finishedJob, context);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "完成任务处理异常");
            context.put(Constants.DARWIN_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("exception occurred when finishing job[{}]", jobId);
            logger.error(e.getMessage(), e);
        } finally {
            DarwinUtil.putContext(context, finishedJob);
            if (aspectLogger != null) aspectLogger.commit(context.getFeatureMap());
        }
    }

    /**
     * 推送任务完成消息
     *
     * @param job 任务
     * @param context 上下文
     */
    private void pushMessage(Job job, Context context) {
        String jobString = JSON.toJSONString(job, SerializerFeature.DisableCircularReferenceDetect);
        RecordMetadata metadata = producer.send(job.jobId,
                jobString.getBytes(StandardCharsets.UTF_8), config.topicJob);
        if (metadata == null) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息失败");
            logger.warn("push finish message failed for job[{}]", job.jobId);
            return;
        }
        context.put(Constants.DARWIN_MESSAGE_KEY, job.jobId);
    }

    /**
     * 更新任务完成状态
     *
     * @param jobId 任务ID
     */
    private void updateFinishStatus(String jobId) {
        Job job = new Job();
        job.allowRepeat = null;
        job.jobId = jobId;
        job.status = Constants.JOB_STATUS_FINISHED;
        if (!jobService.update(job)) logger.warn("update finish status failed for job[{}]", job.jobId);
    }

    /**
     * 获取完成任务信息
     *
     * @param jobId 任务ID
     * @return 完成任务信息
     */
    private Job getFinishedJob(String jobId) {
        Job finished = new Job();
        finished.jobId = jobId;
        finished.status = Constants.JOB_STATUS_FINISHED;
        Job job = jobService.getCache(jobId);
        if (job == null) return finished;
        finished.planId = job.planId;
        finished.appId = job.appId;
        finished.name = job.name;
        finished.priority = job.priority;
        return finished;
    }
}
