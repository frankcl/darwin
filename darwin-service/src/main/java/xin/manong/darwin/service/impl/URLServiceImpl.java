package xin.manong.darwin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import javax.annotation.Resource;

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
    public Boolean add(URLRecord record) {
        LambdaQueryWrapper<URLRecord> query = new LambdaQueryWrapper<>();
        query.eq(URLRecord::getKey, record.key);
        if (urlMapper.selectCount(query) > 0) {
            logger.error("record key[{}] has existed for url[{}]", record.key, record.url);
            throw new RuntimeException(String.format("URL记录key[%s]已存在", record.key));
        }
        return urlMapper.insert(record) > 0;
    }

    @Override
    public Boolean updateContent(URLRecord contentRecord) {
        if (contentRecord == null || StringUtils.isEmpty(contentRecord.key)) {
            logger.error("content record is null or key is missing");
            throw new RuntimeException("抓取结果为空或key缺失");
        }
        URLRecord record = get(contentRecord.key);
        if (record == null) {
            logger.error("record is not found for key[{}]", contentRecord.key);
            return false;
        }
        LambdaUpdateWrapper<URLRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(URLRecord::getKey, contentRecord.key);
        wrapper.set(URLRecord::getUpdateTime, System.currentTimeMillis());
        if (contentRecord.fetchTime != null) wrapper.set(URLRecord::getFetchTime, contentRecord.fetchTime);
        if (contentRecord.status != null) wrapper.set(URLRecord::getStatus, contentRecord.status);
        if (contentRecord.httpCode != null) wrapper.set(URLRecord::getHttpCode, contentRecord.httpCode);
        if (!StringUtils.isEmpty(contentRecord.mimeType)) {
            wrapper.set(URLRecord::getMimeType, contentRecord.mimeType);
        }
        if (!StringUtils.isEmpty(contentRecord.subMimeType)) {
            wrapper.set(URLRecord::getSubMimeType, contentRecord.subMimeType);
        }
        if (!StringUtils.isEmpty(contentRecord.fetchContentURL)) {
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
    public Boolean updateQueueTime(URLRecord record) {
        if (record == null || StringUtils.isEmpty(record.key)) {
            logger.error("url record is null or key is missing");
            throw new RuntimeException("URL记录为空或key缺失");
        }
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
    public Boolean updateStatus(String key, int status) {
        if (!Constants.SUPPORT_URL_STATUSES.containsKey(status)) {
            logger.error("not support URL status[{}]", status);
            throw new RuntimeException(String.format("不支持URL状态[%d]", status));
        }
        URLRecord record = get(key);
        if (record == null) {
            logger.error("record is not found for key[{}]", key);
            return false;
        }
        LambdaUpdateWrapper<URLRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(URLRecord::getKey, key).set(URLRecord::getStatus, status).
                set(URLRecord::getUpdateTime, System.currentTimeMillis());
        int n = urlMapper.update(null, wrapper);
        if (n > 0 && !StringUtils.isEmpty(record.url)) recordCache.invalidate(record.url);
        return n > 0;
    }



    @Override
    public URLRecord get(String key) {
        if (StringUtils.isEmpty(key)) {
            logger.error("url record key is empty");
            throw new IllegalArgumentException("URL记录key为空");
        }
        return urlMapper.selectById(key);
    }

    @Override
    public Boolean delete(String key) {
        URLRecord record = urlMapper.selectById(key);
        if (record == null) {
            logger.error("url record[{}] is not found", key);
            return false;
        }
        int n = urlMapper.deleteById(key);
        if (n > 0 && !StringUtils.isEmpty(record.url)) recordCache.invalidate(record.url);
        return n > 0;
    }

    @Override
    public Pager<URLRecord> search(URLSearchRequest searchRequest) {
        if (searchRequest == null) searchRequest = new URLSearchRequest();
        if (searchRequest.current == null || searchRequest.current < 1) searchRequest.current = Constants.DEFAULT_CURRENT;
        if (searchRequest.size == null || searchRequest.size <= 0) searchRequest.size = Constants.DEFAULT_PAGE_SIZE;
        LambdaQueryWrapper<URLRecord> query = new LambdaQueryWrapper<>();
        query.orderByDesc(URLRecord::getCreateTime);
        if (searchRequest.category != null) query.eq(URLRecord::getCategory, searchRequest.category);
        if (searchRequest.priority != null) query.eq(URLRecord::getPriority, searchRequest.priority);
        if (searchRequest.fetchMethod != null) query.eq(URLRecord::getFetchMethod, searchRequest.fetchMethod);
        if (!StringUtils.isEmpty(searchRequest.jobId)) query.eq(URLRecord::getJobId, searchRequest.jobId);
        if (!StringUtils.isEmpty(searchRequest.planId)) query.eq(URLRecord::getPlanId, searchRequest.planId);
        if (!StringUtils.isEmpty(searchRequest.url)) query.eq(URLRecord::getHash, DigestUtils.md5Hex(searchRequest.url));
        if (searchRequest.statusList != null && !searchRequest.statusList.isEmpty()) {
            query.in(URLRecord::getStatus, searchRequest.statusList);
        }
        if (searchRequest.fetchTime != null && searchRequest.fetchTime.start != null) {
            if (searchRequest.fetchTime.includeLower) query.ge(URLRecord::getFetchTime, searchRequest.fetchTime.start);
            else query.gt(URLRecord::getFetchTime, searchRequest.fetchTime.start);
        }
        if (searchRequest.fetchTime != null && searchRequest.fetchTime.end != null) {
            if (searchRequest.fetchTime.includeUpper) query.le(URLRecord::getFetchTime, searchRequest.fetchTime.end);
            else query.lt(URLRecord::getFetchTime, searchRequest.fetchTime.end);
        }
        if (searchRequest.createTime != null && searchRequest.createTime.start != null) {
            if (searchRequest.createTime.includeLower) query.ge(URLRecord::getCreateTime, searchRequest.createTime.start);
            else query.gt(URLRecord::getCreateTime, searchRequest.createTime.start);
        }
        if (searchRequest.createTime != null && searchRequest.createTime.end != null) {
            if (searchRequest.createTime.includeUpper) query.le(URLRecord::getCreateTime, searchRequest.createTime.end);
            else query.lt(URLRecord::getCreateTime, searchRequest.createTime.end);
        }
        IPage<URLRecord> page = urlMapper.selectPage(new Page<>(searchRequest.current, searchRequest.size), query);
        return Converter.convert(page);
    }
}
