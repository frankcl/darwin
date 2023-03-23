package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Plan;

/**
 * 事务型服务接口定义
 *
 * @author frankcl
 * @date 2023-03-23 11:13:05
 */
public interface TransactionService {

    /**
     * 为周期性计划构建任务
     * 1. 创建周期性计划任务
     * 2. 添加种子URL记录
     * 3. 更新周期性计划下次调度时间
     *
     * @param plan 周期性计划
     * @return 成功返回任务，否则返回null
     */
    Job buildJobRepeatedPlan(Plan plan);
}
