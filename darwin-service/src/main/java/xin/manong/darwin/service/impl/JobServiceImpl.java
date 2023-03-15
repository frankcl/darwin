package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.JobMapper;
import xin.manong.darwin.service.iface.JobService;

import javax.annotation.Resource;

/**
 * MySQL任务服务实现
 *
 * @author frankcl
 * @date 2023-03-15 14:40:19
 */
@Service
public class JobServiceImpl implements JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

    @Resource
    protected JobMapper jobMapper;

    @Override
    public Job get(String jobId) {
        if (StringUtils.isEmpty(jobId)) {
            logger.error("job id is empty");
            throw new RuntimeException("任务ID为空");
        }
        return jobMapper.selectById(jobId);
    }

    @Override
    public Boolean add(Job job) {
        QueryWrapper<Job> query = new QueryWrapper<>();
        query.lambda().eq(Job::getName, job.getName());
        query.lambda().eq(Job::getPlanId, job.getPlanId());
        if (jobMapper.selectCount(query) > 0) {
            logger.error("job[{}] has existed for plan[{}]", job.name, job.planId);
            throw new RuntimeException(String.format("同名任务[%s]已存在", job.name));
        }
        return jobMapper.insert(job) > 0;
    }

    @Override
    public Boolean update(Job job) {
        if (jobMapper.selectById(job.jobId) == null) {
            logger.error("job[{}] is not found", job.jobId);
            throw new RuntimeException(String.format("任务[%d]不存在", job.jobId));
        }
        return jobMapper.updateById(job) > 0;
    }

    @Override
    public Boolean delete(String jobId) {
        if (jobMapper.selectById(jobId) == null) {
            logger.error("job[{}] is not found", jobId);
            throw new RuntimeException(String.format("任务[%d]不存在", jobId));
        }
        return jobMapper.deleteById(jobId) > 0;
    }

    @Override
    public Pager<Job> getList(int current, int size) {
        QueryWrapper<Job> query = new QueryWrapper<>();
        query.lambda().orderByDesc(Job::getCreateTime);
        query.lambda().orderByAsc(Job::getName);
        IPage<Job> page = jobMapper.selectPage(new Page<>(current, size), query);
        return Converter.convert(page);
    }

    @Override
    public Pager<Job> getJobs(int status, int current, int size) {
        if (!Constants.SUPPORT_JOB_STATUSES.contains(status)) {
            logger.error("not support job status[{}]", status);
            throw new RuntimeException(String.format("不支持的任务状态[%d]", status));
        }
        QueryWrapper<Job> query = new QueryWrapper<>();
        query.lambda().eq(Job::getStatus, status);
        query.lambda().orderByDesc(Job::getCreateTime);
        query.lambda().orderByAsc(Job::getName);
        IPage<Job> page = jobMapper.selectPage(new Page<>(current, size), query);
        return Converter.convert(page);
    }
}
