package xin.manong.darwin.service.impl.ots;

import com.alibaba.fastjson.JSON;
import com.alicloud.openservices.tablestore.model.search.query.BoolQuery;
import com.alicloud.openservices.tablestore.model.search.query.Query;
import jakarta.annotation.Resource;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.RangeValue;
import xin.manong.darwin.common.model.URLGroupCount;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.config.ServiceConfig;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.weapon.aliyun.ots.*;
import xin.manong.weapon.base.record.KVRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OTS URL服务实现
 *
 * @author frankcl
 * @date 2023-03-21 20:22:27
 */
public class URLServiceImpl extends URLService {

    private static final Logger logger = LoggerFactory.getLogger(URLServiceImpl.class);

    private static final String KEY_KEY = "key";
    private static final String KEY_APP_ID = "app_id";
    private static final String KEY_JOB_ID = "job_id";
    private static final String KEY_PLAN_ID = "plan_id";
    private static final String KEY_HASH = "hash";
    private static final String KEY_REQUEST_HASH = "request_hash";
    private static final String KEY_HTTP_REQUEST = "http_request";
    private static final String KEY_HOST = "host";
    private static final String KEY_DOMAIN = "domain";
    private static final String KEY_CONCURRENCY_UNIT = "concurrency_unit";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PRIORITY = "priority";
    private static final String KEY_CONTENT_TYPE = "content_type";
    private static final String KEY_FETCH_TIME = "fetch_time";
    private static final String KEY_CREATE_TIME = "create_time";

    @Resource
    private ServiceConfig serviceConfig;
    @Resource
    private OTSClient otsClient;

