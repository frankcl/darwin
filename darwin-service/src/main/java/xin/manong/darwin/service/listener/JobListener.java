package xin.manong.darwin.service.listener;

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
 * 任务监听器
 *
 * @author frankcl
 * @date 2023-07-26 15:23:02
 */
@Component
public class JobListener {

    private static final Logger logger = LoggerFactory.getLogger(JobListener.class);

    @Resource
    protected ServiceConfig config;
    @Resource
    protected JobService jobService;
    @Resource
    protected ONSProducer producer;
    @Resource(name = "jobAspectLogger")
    protected JSONLogger aspectLogger;

    /**
     * 任务结束处理工作
     *
     * @param job 任务
     */
    public void onFinish(Job job) {
        Context context = new Context();
        Job finishedJob = new Job();
        try {
            finishedJob.avoidRepeatedFetch = null;
            finishedJob.jobId = job.jobId;
            finishedJob.status = Constants.JOB_STATUS_FINISHED;
            if (!jobService.update(finishedJob)) logger.warn("update finish status failed for job[{}]", job.jobId);
            Job wholeJob = jobService.getCache(job.jobId);
            if (wholeJob != null) {
                finishedJob.planId = wholeJob.planId;
                finishedJob.appId = wholeJob.appId;
                finishedJob.name = wholeJob.name;
                finishedJob.priority = wholeJob.priority;
            }
            String jobString = JSON.toJSONString(finishedJob, SerializerFeature.DisableCircularReferenceDetect);
            Message message = new Message(config.jobTopic, String.format("%d", finishedJob.appId), finishedJob.jobId,
                    jobString.getBytes(Charset.forName("UTF-8")));
            SendResult sendResult = producer.send(message);
            if (sendResult == null || StringUtils.isEmpty(sendResult.getMessageId())) {
                context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息失败");
                logger.warn("push finish message failed for job[{}]", finishedJob.jobId);
                return;
            }
            context.put(Constants.DARWIN_MESSAGE_ID, sendResult.getMessageId());
            context.put(Constants.DARWIN_MESSAGE_KEY, finishedJob.jobId);
        } catch (Exception e) {
            context.put(Constants.DARWIN_DEBUG_MESSAGE, "推送消息异常");
            context.put(Constants.DARWIN_STRACE_TRACE, ExceptionUtils.getStackTrace(e));
            logger.error("process finished job[{}] failed", finishedJob.jobId);
            logger.error(e.getMessage(), e);
        } finally {
            DarwinUtil.putContext(context, finishedJob);
            if (aspectLogger != null) aspectLogger.commit(context.getFeatureMap());
        }
    }
}
