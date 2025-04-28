package xin.manong.darwin.log.core;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.log.JSONLogger;

/**
 * 切面日志功能支持
 *
 * @author frankcl
 * @date 2025-04-28 11:14:56
 */
@Component
public class AspectLogSupport {

    @Resource(name = "jobAspectLogger")
    private JSONLogger jobAspectLogger;
    @Resource(name = "planAspectLogger")
    private JSONLogger planAspectLogger;
    @Resource(name = "urlAspectLogger")
    private JSONLogger urlAspectLogger;
    @Resource(name = "concurrencyAspectLogger")
    private JSONLogger concurrencyAspectLogger;

    /**
     * 提交并发单元切面日志
     *
     * @param context 上下文
     * @param concurrencyUnit 并发单元
     * @param applyRecordNum 申请记录数
     * @param allocateRecordNum 分配记录数
     * @param overflowRecordNum 溢出记录数
     */
    public void commitAspectLog(Context context, String concurrencyUnit,
                                int applyRecordNum, int allocateRecordNum,
                                int overflowRecordNum) {
        if (context == null || concurrencyAspectLogger == null) return;
        context.put(Constants.DARWIN_RECORD_TYPE, Constants.RECORD_TYPE_CONCURRENCY);
        context.put(Constants.CONCURRENCY_UNIT, concurrencyUnit);
        context.put(Constants.APPLY_RECORD_NUM, applyRecordNum);
        context.put(Constants.ALLOCATE_RECORD_NUM, allocateRecordNum);
        context.put(Constants.OVERFLOW_RECORD_NUM, overflowRecordNum);
        concurrencyAspectLogger.commit(context.getFeatureMap());
    }

    /**
     * 提交URL切面日志
     *
     * @param context 上下文
     * @param record URL记录
     */
    public void commitAspectLog(Context context, URLRecord record) {
        if (context == null || record == null || urlAspectLogger == null) return;
        DarwinUtil.putContext(context, record);
        urlAspectLogger.commit(context.getFeatureMap());
    }

    /**
     * 提交计划切面日志
     *
     * @param context 上下文
     * @param plan 计划
     */
    public void commitAspectLog(Context context, Plan plan) {
        if (context == null || plan == null || planAspectLogger == null) return;
        DarwinUtil.putContext(context, plan);
        planAspectLogger.commit(context.getFeatureMap());
    }

    /**
     * 提交任务切面日志
     *
     * @param context 上下文
     * @param job 任务
     */
    public void commitAspectLog(Context context, Job job) {
        if (context == null || job == null || jobAspectLogger == null) return;
        DarwinUtil.putContext(context, job);
        jobAspectLogger.commit(context.getFeatureMap());
    }
}