    @Override
    public boolean add(URLRecord record) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_KEY, record.key);
        KVRecord kvRecord = otsClient.get(serviceConfig.ots.urlTable, keyMap);
        if (kvRecord != null) throw new IllegalStateException("URL记录已存在");
        kvRecord = OTSConverter.convertJavaObjectToKVRecord(record);
        return otsClient.put(serviceConfig.ots.urlTable, kvRecord, null) == OTSStatus.SUCCESS;
    }

    @Override
    public boolean updateContent(URLRecord record) {
        URLRecord prevRecord = get(record.key);
        if (prevRecord == null) return false;
        URLRecord updateRecord = newUpdateRecord();
        updateRecord.updateTime = System.currentTimeMillis();
        updateRecord.key = record.key;
        updateRecord.status = record.status;
        updateRecord.mediaType = record.mediaType;
        updateRecord.charset = record.charset;
        updateRecord.htmlCharset = record.htmlCharset;
        updateRecord.fetched = record.fetched;
        updateRecord.fetchTime = record.fetchTime;
        updateRecord.downTime = record.downTime;
        updateRecord.fetchContentURL = record.fetchContentURL;
        updateRecord.redirectURL = record.redirectURL;
        updateRecord.parentURL = record.parentURL;
        updateRecord.httpCode = record.httpCode;
        updateRecord.contentLength = record.contentLength;
        updateRecord.contentType = record.contentType;
        if (record.fieldMap != null && !record.fieldMap.isEmpty()) {
            updateRecord.fieldMap = record.fieldMap;
        }
        if (record.customMap != null && !record.customMap.isEmpty()) {
            updateRecord.customMap = record.customMap;
        }
        return update(updateRecord);
    }

    @Override
    public boolean updateQueueTime(URLRecord record) {
        URLRecord prevRecord = get(record.key);
        if (prevRecord == null) return false;
        URLRecord updateRecord = newUpdateRecord();
        updateRecord.updateTime = System.currentTimeMillis();
        updateRecord.key = record.key;
        updateRecord.pushTime = record.pushTime;
        updateRecord.popTime = record.popTime;
        updateRecord.status = record.status;
        return update(updateRecord);
    }

    @Override
    public boolean updateStatus(String key, int status) {
        URLRecord prevRecord = get(key);
        if (prevRecord == null) return false;
        URLRecord updateRecord = newUpdateRecord();
        updateRecord.updateTime = System.currentTimeMillis();
        updateRecord.key = key;
        updateRecord.status = status;
        return update(updateRecord);
    }

    @Override
    public URLRecord get(String key) {
        if (StringUtils.isEmpty(key)) throw new IllegalArgumentException("key为空");
        KVRecord kvRecord = getRecord(key);
        if (kvRecord == null) return null;
        return OTSConverter.convertKVRecordToJavaObject(kvRecord, URLRecord.class);
    }

    @Override
    public List<URLRecord> getChildren(String parentKey) {
        throw new UnsupportedOperationException("Unsupported this method");
    }

    @Override
    public boolean delete(String key) {
        URLRecord prevRecord = get(key);
        if (prevRecord == null) throw new NotFoundException("URL记录不存在");
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_KEY, key);
        OTSStatus status = otsClient.delete(serviceConfig.ots.urlTable, keyMap, null);
        return status == OTSStatus.SUCCESS;
    }

    @Override
    public boolean deleteByJob(String jobId) {
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.jobId = jobId;
        searchRequest.pageNum = 1;
        searchRequest.pageSize = 100;
        while (true) {
            Pager<URLRecord> pager = search(searchRequest);
            if (pager.records == null) break;
            for (URLRecord record : pager.records) {
                if (!delete(record.key)) logger.warn("Delete record:{} failed for job:{}", record.key, jobId);
            }
            if (pager.records.size() < pager.pageSize) break;
            searchRequest.pageNum++;
        }
        return true;
    }

    @Override
    public int deleteExpired(long expiredTime) {
        throw new UnsupportedOperationException("Unsupported this method");
    }

    @Override
    public Pager<URLRecord> search(URLSearchRequest searchRequest) {
        OTSSearchResponse response = searchURL(searchRequest);
        return Converter.convert(response, URLRecord.class, searchRequest.pageNum, searchRequest.pageSize);
    }

    @Override
    public long selectCount(URLSearchRequest searchRequest) {
        return searchURL(searchRequest).totalCount;
    }

    @Override
    public List<URLGroupCount> statusGroupCount(String jobId, RangeValue<Long> timeRange) {
        throw new UnsupportedOperationException("Unsupported this method");
    }

    @Override
    public List<URLGroupCount> contentGroupCount(String jobId, RangeValue<Long> timeRange) {
        throw new UnsupportedOperationException("Unsupported this method");
    }

    @Override
    public int fetchURLCount(RangeValue<Long> timeRange) {
        throw new UnsupportedOperationException("Unsupported this method");
    }

    @Override
    public int fetchHostCount(RangeValue<Long> timeRange) {
        throw new UnsupportedOperationException("Unsupported this method");
    }

    @Override
    public int fetchDomainCount(RangeValue<Long> timeRange) {
        throw new UnsupportedOperationException("Unsupported this method");
    }

    @Override
    public List<URLGroupCount> waitConcurrencyUnits(int n) {
        throw new UnsupportedOperationException("Unsupported this method");
    }

    @Override
    public List<URLGroupCount> queueWaitPriority() {
        throw new UnsupportedOperationException("Unsupported this method");
    }

    @Override
    public List<URLGroupCount> hostFetchCount(RangeValue<Long> timeRange, int n) {
        throw new UnsupportedOperationException("Unsupported this method");
    }

    /**
     * 搜索URL
     *
     * @param searchRequest 搜索请求
     * @return 搜索响应
     */
    private OTSSearchResponse searchURL(URLSearchRequest searchRequest) {
        searchRequest = prepareSearchRequest(searchRequest);
        int offset = (searchRequest.pageNum - 1) * searchRequest.pageSize;
        BoolQuery boolQuery = buildQuery(searchRequest);
        OTSSearchRequest request = new OTSSearchRequest.Builder().offset(offset).limit(searchRequest.pageSize).
                tableName(serviceConfig.ots.urlTable).indexName(serviceConfig.ots.urlIndexName).query(boolQuery).build();
        OTSSearchResponse response = otsClient.search(request);
        if (!response.status) throw new InternalServerErrorException("搜索URL失败");
        return response;
    }

    /**
     * 根据搜索请求构建OTS查询Query
     *
     * @param searchRequest 搜索请求
     * @return OTS查询Query
     */
    private BoolQuery buildQuery(URLSearchRequest searchRequest) {
        BoolQuery boolQuery = new BoolQuery();
        List<Query> queryList = new ArrayList<>();
        if (searchRequest.statusList != null && !searchRequest.statusList.isEmpty()) {
            queryList.add(SearchQueryBuilder.buildTermsQuery(KEY_STATUS, searchRequest.statusList));
        }
        if (searchRequest.priority != null) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_PRIORITY, searchRequest.priority));
        }
        if (searchRequest.contentType != null) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_CONTENT_TYPE, searchRequest.contentType));
        }
        if (searchRequest.appId != null) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_APP_ID, searchRequest.appId));
        }
        if (searchRequest.httpRequest != null) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_HTTP_REQUEST, searchRequest.httpRequest.name()));
        }
        if (StringUtils.isNotEmpty(searchRequest.jobId)) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_JOB_ID, searchRequest.jobId));
        }
        if (StringUtils.isNotEmpty(searchRequest.planId)) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_PLAN_ID, searchRequest.planId));
        }
        if (StringUtils.isNotEmpty(searchRequest.host)) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_HOST, searchRequest.host));
        }
        if (StringUtils.isNotEmpty(searchRequest.domain)) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_DOMAIN, searchRequest.domain));
        }
        if (StringUtils.isNotEmpty(searchRequest.concurrencyUnit)) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_CONCURRENCY_UNIT, searchRequest.concurrencyUnit));
        }
        if (StringUtils.isNotEmpty(searchRequest.url)) {
            if (searchRequest.requestBody != null && !searchRequest.requestBody.isEmpty()) {
                queryList.add(SearchQueryBuilder.buildTermQuery(KEY_REQUEST_HASH, String.format("%s_%s",
                        searchRequest.url, JSON.toJSONString(searchRequest.requestBody))));
            } else {
                queryList.add(SearchQueryBuilder.buildTermQuery(KEY_HASH, DigestUtils.md5Hex(searchRequest.url)));
            }

        }
        if (searchRequest.fetchTimeRange != null) {
            queryList.add(SearchQueryBuilder.buildRangeQuery(KEY_FETCH_TIME, searchRequest.fetchTimeRange));
        }
        if (searchRequest.createTimeRange != null) {
            queryList.add(SearchQueryBuilder.buildRangeQuery(KEY_CREATE_TIME, searchRequest.createTimeRange));
        }
        if (!queryList.isEmpty()) boolQuery.setFilterQueries(queryList);
        return boolQuery;
    }

    /**
     * 新建更新记录
     * 设置不需要更新字段为null
     *
     * @return 更新URL记录
     */
    private URLRecord newUpdateRecord() {
        URLRecord record = new URLRecord();
        record.createTime = null;
        record.customMap = null;
        record.fieldMap = null;
        record.headers = null;
        record.depth = null;
        return record;
    }

    /**
     * 更新数据
     *
     * @param record 更新数据
     * @return 更新成功返回true，否则返回false
     */
    private boolean update(URLRecord record) {
        KVRecord kvRecord = OTSConverter.convertJavaObjectToKVRecord(record);
        OTSStatus status = otsClient.update(serviceConfig.ots.urlTable, kvRecord, null);
        return status == OTSStatus.SUCCESS;
    }

    /**
     * 获取数据
     *
     * @param key URL key
     * @return 存在返回数据，否则返回null
     */
    private KVRecord getRecord(String key) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_KEY, key);
        KVRecord kvRecord = otsClient.get(serviceConfig.ots.urlTable, keyMap);
        if (kvRecord == null) logger.warn("Record is not found for key:{}", key);
        return kvRecord;
    }
}
