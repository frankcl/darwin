package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.JobMapper;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.request.JobSearchRequest;

import javax.annotation.Resource;

/**
 * MySQL任务服务实现
 *
 * @author frankcl
 * @date 2023-03-15 14:40:19
 */
@Service
public class JobServiceImpl extends JobService {

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
        LambdaQueryWrapper<Job> query = new LambdaQueryWrapper<>();
        query.eq(Job::getName, job.getName()).eq(Job::getPlanId, job.getPlanId());
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
            return false;
        }
        int n = jobMapper.updateById(job);
        if (n > 0) jobCache.invalidate(job.jobId);
        return n > 0;
    }

    @Override
    public Boolean delete(String jobId) {
        if (jobMapper.selectById(jobId) == null) {
            logger.error("job[{}] is not found", jobId);
            return false;
        }
        int n = jobMapper.deleteById(jobId);
        if (n > 0) jobCache.invalidate(jobId);
        return n > 0;
    }

    @Override
    public Pager<Job> search(JobSearchRequest searchRequest) {
        LambdaQueryWrapper<Job> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Job::getCreateTime);
        if (searchRequest != null) {
            if (searchRequest.priority != null) query.eq(Job::getPriority, searchRequest.priority);
            if (searchRequest.status != null) query.eq(Job::getStatus, searchRequest.status);
            if (searchRequest.planId != null) query.eq(Job::getPlanId, searchRequest.planId);
            if (!StringUtils.isEmpty(searchRequest.name)) query.like(Job::getName, searchRequest.name);
        }
        IPage<Job> page = jobMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
