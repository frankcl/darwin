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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.config.CacheConfig;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.dao.mapper.URLMapper;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.darwin.service.util.ModelValidator;

/**
 * MySQL URL服务实现
 *
 * @author frankcl
 * @date 2023-03-20 20:02:44
 */
@Service
public class URLServiceImpl extends URLService {

    private static final Logger logger = LoggerFactory.getLogger(URLServiceImpl.class);

    @Resource
    protected URLMapper urlMapper;

    @Autowired
    public URLServiceImpl(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    public boolean add(URLRecord record) {
        LambdaQueryWrapper<URLRecord> query = new LambdaQueryWrapper<>();
        query.eq(URLRecord::getKey, record.key);
        if (urlMapper.selectCount(query) > 0) throw new IllegalStateException("URL记录已存在");
        return urlMapper.insert(record) > 0;
    }

    @Override
    public boolean updateContent(URLRecord contentRecord) {
        if (contentRecord == null || StringUtils.isEmpty(contentRecord.key)) {
            throw new BadRequestException("抓取结果为空或key缺失");
        }
        URLRecord record = get(contentRecord.key);
        if (record == null) return false;
        LambdaUpdateWrapper<URLRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(URLRecord::getKey, contentRecord.key);
        wrapper.set(URLRecord::getUpdateTime, System.currentTimeMillis());
        if (contentRecord.fetchTime != null) wrapper.set(URLRecord::getFetchTime, contentRecord.fetchTime);
        if (contentRecord.status != null) wrapper.set(URLRecord::getStatus, contentRecord.status);
        if (contentRecord.httpCode != null) wrapper.set(URLRecord::getHttpCode, contentRecord.httpCode);
        if (StringUtils.isNotEmpty(contentRecord.mimeType)) {
            wrapper.set(URLRecord::getMimeType, contentRecord.mimeType);
        }
        if (StringUtils.isNotEmpty(contentRecord.subMimeType)) {
            wrapper.set(URLRecord::getSubMimeType, contentRecord.subMimeType);
        }
        if (StringUtils.isNotEmpty(contentRecord.fetchContentURL)) {
            wrapper.set(URLRecord::getFetchContentURL, contentRecord.fetchContentURL);
        }
        if (contentRecord.fieldMap != null && !contentRecord.fieldMap.isEmpty()) {
            wrapper.set(URLRecord::getFieldMap, JSON.toJSONString(contentRecord.fieldMap));
        }
        if (contentRecord.userDefinedMap != null && !contentRecord.userDefinedMap.isEmpty()) {
            wrapper.set(URLRecord::getUserDefinedMap, JSON.toJSONString(contentRecord.userDefinedMap));
        }
        int n = urlMapper.update(null, wrapper);
        if (n > 0 && !StringUtils.isEmpty(record.url)) recordCache.invalidate(record.url);
        return n > 0;
    }

    @Override
    public boolean updateQueueTime(URLRecord record) {
        if (record == null || StringUtils.isEmpty(record.key)) throw new BadRequestException("抓取结果为空或key缺失");
        LambdaUpdateWrapper<URLRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(URLRecord::getKey, record.key);
        wrapper.set(URLRecord::getUpdateTime, System.currentTimeMillis());
        if (record.status != null) wrapper.set(URLRecord::getStatus, record.status);
        if (record.inQueueTime != null) wrapper.set(URLRecord::getInQueueTime, record.inQueueTime);
        if (record.outQueueTime != null) wrapper.set(URLRecord::getOutQueueTime, record.outQueueTime);
        int n = urlMapper.update(null, wrapper);
        if (n > 0 && !StringUtils.isEmpty(record.url)) recordCache.invalidate(record.url);
        return n > 0;
    }

    @Override
    public boolean updateStatus(String key, int status) {
        if (!Constants.SUPPORT_URL_STATUSES.containsKey(status)) throw new BadRequestException("不支持URL状态");
        URLRecord record = get(key);
        if (record == null) return false;
        LambdaUpdateWrapper<URLRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(URLRecord::getKey, key).set(URLRecord::getStatus, status).
                set(URLRecord::getUpdateTime, System.currentTimeMillis());
        int n = urlMapper.update(null, wrapper);
        if (n > 0 && !StringUtils.isEmpty(record.url)) recordCache.invalidate(record.url);
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
        if (n > 0 && !StringUtils.isEmpty(record.url)) recordCache.invalidate(record.url);
        return n > 0;
    }

    @Override
    public Pager<URLRecord> search(URLSearchRequest searchRequest) {
        searchRequest = prepareSearchRequest(searchRequest);
        ModelValidator.validateOrderBy(URLRecord.class, searchRequest);
        QueryWrapper<URLRecord> query = new QueryWrapper<>();
        searchRequest.prepareOrderBy(query);
        if (searchRequest.category != null) query.eq("category", searchRequest.category);
        if (searchRequest.priority != null) query.eq("priority", searchRequest.priority);
        if (searchRequest.fetchMethod != null) query.eq("fetch_method", searchRequest.fetchMethod);
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
        IPage<URLRecord> page = urlMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
