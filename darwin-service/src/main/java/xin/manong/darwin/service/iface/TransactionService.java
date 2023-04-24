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
     * 根据计划构建任务
     * 1. 创建任务
     * 2. 添加种子URL记录
     * 3. 针对周期性计划，更新计划下次调度时间
     *
     * @param plan 计划
     * @return 成功返回任务，否则返回null
     */
    Job buildJob(Plan plan);
}
