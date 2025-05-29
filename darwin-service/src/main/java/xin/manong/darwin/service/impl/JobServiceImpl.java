package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.config.CacheConfig;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.JobMapper;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.request.JobSearchRequest;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

/**
 * MySQL任务服务实现
 *
 * @author frankcl
 * @date 2023-03-15 14:40:19
 */
@Service
public class JobServiceImpl extends JobService {

    @Resource
    private JobMapper jobMapper;

    @Autowired
    public JobServiceImpl(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    public Job get(String jobId) {
        if (StringUtils.isEmpty(jobId)) throw new BadRequestException("任务ID为空");
        return jobMapper.selectById(jobId);
    }

    @Override
    public boolean add(Job job) {
        LambdaQueryWrapper<Job> query = new LambdaQueryWrapper<>();
        query.eq(Job::getName, job.getName()).eq(Job::getPlanId, job.getPlanId());
        if (jobMapper.selectCount(query) > 0) throw new IllegalStateException("任务已存在");
        return jobMapper.insert(job) > 0;
    }

    @Override
    public boolean update(Job job) {
        if (jobMapper.selectById(job.jobId) == null) throw new NotFoundException("任务不存在");
        if (jobMapper.updateById(job) > 0) {
            jobCache.invalidate(job.jobId);
            return true;
        }
        return false;
    }

    @Override
    public void complete(String jobId) {
        if (jobMapper.selectById(jobId) == null) throw new NotFoundException("任务不存在");
        LambdaUpdateWrapper<Job> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Job::getJobId, jobId);
        updateWrapper.set(Job::getStatus, false);
        if (jobMapper.update(updateWrapper) > 0) {
            jobCache.invalidate(jobId);
        }
    }

    @Override
    public boolean delete(String jobId) {
        if (jobMapper.selectById(jobId) == null) throw new NotFoundException("任务不存在");
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.jobId = jobId;
        Pager<URLRecord> pager = urlService.search(searchRequest);
        if (pager.total > 0) throw new ForbiddenException("任务URL记录不为空");
        if (jobMapper.deleteById(jobId) > 0) {
            jobCache.invalidate(jobId);
            return true;
        }
        return false;
    }

    @Override
    public int deleteExpired(long expiredTime) {
        LambdaQueryWrapper<Job> query = new LambdaQueryWrapper<>();
        query.lt(Job::getCreateTime, expiredTime);
        return jobMapper.delete(query);
    }

    @Override
    public Pager<Job> search(JobSearchRequest searchRequest) {
        searchRequest = prepareSearchRequest(searchRequest);
        ModelValidator.validateOrderBy(Job.class, searchRequest);
        QueryWrapper<Job> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (searchRequest.status != null) query.eq("status", searchRequest.status);
        if (searchRequest.planId != null) query.eq("plan_id", searchRequest.planId);
        if (StringUtils.isNotEmpty(searchRequest.name)) {
            query.like("name", searchRequest.name).or().eq("job_id", searchRequest.name);
        }
        if (searchRequest.createTimeRange != null && searchRequest.createTimeRange.end != null) {
            if (searchRequest.createTimeRange.includeUpper) query.le("create_time", searchRequest.createTimeRange.end);
            else query.lt("create_time", searchRequest.createTimeRange.end);
        }
        if (searchRequest.createTimeRange != null && searchRequest.createTimeRange.start != null) {
            if (searchRequest.createTimeRange.includeLower) query.ge("create_time", searchRequest.createTimeRange.start);
            else query.gt("create_time", searchRequest.createTimeRange.start);
        }
        IPage<Job> page = jobMapper.selectPage(new Page<>(searchRequest.pageNum, searchRequest.pageSize), query);
        return Converter.convert(page);
    }
}
