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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.URLGroupCount;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.config.CacheConfig;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.URLGroupCountMapper;
import xin.manong.darwin.service.dao.mapper.URLMapper;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

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
    protected URLMapper urlMapper;
    @Resource
    protected URLGroupCountMapper URLGroupCountMapper;

    @Autowired
    public URLServiceImpl(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    public boolean add(URLRecord record) {
        if (get(record.key) != null) throw new IllegalStateException("URL记录已存在");
        if (urlMapper.insert(record) > 0) {
            keyCache.invalidate(record.hash);
            return true;
        }
        return false;
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
        if (record.httpCode != null) updateWrapper.set(URLRecord::getHttpCode, record.httpCode);
        if (StringUtils.isNotEmpty(record.mimeType)) {
            updateWrapper.set(URLRecord::getMimeType, record.mimeType);
        }
        if (StringUtils.isNotEmpty(record.subMimeType)) {
            updateWrapper.set(URLRecord::getSubMimeType, record.subMimeType);
        }
        if (StringUtils.isNotEmpty(record.fetchContentURL)) {
            updateWrapper.set(URLRecord::getFetchContentURL, record.fetchContentURL);
        }
        if (record.fieldMap != null && !record.fieldMap.isEmpty()) {
            updateWrapper.set(URLRecord::getFieldMap, JSON.toJSONString(record.fieldMap));
        }
        if (record.userDefinedMap != null && !record.userDefinedMap.isEmpty()) {
            updateWrapper.set(URLRecord::getUserDefinedMap, JSON.toJSONString(record.userDefinedMap));
        }
        int n = urlMapper.update(null, updateWrapper);
        if (n > 0) {
            recordCache.invalidate(record.key);
            keyCache.invalidate(prevRecord.hash);
        }
        return n > 0;
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
        int n = urlMapper.update(null, updateWrapper);
        if (n > 0) {
            recordCache.invalidate(record.key);
            keyCache.invalidate(prevRecord.hash);
        }
        return n > 0;
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
        int n = urlMapper.update(null, updateWrapper);
        if (n > 0) {
            recordCache.invalidate(key);
            keyCache.invalidate(record.hash);
        }
        return n > 0;
    }

    @Override
    public URLRecord get(String key) {
        return urlMapper.selectById(key);
    }

    @Override
    public boolean delete(String key) {
        URLRecord record = urlMapper.selectById(key);
        if (record == null) throw new NotFoundException("URL记录不存在");
        int n = urlMapper.deleteById(key);
        if (n > 0) {
            recordCache.invalidate(key);
            keyCache.invalidate(record.hash);
        }
        return n > 0;
    }

    @Override
    public Pager<URLRecord> search(URLSearchRequest searchRequest) {
        searchRequest = prepareSearchRequest(searchRequest);
        ModelValidator.validateOrderBy(URLRecord.class, searchRequest);
        QueryWrapper<URLRecord> query = buildQueryWrapper(searchRequest);
        searchRequest.prepareOrderBy(query);
        IPage<URLRecord> page = urlMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }

    @Override
    public long selectCount(URLSearchRequest searchRequest) {
        searchRequest = prepareSearchRequest(searchRequest);
        QueryWrapper<URLRecord> query = buildQueryWrapper(searchRequest);
        return urlMapper.selectCount(query);
    }

    @Override
    public List<URLGroupCount> bucketCountGroupByStatus(String jobId) {
        LambdaQueryWrapper<URLGroupCount> query = new LambdaQueryWrapper<>();
        query.select(URLGroupCount::getStatus, URLGroupCount::getCount);
        query.eq(URLGroupCount::getJobId, jobId);
        query.groupBy(URLGroupCount::getStatus);
        return URLGroupCountMapper.selectList(query);
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
