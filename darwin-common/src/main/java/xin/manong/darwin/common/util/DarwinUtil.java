package xin.manong.darwin.common.util;

import org.apache.commons.lang3.StringUtils;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.util.CommonUtil;
import xin.manong.weapon.base.util.DomainUtil;

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
        if (!StringUtils.isEmpty(record.parentKey)) context.put(Constants.PARENT_KEY, record.parentKey);
        if (!StringUtils.isEmpty(record.parentURL)) context.put(Constants.PARENT_URL, record.parentURL);
        if (!StringUtils.isEmpty(record.redirectURL)) context.put(Constants.REDIRECT_URL, record.redirectURL);
        if (!StringUtils.isEmpty(record.fetchContentURL)) context.put(Constants.FETCH_CONTENT_URL, record.fetchContentURL);
        if (!StringUtils.isEmpty(record.jobId)) context.put(Constants.JOB_ID, record.jobId);
        if (!StringUtils.isEmpty(record.hash)) context.put(Constants.HASH, record.hash);
        if (!StringUtils.isEmpty(record.charset)) context.put(Constants.CHARSET, record.charset);
        if (!StringUtils.isEmpty(record.htmlCharset)) context.put(Constants.HTML_CHARSET, record.htmlCharset);
        if (record.mediaType != null) context.put(Constants.MEDIA_TYPE, record.mediaType.toString());
        if (record.appId != null) context.put(Constants.APP_ID, record.appId);
        if (record.status != null) context.put(Constants.STATUS, Constants.SUPPORT_URL_STATUSES.get(record.status));
        if (record.httpCode != null) context.put(Constants.HTTP_CODE, record.httpCode);
        if (record.fetchMethod != null) context.put(Constants.FETCH_METHOD, Constants.SUPPORT_FETCH_METHODS.get(record.fetchMethod));
        if (record.fetchTime != null) context.put(Constants.FETCH_TIME, record.fetchTime);
        if (record.pushTime != null) context.put(Constants.PUSH_TIME, record.pushTime);
        if (record.popTime != null) context.put(Constants.POP_TIME, record.popTime);
        if (record.depth != null) context.put(Constants.DEPTH, record.depth);
        if (record.timeout != null) context.put(Constants.TIMEOUT, record.timeout);
        if (record.priority != null) context.put(Constants.PRIORITY, record.priority);
        if (record.contentType != null) context.put(Constants.CONTENT_TYPE, Constants.SUPPORT_CONTENT_TYPES.get(record.contentType));
        if (record.allowDispatch != null) context.put(Constants.ALLOW_DISPATCH, record.allowDispatch);
        if (record.fetched != null) context.put(Constants.FETCHED, record.fetched);
        if (record.concurrencyLevel != null) context.put(Constants.CONCURRENCY_LEVEL, Constants.SUPPORT_CONCURRENCY_LEVELS.get(record.concurrencyLevel));
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
        if (plan.status != null) context.put(Constants.STATUS, plan.status);
        if (plan.category != null) context.put(Constants.CATEGORY, Constants.SUPPORT_PLAN_CATEGORIES.get(plan.category));
        if (plan.appId != null) context.put(Constants.APP_ID, plan.appId);
        if (plan.maxDepth != null) context.put(Constants.MAX_DEPTH, plan.maxDepth);
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
        if (job.appId != null) context.put(Constants.APP_ID, job.appId);
        if (job.status != null) context.put(Constants.STATUS, job.status);
    }

    /**
     * 判断URL的host是否相同
     *
     * @param record1 URL数据
     * @param record2 URL数据
     * @return 相同返回true，否则返回false
     */
    public static boolean isSameHost(URLRecord record1, URLRecord record2) {
        if (record1 == null || record2 == null) return false;
        return isSameHost(record1.url, record2.url);
    }

    /**
     * 判断URL的host是否相同
     *
     * @param url1 URL数据
     * @param url2 URL数据
     * @return 相同返回true，否则返回false
     */
    public static boolean isSameHost(String url1, String url2) {
        if (StringUtils.isEmpty(url1) || StringUtils.isEmpty(url2)) return false;
        String host1 = CommonUtil.getHost(url1);
        String host2 = CommonUtil.getHost(url2);
        return host1.equals(host2);
    }

    /**
     * 判断URL的domain是否相同
     *
     * @param record1 URL数据
     * @param record2 URL数据
     * @return 相同返回true，否则返回false
     */
    public static boolean isSameDomain(URLRecord record1, URLRecord record2) {
        if (record1 == null || record2 == null) return false;
        return isSameDomain(record1.url, record2.url);
    }

    /**
     * 判断URL的domain是否相同
     *
     * @param url1 URL数据
     * @param url2 URL数据
     * @return 相同返回true，否则返回false
     */
    public static boolean isSameDomain(String url1, String url2) {
        if (StringUtils.isEmpty(url1) || StringUtils.isEmpty(url2)) return false;
        String host1 = CommonUtil.getHost(url1);
        String host2 = CommonUtil.getHost(url2);
        if (StringUtils.isEmpty(host1) || StringUtils.isEmpty(host2)) return false;
        String domain1 = DomainUtil.getDomain(host1);
        String domain2 = DomainUtil.getDomain(host2);
        return domain1.equals(domain2);
    }
}
