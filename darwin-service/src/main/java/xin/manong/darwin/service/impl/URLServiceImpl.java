package xin.manong.darwin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.FetchRecord;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.URLRecord;
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
public class URLServiceImpl implements URLService {

    private static final Logger logger = LoggerFactory.getLogger(URLServiceImpl.class);

    @Resource
    protected URLMapper urlMapper;

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
    public Boolean updateWithFetchRecord(FetchRecord fetchRecord) {
        if (fetchRecord == null || StringUtils.isEmpty(fetchRecord.key)) {
            logger.error("fetch record is null or key is missing");
            throw new RuntimeException("抓取结果为空或key缺失");
        }
        URLRecord record = get(fetchRecord.key);
        if (record == null) {
            logger.error("record is not found for key[{}]", fetchRecord.key);
            throw new RuntimeException(String.format("未找到URL记录[%s]", fetchRecord.key));
        }
        LambdaUpdateWrapper<URLRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(URLRecord::getKey, fetchRecord.key);
        wrapper.set(URLRecord::getUpdateTime, System.currentTimeMillis());
        if (fetchRecord.fetchTime != null) wrapper.set(URLRecord::getFetchTime, fetchRecord.fetchTime);
        if (fetchRecord.status != null) wrapper.set(URLRecord::getStatus, fetchRecord.status);
        if (!StringUtils.isEmpty(fetchRecord.fetchContentURL)) {
            wrapper.set(URLRecord::getFetchContentURL, fetchRecord.fetchContentURL);
        }
        if (fetchRecord.structureMap != null && !fetchRecord.structureMap.isEmpty()) {
            wrapper.set(URLRecord::getStructureMap, fetchRecord.structureMap);
        }
        return urlMapper.update(null, wrapper) > 0;
    }

    @Override
    public Boolean updateQueueTime(URLRecord record) {
        if (record == null || StringUtils.isEmpty(record.key)) {
            logger.error("url record is null or key is missing");
            throw new RuntimeException("URL记录为空或key缺失");
        }
        if (get(record.key) == null) {
            logger.error("record is not found for key[{}]", record.key);
            throw new RuntimeException(String.format("未找到URL记录[%s]", record.key));
        }
        LambdaUpdateWrapper<URLRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(URLRecord::getKey, record.key);
        wrapper.set(URLRecord::getUpdateTime, System.currentTimeMillis());
        if (record.status != null) wrapper.set(URLRecord::getStatus, record.status);
        if (record.inQueueTime != null) wrapper.set(URLRecord::getInQueueTime, record.inQueueTime);
        if (record.outQueueTime != null) wrapper.set(URLRecord::getOutQueueTime, record.outQueueTime);
        return urlMapper.update(null, wrapper) > 0;
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
            throw new RuntimeException(String.format("未找到URL记录[%s]", key));
        }
        LambdaUpdateWrapper<URLRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(URLRecord::getKey, key).set(URLRecord::getStatus, status).
                set(URLRecord::getUpdateTime, System.currentTimeMillis());
        return urlMapper.update(null, wrapper) > 0;
    }



    @Override
    public URLRecord get(String key) {
        if (StringUtils.isEmpty(key)) {
            logger.error("url record key is empty");
            throw new RuntimeException("URL记录key为空");
        }
        return urlMapper.selectById(key);
    }

    @Override
    public Boolean delete(String key) {
        if (urlMapper.selectById(key) == null) {
            logger.error("url record[{}] is not found", key);
            throw new RuntimeException(String.format("URL记录[%s]不存在", key));
        }
        return urlMapper.deleteById(key) > 0;
    }

    @Override
    public Pager<URLRecord> search(URLSearchRequest searchRequest, int current, int size) {
        LambdaQueryWrapper<URLRecord> query = new LambdaQueryWrapper<>();
        query.orderByDesc(URLRecord::getCreateTime);
        if (searchRequest != null) {
            if (searchRequest.category != null) query.eq(URLRecord::getCategory, searchRequest.category);
            if (searchRequest.status != null) query.eq(URLRecord::getStatus, searchRequest.status);
            if (searchRequest.priority != null) query.eq(URLRecord::getPriority, searchRequest.priority);
            if (!StringUtils.isEmpty(searchRequest.jobId)) query.eq(URLRecord::getJobId, searchRequest.jobId);
            if (!StringUtils.isEmpty(searchRequest.url)) query.eq(URLRecord::getHash, DigestUtils.md5Hex(searchRequest.url));
            if (searchRequest.fetchTime != null && searchRequest.fetchTime.start != null) {
                if (searchRequest.fetchTime.includeLower) query.ge(URLRecord::getFetchTime, searchRequest.fetchTime.start);
                else query.gt(URLRecord::getFetchTime, searchRequest.fetchTime.start);
            }
            if (searchRequest.fetchTime != null && searchRequest.fetchTime.end != null) {
                if (searchRequest.fetchTime.includeUpper) query.le(URLRecord::getFetchTime, searchRequest.fetchTime.end);
                else query.lt(URLRecord::getFetchTime, searchRequest.fetchTime.end);
            }
        }
        IPage<URLRecord> page = urlMapper.selectPage(new Page<>(current, size), query);
        return Converter.convert(page);
    }
}
