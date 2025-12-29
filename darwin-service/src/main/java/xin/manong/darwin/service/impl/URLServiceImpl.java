package xin.manong.darwin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.URLGroupCountMapper;
import xin.manong.darwin.service.dao.mapper.URLMapper;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * MySQL URL服务实现
 *
 * @author frankcl
 * @date 2023-03-20 20:02:44
 */
@Service
public class URLServiceImpl extends URLService {

    @Resource
    private URLMapper urlMapper;
    @Resource
    private URLGroupCountMapper URLGroupCountMapper;

    @Override
    public boolean add(URLRecord record) {
        if (get(record.key) != null) throw new IllegalStateException("URL记录已存在");
        return urlMapper.insert(record) > 0;
    }

    @Override
    public boolean updateContent(URLRecord record) {
        if (StringUtils.isEmpty(record.key)) throw new BadRequestException("key缺失");
        URLRecord prevRecord = get(record.key);
        if (prevRecord == null) return false;
        LambdaUpdateWrapper<URLRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(URLRecord::getKey, record.key);
        updateWrapper.set(URLRecord::getUpdateTime, System.currentTimeMillis());
        if (record.fetchTime != null) updateWrapper.set(URLRecord::getFetchTime, record.fetchTime);
        if (record.downTime != null) updateWrapper.set(URLRecord::getDownTime, record.downTime);
        if (record.status != null) updateWrapper.set(URLRecord::getStatus, record.status);
        if (record.contentType != null) updateWrapper.set(URLRecord::getContentType, record.contentType);
        if (record.httpCode != null) updateWrapper.set(URLRecord::getHttpCode, record.httpCode);
        if (record.fetched != null) updateWrapper.set(URLRecord::getFetched, record.fetched);
        if (record.contentLength != null) updateWrapper.set(URLRecord::getContentLength, record.contentLength);
        if (record.mediaType != null) updateWrapper.set(URLRecord::getMediaType, JSON.toJSONString(record.mediaType));
        if (StringUtils.isNotEmpty(record.charset)) {
            updateWrapper.set(URLRecord::getCharset, record.charset);
        }
        if (StringUtils.isNotEmpty(record.htmlCharset)) {
            updateWrapper.set(URLRecord::getHtmlCharset, record.htmlCharset);
        }
        if (StringUtils.isNotEmpty(record.fetchContentURL)) {
            updateWrapper.set(URLRecord::getFetchContentURL, record.fetchContentURL);
        }
        if (StringUtils.isNotEmpty(record.redirectURL)) {
            updateWrapper.set(URLRecord::getRedirectURL, record.redirectURL);
        }
        if (StringUtils.isNotEmpty(record.parentURL)) {
            updateWrapper.set(URLRecord::getParentURL, record.parentURL);
        }
        if (record.fieldMap != null && !record.fieldMap.isEmpty()) {
            updateWrapper.set(URLRecord::getFieldMap, JSON.toJSONString(record.fieldMap));
        }
        if (record.customMap != null && !record.customMap.isEmpty()) {
            updateWrapper.set(URLRecord::getCustomMap, JSON.toJSONString(record.customMap));
        }
        return urlMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean updateQueueTime(URLRecord record) {
        if (StringUtils.isEmpty(record.key)) throw new BadRequestException("key缺失");
        URLRecord prevRecord = get(record.key);
        if (prevRecord == null) return false;
        LambdaUpdateWrapper<URLRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(URLRecord::getUpdateTime, System.currentTimeMillis());
        updateWrapper.eq(URLRecord::getKey, record.key);
        if (record.status != null) updateWrapper.set(URLRecord::getStatus, record.status);
        if (record.pushTime != null) updateWrapper.set(URLRecord::getPushTime, record.pushTime);
        if (record.popTime != null) updateWrapper.set(URLRecord::getPopTime, record.popTime);
        return urlMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean updateStatus(String key, int status) {
        if (!Constants.SUPPORT_URL_STATUSES.containsKey(status)) throw new BadRequestException("URL状态非法");
        URLRecord record = get(key);
        if (record == null) return false;
        LambdaUpdateWrapper<URLRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(URLRecord::getKey, key).
                set(URLRecord::getStatus, status).
                set(URLRecord::getUpdateTime, System.currentTimeMillis());
        return urlMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public URLRecord get(String key) {
        return urlMapper.selectById(key);
    }

    @Override
    public List<URLRecord> getChildren(String parentKey) {
        if (StringUtils.isEmpty(parentKey)) return new ArrayList<>();
        LambdaQueryWrapper<URLRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(URLRecord::getParentKey, parentKey);
        return urlMapper.selectList(queryWrapper);
    }

    @Override
    public boolean delete(String key) {
        URLRecord record = urlMapper.selectById(key);
        if (record == null) throw new NotFoundException("URL记录不存在");
        return urlMapper.deleteById(key) > 0;
    }

    @Override
    public boolean deleteByJob(String jobId) {
        if (StringUtils.isEmpty(jobId)) throw new BadRequestException("任务ID为空");
        LambdaQueryWrapper<URLRecord> query = new LambdaQueryWrapper<>();
        query.eq(URLRecord::getJobId, jobId);
        if (urlMapper.selectCount(query) == 0) return true;
        return urlMapper.delete(query) > 0;
    }

    @Override
    public int deleteExpired(long expiredTime) {
        LambdaQueryWrapper<URLRecord> query = new LambdaQueryWrapper<>();
        query.lt(URLRecord::getCreateTime, expiredTime);
        return urlMapper.delete(query);
    }

    @Override
    public Pager<URLRecord> search(URLSearchRequest searchRequest) {
        searchRequest = prepareSearchRequest(searchRequest);
        ModelValidator.validateOrderBy(URLRecord.class, searchRequest);
        QueryWrapper<URLRecord> query = buildQueryWrapper(searchRequest);
        searchRequest.prepareOrderBy(query);
        IPage<URLRecord> page = urlMapper.selectPage(
                new Page<>(searchRequest.pageNum, searchRequest.pageSize), query);
        return Converter.convert(page);
    }

    @Override
    public long selectCount(URLSearchRequest searchRequest) {
        searchRequest = prepareSearchRequest(searchRequest);
        QueryWrapper<URLRecord> query = buildQueryWrapper(searchRequest);
        return urlMapper.selectCount(query);
    }

    @Override
    public List<URLGroupCount> statusGroupCount(String jobId, RangeValue<Long> timeRange) {
        LambdaQueryWrapper<URLGroupCount> query = new LambdaQueryWrapper<>();
        query.select(URLGroupCount::getStatus, URLGroupCount::getCount);
        if (jobId != null) query.eq(URLGroupCount::getJobId, jobId);
        if (timeRange != null) prepareCreateTimeRange(query, timeRange);
        query.groupBy(URLGroupCount::getStatus);
        return URLGroupCountMapper.selectList(query);
    }

    @Override
    public List<URLGroupCount> contentGroupCount(String jobId, RangeValue<Long> timeRange) {
        LambdaQueryWrapper<URLGroupCount> query = new LambdaQueryWrapper<>();
        query.select(URLGroupCount::getContentType, URLGroupCount::getCount);
        query.ge(URLGroupCount::getContentType, Constants.CONTENT_TYPE_PAGE);
        if (timeRange != null) prepareCreateTimeRange(query, timeRange);
        if (jobId != null) query.eq(URLGroupCount::getJobId, jobId);
        query.groupBy(URLGroupCount::getContentType);
        return URLGroupCountMapper.selectList(query);
    }

    @Override
    public int fetchURLCount(RangeValue<Long> timeRange) {
        LambdaQueryWrapper<URLGroupCount> query = new LambdaQueryWrapper<>();
        query.select(URLGroupCount::getCount);
        prepareCreateTimeRange(query, timeRange);
        URLGroupCount urlGroupCount = URLGroupCountMapper.selectOne(query, false);
        return urlGroupCount == null ? 0 : urlGroupCount.count;
    }

    @Override
    public int fetchHostCount(RangeValue<Long> timeRange) {
        QueryWrapper<URLRecord> query = new QueryWrapper<>();
        query.select("DISTINCT host");
        prepareCreateTimeRange(query, timeRange);
        return urlMapper.selectCount(query).intValue();
    }

    @Override
    public int fetchDomainCount(RangeValue<Long> timeRange) {
        QueryWrapper<URLRecord> query = new QueryWrapper<>();
        query.select("DISTINCT domain");
        prepareCreateTimeRange(query, timeRange);
        return urlMapper.selectCount(query).intValue();
    }

    @Override
    public List<URLGroupCount> waitConcurrencyUnits(int n) {
        List<Integer> statusList = new ArrayList<>();
        statusList.add(Constants.URL_STATUS_QUEUING);
        LambdaQueryWrapper<URLGroupCount> query = new LambdaQueryWrapper<>();
        query.select(URLGroupCount::getConcurrencyUnit, URLGroupCount::getCount);
        query.in(URLGroupCount::getStatus, statusList);
        query.groupBy(URLGroupCount::getConcurrencyUnit);
        query.orderByDesc(URLGroupCount::getCount);
        query.last(String.format("LIMIT %d", n));
        return URLGroupCountMapper.selectList(query);
    }

    @Override
    public List<URLGroupCount> queueWaitPriority() {
        List<Integer> statusList = new ArrayList<>();
        statusList.add(Constants.URL_STATUS_QUEUING);
        LambdaQueryWrapper<URLGroupCount> query = new LambdaQueryWrapper<>();
        query.in(URLGroupCount::getStatus, statusList);
        query.groupBy(URLGroupCount::getPriority);
        query.select(URLGroupCount::getPriority, URLGroupCount::getCount);
        return URLGroupCountMapper.selectList(query);
    }

    @Override
    public List<URLGroupCount> hostFetchCount(RangeValue<Long> timeRange, int n) {
        LambdaQueryWrapper<URLGroupCount> query = new LambdaQueryWrapper<>();
        query.select(URLGroupCount::getHost, URLGroupCount::getCount);
        prepareCreateTimeRange(query, timeRange);
        query.groupBy(URLGroupCount::getHost);
        query.orderByDesc(URLGroupCount::getCount);
        query.last(String.format("LIMIT %d", n));
        return URLGroupCountMapper.selectList(query);
    }

    /**
     * 准备创建时间范围查询条件
     *
     * @param query SQL查询
     * @param timeRange 时间范围
     */
    private void prepareCreateTimeRange(LambdaQueryWrapper<? extends BaseModel> query, RangeValue<Long> timeRange) {
        if (timeRange.start != null) {
            if (timeRange.includeLower) query.ge(BaseModel::getCreateTime, timeRange.start);
            else query.gt(BaseModel::getCreateTime, timeRange.start);
        }
        if (timeRange.end != null) {
            if (timeRange.includeLower) query.le(BaseModel::getCreateTime, timeRange.end);
            else query.lt(BaseModel::getCreateTime, timeRange.end);
        }
    }

    /**
     * 准备创建时间范围查询条件
     *
     * @param query SQL查询
     * @param timeRange 时间范围
     */
    private void prepareCreateTimeRange(QueryWrapper<? extends BaseModel> query, RangeValue<Long> timeRange) {
        if (timeRange.start != null) {
            if (timeRange.includeLower) query.ge("create_time", timeRange.start);
            else query.gt("create_time", timeRange.start);
        }
        if (timeRange.end != null) {
            if (timeRange.includeLower) query.le("create_time", timeRange.end);
            else query.lt("create_time", timeRange.end);
        }
    }

    /**
     * 根据搜索请求构建SQL查询条件
     *
     * @param searchRequest 搜索请求
     * @return SQL查询条件
     */
    private QueryWrapper<URLRecord> buildQueryWrapper(URLSearchRequest searchRequest) {
        QueryWrapper<URLRecord> query = new QueryWrapper<>();
        if (searchRequest.contentType != null) query.eq("content_type", searchRequest.contentType);
        if (searchRequest.priority != null) query.eq("priority", searchRequest.priority);
        if (searchRequest.fetchMethod != null) query.eq("fetch_method", searchRequest.fetchMethod);
        if (searchRequest.appId != null) query.eq("app_id", searchRequest.appId);
        if (searchRequest.httpRequest != null) query.eq("http_request", searchRequest.httpRequest.name());
        if (StringUtils.isNotEmpty(searchRequest.jobId)) query.eq("job_id", searchRequest.jobId);
        if (StringUtils.isNotEmpty(searchRequest.planId)) query.eq("plan_id", searchRequest.planId);
        if (StringUtils.isNotEmpty(searchRequest.host)) query.eq("host", searchRequest.host);
        if (StringUtils.isNotEmpty(searchRequest.domain)) query.eq("domain", searchRequest.domain);
        if (StringUtils.isNotEmpty(searchRequest.concurrencyUnit)) query.eq("concurrency_unit", searchRequest.concurrencyUnit);
        if (StringUtils.isNotEmpty(searchRequest.url)) {
            if (searchRequest.requestBody != null && !searchRequest.requestBody.isEmpty()) {
                query.eq("request_hash", DigestUtils.md5Hex(String.format("%s_%s",
                        searchRequest.url, JSON.toJSONString(searchRequest.requestBody))));
            } else {
                query.eq("hash", DigestUtils.md5Hex(searchRequest.url));
            }
        }
        if (searchRequest.statusList != null && !searchRequest.statusList.isEmpty()) {
            query.in("status", searchRequest.statusList);
        }
        if (searchRequest.fetchTimeRange != null && searchRequest.fetchTimeRange.start != null) {
            if (searchRequest.fetchTimeRange.includeLower) query.ge("fetch_time", searchRequest.fetchTimeRange.start);
            else query.gt("fetch_time", searchRequest.fetchTimeRange.start);
        }
        if (searchRequest.fetchTimeRange != null && searchRequest.fetchTimeRange.end != null) {
            if (searchRequest.fetchTimeRange.includeUpper) query.le("fetch_time", searchRequest.fetchTimeRange.end);
            else query.lt("fetch_time", searchRequest.fetchTimeRange.end);
        }
        if (searchRequest.createTimeRange != null && searchRequest.createTimeRange.start != null) {
            if (searchRequest.createTimeRange.includeLower) query.ge("create_time", searchRequest.createTimeRange.start);
            else query.gt("create_time", searchRequest.createTimeRange.start);
        }
        if (searchRequest.createTimeRange != null && searchRequest.createTimeRange.end != null) {
            if (searchRequest.createTimeRange.includeUpper) query.le("create_time", searchRequest.createTimeRange.end);
            else query.lt("create_time", searchRequest.createTimeRange.end);
        }
        return query;
    }
}
