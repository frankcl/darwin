package xin.manong.darwin.schedule;

import jakarta.annotation.Resource;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Plan;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.common.util.DarwinUtil;
import xin.manong.weapon.base.common.Context;
import xin.manong.weapon.base.executor.ExecuteRunner;
import xin.manong.weapon.base.log.JSONLogger;

/**
 * 调度执行器
 *
 * @author frankcl
 * @date 2023-07-28 11:14:56
 */
public abstract class AbstractExecuteRunner extends ExecuteRunner {

    @Resource(name = "planAspectLogger")
    protected JSONLogger planAspectLogger;
    @Resource(name = "recordAspectLogger")
    protected JSONLogger recordAspectLogger;
    @Resource(name = "concurrentAspectLogger")
    protected JSONLogger concurrentAspectLogger;

    public AbstractExecuteRunner(String name, Long executeIntervalMs) {
        super(name, executeIntervalMs);
    }

    /**
     * 提交并发单元切面日志
     *
     * @param context 上下文
     * @param concurrentUnit 并发单元
     * @param appliedConnections 申请连接数
     * @param acquiredConnections 获取连接数
     * @param overflowConnections 溢出连接数
     */
    protected void commitAspectLog(Context context, String concurrentUnit,
                                   int appliedConnections, int acquiredConnections, int overflowConnections) {
        if (context == null || concurrentAspectLogger == null) return;
        context.put(Constants.DARWIN_RECORD_TYPE, Constants.RECORD_TYPE_CONCURRENT_UNIT);
        context.put(Constants.CONCURRENT_UNIT, concurrentUnit);
        context.put(Constants.APPLIED_CONNECTION_NUM, appliedConnections);
        context.put(Constants.ACQUIRED_CONNECTION_NUM, acquiredConnections);
        context.put(Constants.OVERFLOW_CONNECTION_NUM, overflowConnections);
        concurrentAspectLogger.commit(context.getFeatureMap());
    }

    /**
     * 提交URL记录切面日志
     *
     * @param context 上下文
     * @param record URL记录
     */
    protected void commitAspectLog(Context context, URLRecord record) {
        if (context == null || record == null || recordAspectLogger == null) return;
        DarwinUtil.putContext(context, record);
        recordAspectLogger.commit(context.getFeatureMap());
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
