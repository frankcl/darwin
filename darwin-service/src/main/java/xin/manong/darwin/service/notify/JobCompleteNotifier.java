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
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.darwin.service.config.ServiceConfig;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.weapon.aliyun.ons.ONSProducer;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.log.JSONLogger;

import javax.annotation.Resource;
import java.nio.charset.Charset;

/**
 * Job完成通知
 *
 * @author frankcl
 * @date 2023-12-09 16:12:08
 */
@Component
public class JobCompleteNotifier implements CompleteNotifier<Job> {

    private static final Logger logger = LoggerFactory.getLogger(JobCompleteNotifier.class);

    @Resource
    protected ServiceConfig config;
    @Resource
    protected JobService jobService;
    @Resource
    protected ONSProducer producer;
    @Resource(name = "jobAspectLogger")
    protected JSONLogger aspectLogger;

    @Override
    public void onComplete(Job job, Context context) {
        Job finishJob = null;
        try {
            if (job == null || StringUtils.isEmpty(job.jobId)) {
                logger.warn("finished job is null");
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "任务信息异常");
                return;
            }
            finishJob = finishGetJob(job.jobId);
            pushMessage(finishJob, context);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "完成任务处理异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("exception occurred when finishing job[{}]", job.jobId);
            logger.error(e.getMessage(), e);
        } finally {
            DarwinUtil.putContext(context, finishJob);
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
        Message message = new Message(config.jobTopic, String.format("%d", job.appId), job.jobId,
                jobString.getBytes(Charset.forName("UTF-8")));
        SendResult sendResult = producer.send(message);
        if (sendResult == null || StringUtils.isEmpty(sendResult.getMessageId())) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息失败");
            logger.warn("push finish message failed for job[{}]", job.jobId);
            return;
        }
        context.put(Constants.DARWIN_MESSAGE_ID, sendResult.getMessageId());
        context.put(Constants.DARWIN_MESSAGE_KEY, job.jobId);
    }

    /**
     * 更新任务完成状态并获取分发任务信息
     *
     * @param jobId 任务ID
     * @return 分发任务
     */
    private Job finishGetJob(String jobId) {
        Job job = new Job();
        job.avoidRepeatedFetch = null;
        job.jobId = jobId;
        job.status = Constants.JOB_STATUS_FINISHED;
        if (!jobService.update(job)) logger.warn("update finish status failed for job[{}]", job.jobId);
        Job tempJob = jobService.getCache(job.jobId);
        if (tempJob == null) return job;
        job.planId = tempJob.planId;
        job.appId = tempJob.appId;
        job.name = tempJob.name;
        job.priority = tempJob.priority;
        return job;
    }
}
