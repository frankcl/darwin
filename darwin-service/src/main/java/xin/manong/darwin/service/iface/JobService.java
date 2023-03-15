package xin.manong.darwin.service.iface;

import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;

/**
 * 任务服务接口定义
 *
 * @author frankcl
 * @date 2023-03-15 14:29:12
 */
public interface JobService {

    /**
     * 根据任务ID获取任务信息
     *
     * @param jobId 任务ID
     * @return 任务信息，如果不存在返回null
     */
    Job get(String jobId);

    /**
     * 添加任务
     *
     * @param job 任务信息
     * @return 成功返回true，否则返回false
     */
    Boolean add(Job job);

    /**
     * 更新任务
     *
     * @param job 任务信息
     * @return 成功返回true，否则返回false
     */
    Boolean update(Job job);

    /**
     * 删除任务
     *
     * @param jobId 任务ID
     * @return 成功返回true，否则返回false
     */
    Boolean delete(String jobId);

    /**
     * 获取分页任务列表
     *
     * @param current 页码，从1开始
     * @param size 每页数量
     * @return 分页列表
     */
    Pager<Job> getList(int current, int size);

    /**
     * 根据状态获取任务列表
     *
     * @param status
     * @param current
     * @param size
     * @return 分页列表
     */
    Pager<Job> getJobs(int status, int current, int size);
}
