package xin.manong.darwin.runner.core;

import jakarta.annotation.Resource;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.executor.ExecuteRunner;
import xin.manong.weapon.base.log.JSONLogger;

/**
 * 支持切面日志的调度执行器
 *
 * @author frankcl
 * @date 2023-07-28 11:14:56
 */
public abstract class AspectLogSupport extends ExecuteRunner {

    @Resource(name = "planAspectLogger")
    protected JSONLogger planAspectLogger;
    @Resource(name = "urlAspectLogger")
    protected JSONLogger urlAspectLogger;
    @Resource(name = "concurrentAspectLogger")
    protected JSONLogger concurrentAspectLogger;

    public AspectLogSupport(String name, Long executeIntervalMs) {
        super(name, executeIntervalMs);
    }

    /**
     * 提交并发单元切面日志
     *
     * @param context 上下文
     * @param concurrentUnit 并发单元
     * @param applyRecordNum 申请记录数
     * @param allocateRecordNum 分配记录数
     * @param overflowRecordNum 溢出记录数
     */
    protected void commitAspectLog(Context context, String concurrentUnit,
                                   int applyRecordNum, int allocateRecordNum,
                                   int overflowRecordNum) {
        if (context == null || concurrentAspectLogger == null) return;
        context.put(Constants.DARWIN_RECORD_TYPE, Constants.RECORD_TYPE_CONCURRENT_UNIT);
        context.put(Constants.CONCURRENT_UNIT, concurrentUnit);
        context.put(Constants.APPLY_RECORD_NUM, applyRecordNum);
        context.put(Constants.ALLOCATE_RECORD_NUM, allocateRecordNum);
        context.put(Constants.OVERFLOW_RECORD_NUM, overflowRecordNum);
        concurrentAspectLogger.commit(context.getFeatureMap());
    }

    /**
     * 提交URL记录切面日志
     *
     * @param context 上下文
     * @param record URL记录
     */
    protected void commitAspectLog(Context context, URLRecord record) {
        if (context == null || record == null || urlAspectLogger == null) return;
        DarwinUtil.putContext(context, record);
        urlAspectLogger.commit(context.getFeatureMap());
    }

    /**
     * 提交计划记录切面日志
     *
     * @param context 上下文
     * @param plan 计划
     */
    protected void commitAspectLog(Context context, Plan plan) {
        if (context == null || plan == null || planAspectLogger == null) return;
        DarwinUtil.putContext(context, plan);
        planAspectLogger.commit(context.getFeatureMap());
    }
}
