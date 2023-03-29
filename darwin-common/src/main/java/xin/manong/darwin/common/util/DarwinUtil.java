package xin.manong.darwin.common.util;

import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.base.common.Context;

/**
 * 爬虫通用工具
 *
 * @author frankcl
 * @date 2023-03-24 11:43:40
 */
public class DarwinUtil {

    /**
     * URL记录信息放入上下文
     *
     * @param context 上下文
     * @param record URL记录
     */
    public static void putContext(Context context, URLRecord record) {
        if (context == null || record == null) return;
        context.put(Constants.DARWIN_RECORD_TYPE, Constants.RECORD_TYPE_URL);
        if (!StringUtils.isEmpty(record.key)) context.put(Constants.KEY, record.key);
        if (!StringUtils.isEmpty(record.url)) context.put(Constants.URL, record.url);
        if (!StringUtils.isEmpty(record.parentURL)) context.put(Constants.PARENT_URL, record.parentURL);
        if (!StringUtils.isEmpty(record.fetchContentURL)) context.put(Constants.FETCH_CONTENT_URL, record.fetchContentURL);
        if (!StringUtils.isEmpty(record.jobId)) context.put(Constants.JOB_ID, record.jobId);
        if (!StringUtils.isEmpty(record.hash)) context.put(Constants.HASH, record.hash);
        if (record.fetchTime != null) context.put(Constants.FETCH_TIME, record.fetchTime);
        if (record.inQueueTime != null) context.put(Constants.IN_QUEUE_TIME, record.inQueueTime);
        if (record.outQueueTime != null) context.put(Constants.OUT_QUEUE_TIME, record.outQueueTime);
        if (record.depth != null) context.put(Constants.DEPTH, record.depth);
        if (record.timeout != null) context.put(Constants.TIMEOUT, record.timeout);
        if (record.category != null) context.put(Constants.CATEGORY, Constants.SUPPORT_CONTENT_CATEGORIES.get(record.category));
        if (record.concurrentLevel != null) context.put(Constants.CONCURRENT_LEVEL, Constants.SUPPORT_CONCURRENT_LEVELS.get(record.concurrentLevel));
    }

    /**
     * 计划信息放入上下文
     *
     * @param context 上下文
     * @param plan 计划
     */
    public static void putContext(Context context, Plan plan) {
        if (context == null || plan == null) return;
        context.put(Constants.DARWIN_RECORD_TYPE, Constants.RECORD_TYPE_PLAN);
        if (!StringUtils.isEmpty(plan.planId)) context.put(Constants.PLAN_ID, plan.planId);
        if (!StringUtils.isEmpty(plan.name)) context.put(Constants.NAME, plan.name);
        if (!StringUtils.isEmpty(plan.crontabExpression)) context.put(Constants.CRONTAB_EXPRESSION, plan.crontabExpression);
        if (plan.status != null) context.put(Constants.STATUS, Constants.SUPPORT_PLAN_STATUSES.get(plan.status));
        if (plan.appId != null) context.put(Constants.APP_ID, plan.appId);
        if (plan.priority != null) context.put(Constants.PRIORITY, plan.priority);
        if (plan.avoidRepeatedFetch != null) context.put(Constants.AVOID_REPEATED_FETCH, plan.avoidRepeatedFetch);
    }

    /**
     * 任务信息放入上下文
     *
     * @param context 上下文
     * @param job 任务
     */
    public static void putContext(Context context, Job job) {
        if (context == null || job == null) return;
        context.put(Constants.DARWIN_RECORD_TYPE, Constants.RECORD_TYPE_JOB);
        if (!StringUtils.isEmpty(job.jobId)) context.put(Constants.JOB_ID, job.jobId);
        if (!StringUtils.isEmpty(job.planId)) context.put(Constants.PLAN_ID, job.planId);
        if (!StringUtils.isEmpty(job.name)) context.put(Constants.NAME, job.name);
        if (job.status != null) context.put(Constants.STATUS, Constants.SUPPORT_JOB_STATUSES.get(job.status));
        if (job.priority != null) context.put(Constants.PRIORITY, job.priority);
    }
}
