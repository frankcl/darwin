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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        if (record.status != null) updateWrapper.set(URLRecord::getStatus, record.status);
        if (record.category != null) updateWrapper.set(URLRecord::getCategory, record.category);
        if (record.httpCode != null) updateWrapper.set(URLRecord::getHttpCode, record.httpCode);
        if (record.fetched != null) updateWrapper.set(URLRecord::getFetched, record.fetched);
        if (record.contentLength != null) updateWrapper.set(URLRecord::getContentLength, record.contentLength);
        if (StringUtils.isNotEmpty(record.mimeType)) {
            updateWrapper.set(URLRecord::getMimeType, record.mimeType);
        }
        if (StringUtils.isNotEmpty(record.subMimeType)) {
            updateWrapper.set(URLRecord::getSubMimeType, record.subMimeType);
        }
        if (StringUtils.isNotEmpty(record.mediaType)) {
            updateWrapper.set(URLRecord::getMediaType, record.mediaType);
        }
        if (StringUtils.isNotEmpty(record.primitiveCharset)) {
            updateWrapper.set(URLRecord::getPrimitiveCharset, record.primitiveCharset);
        }
        if (StringUtils.isNotEmpty(record.charset)) {
            updateWrapper.set(URLRecord::getCharset, record.charset);
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
    public boolean delete(String key) {
        URLRecord record = urlMapper.selectById(key);
        if (record == null) throw new NotFoundException("URL记录不存在");
        return urlMapper.deleteById(key) > 0;
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
    public List<URLGroupCount> countGroupByStatus(String jobId, RangeValue<Long> timeRange) {
        LambdaQueryWrapper<URLGroupCount> query = new LambdaQueryWrapper<>();
        query.select(URLGroupCount::getStatus, URLGroupCount::getCount);
        if (jobId != null) query.eq(URLGroupCount::getJobId, jobId);
        if (timeRange != null) prepareCreateTimeRange(query, timeRange);
        query.groupBy(URLGroupCount::getStatus);
        return URLGroupCountMapper.selectList(query);
    }

    @Override
    public List<URLGroupCount> countGroupByCategory(String jobId, RangeValue<Long> timeRange) {
        LambdaQueryWrapper<URLGroupCount> query = new LambdaQueryWrapper<>();
        query.select(URLGroupCount::getCategory, URLGroupCount::getCount);
        if (timeRange != null) prepareCreateTimeRange(query, timeRange);
        if (jobId != null) query.eq(URLGroupCount::getJobId, jobId);
        query.groupBy(URLGroupCount::getCategory);
        return URLGroupCountMapper.selectList(query);
    }

    @Override
    public int urlCount(RangeValue<Long> timeRange) {
        LambdaQueryWrapper<URLGroupCount> query = new LambdaQueryWrapper<>();
        query.select(URLGroupCount::getCount);
        prepareCreateTimeRange(query, timeRange);
        URLGroupCount urlGroupCount = URLGroupCountMapper.selectOne(query, false);
        return urlGroupCount == null ? 0 : urlGroupCount.count;
    }

    @Override
    public int hostCount(RangeValue<Long> timeRange) {
        QueryWrapper<URLRecord> query = new QueryWrapper<>();
        query.select("DISTINCT host");
        prepareCreateTimeRange(query, timeRange);
        return urlMapper.selectCount(query).intValue();
    }

    @Override
    public int domainCount(RangeValue<Long> timeRange) {
        QueryWrapper<URLRecord> query = new QueryWrapper<>();
        query.select("DISTINCT domain");
        prepareCreateTimeRange(query, timeRange);
        return urlMapper.selectCount(query).intValue();
    }

    @Override
    public long avgContentLength(Integer category, RangeValue<Long> timeRange) {
        QueryWrapper<URLRecord> query = new QueryWrapper<>();
        String columnName = "content_length";
        String avgColumnName = "avgContentLength";
        query.select(String.format("AVG(%s) AS %s", columnName, avgColumnName));
        query.gt(columnName, 0);
        if (category != null) query.eq("category", category);
        prepareCreateTimeRange(query, timeRange);
        List<Map<String, Object>> results = urlMapper.selectMaps(query);
        if (results == null || results.isEmpty() || results.get(0) == null) return 0L;
        return ((BigDecimal) results.get(0).get(avgColumnName)).longValue();
    }

    @Override
    public List<URLGroupCount> topConcurrencyUnits(int top) {
        List<Integer> statusList = new ArrayList<>();
        statusList.add(Constants.URL_STATUS_QUEUING);
        statusList.add(Constants.URL_STATUS_FETCHING);
        LambdaQueryWrapper<URLGroupCount> query = new LambdaQueryWrapper<>();
        query.select(URLGroupCount::getConcurrencyUnit, URLGroupCount::getCount);
        query.in(URLGroupCount::getStatus, statusList);
        query.groupBy(URLGroupCount::getConcurrencyUnit);
        query.orderByDesc(URLGroupCount::getCount);
        query.last(String.format("LIMIT %d", top));
        return URLGroupCountMapper.selectList(query);
    }

    @Override
    public List<URLGroupCount> topHosts(RangeValue<Long> timeRange, int top) {
        LambdaQueryWrapper<URLGroupCount> query = new LambdaQueryWrapper<>();
        query.select(URLGroupCount::getHost, URLGroupCount::getCount);
        prepareCreateTimeRange(query, timeRange);
        query.groupBy(URLGroupCount::getHost);
        query.orderByDesc(URLGroupCount::getCount);
        query.last(String.format("LIMIT %d", top));
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
        if (searchRequest.category != null) query.eq("category", searchRequest.category);
        if (searchRequest.priority != null) query.eq("priority", searchRequest.priority);
        if (searchRequest.fetchMethod != null) query.eq("fetch_method", searchRequest.fetchMethod);
        if (searchRequest.appId != null) query.eq("app_id", searchRequest.appId);
        if (StringUtils.isNotEmpty(searchRequest.jobId)) query.eq("job_id", searchRequest.jobId);
        if (StringUtils.isNotEmpty(searchRequest.planId)) query.eq("plan_id", searchRequest.planId);
        if (StringUtils.isNotEmpty(searchRequest.mediaType)) query.eq("media_type", searchRequest.mediaType);
        if (StringUtils.isNotEmpty(searchRequest.host)) query.eq("host", searchRequest.host);
        if (StringUtils.isNotEmpty(searchRequest.domain)) query.eq("domain", searchRequest.domain);
        if (StringUtils.isNotEmpty(searchRequest.url)) query.eq("hash", DigestUtils.md5Hex(searchRequest.url));
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
