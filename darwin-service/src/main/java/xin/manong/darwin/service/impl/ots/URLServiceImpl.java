package xin.manong.darwin.service.impl.ots;

import com.alicloud.openservices.tablestore.model.search.query.BoolQuery;
import com.alicloud.openservices.tablestore.model.search.query.Query;
import jakarta.annotation.Resource;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.URLGroupCount;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.config.CacheConfig;
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
    private static final String KEY_STATUS = "status";
    private static final String KEY_PRIORITY = "priority";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_FETCH_TIME = "fetch_time";
    private static final String KEY_CREATE_TIME = "create_time";

    @Resource
    protected ServiceConfig serviceConfig;
    @Resource
    protected OTSClient otsClient;

    @Autowired
    public URLServiceImpl(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

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
    public boolean updateContent(URLRecord contentRecord) {
        KVRecord kvRecord = getRecord(contentRecord.key);
        if (kvRecord == null) return false;
        URLRecord updateRecord = new URLRecord();
        initRecord(updateRecord);
        updateRecord.updateTime = System.currentTimeMillis();
        updateRecord.key = contentRecord.key;
        updateRecord.status = contentRecord.status;
        updateRecord.mimeType = contentRecord.mimeType;
        updateRecord.subMimeType = contentRecord.subMimeType;
        updateRecord.fetchTime = contentRecord.fetchTime;
        updateRecord.fetchContentURL = contentRecord.fetchContentURL;
        updateRecord.httpCode = contentRecord.httpCode;
        if (contentRecord.fieldMap != null && !contentRecord.fieldMap.isEmpty()) {
            updateRecord.fieldMap = contentRecord.fieldMap;
        }
        if (contentRecord.userDefinedMap != null && !contentRecord.userDefinedMap.isEmpty()) {
            updateRecord.userDefinedMap = contentRecord.userDefinedMap;
        }
        return update(contentRecord, updateRecord);
    }

    @Override
    public boolean updateQueueTime(URLRecord record) {
        KVRecord kvRecord = getRecord(record.key);
        if (kvRecord == null) return false;
        URLRecord updateRecord = new URLRecord();
        initRecord(updateRecord);
        updateRecord.updateTime = System.currentTimeMillis();
        updateRecord.key = record.key;
        updateRecord.pushTime = record.pushTime;
        updateRecord.popTime = record.popTime;
        updateRecord.status = record.status;
        return update(record, updateRecord);
    }

    @Override
    public boolean updateStatus(String key, int status) {
        KVRecord kvRecord = getRecord(key);
        if (kvRecord == null) return false;
        URLRecord updateRecord = new URLRecord();
        initRecord(updateRecord);
        updateRecord.updateTime = System.currentTimeMillis();
        updateRecord.key = key;
        updateRecord.status = status;
        kvRecord = OTSConverter.convertJavaObjectToKVRecord(updateRecord);
        return otsClient.update(serviceConfig.ots.urlTable, kvRecord, null) == OTSStatus.SUCCESS;
    }

    @Override
    public URLRecord get(String key) {
        if (StringUtils.isEmpty(key)) throw new IllegalArgumentException("URL记录key为空");
        KVRecord kvRecord = getRecord(key);
        if (kvRecord == null) return null;
        return OTSConverter.convertKVRecordToJavaObject(kvRecord, URLRecord.class);
    }

    @Override
    public boolean delete(String key) {
        KVRecord kvRecord = getRecord(key);
        if (kvRecord == null) throw new NotFoundException("URL记录不存在");
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_KEY, key);
        return otsClient.delete(serviceConfig.ots.urlTable, keyMap, null) == OTSStatus.SUCCESS;
    }

    @Override
    public Pager<URLRecord> search(URLSearchRequest searchRequest) {
        OTSSearchResponse response = searchURL(searchRequest);
        return Converter.convert(response, URLRecord.class, searchRequest.current, searchRequest.size);
    }

    @Override
    public long computeCount(URLSearchRequest searchRequest) {
        return searchURL(searchRequest).totalCount;
    }

    @Override
    public List<URLGroupCount> bucketCountGroupByStatus(String jobId) {
        throw new UnsupportedOperationException("unsupported bucket count group by status");
    }

    /**
     * 搜索URL
     *
     * @param searchRequest 搜索请求
     * @return 搜索响应
     */
    private OTSSearchResponse searchURL(URLSearchRequest searchRequest) {
        searchRequest = prepareSearchRequest(searchRequest);
        int offset = (searchRequest.current - 1) * searchRequest.size;
        BoolQuery boolQuery = buildQuery(searchRequest);
        OTSSearchRequest request = new OTSSearchRequest.Builder().offset(offset).limit(searchRequest.size).
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
        if (searchRequest.category != null) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_CATEGORY, searchRequest.category));
        }
        if (searchRequest.appId != null) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_APP_ID, searchRequest.appId));
        }
        if (StringUtils.isNotEmpty(searchRequest.jobId)) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_JOB_ID, searchRequest.jobId));
        }
        if (StringUtils.isNotEmpty(searchRequest.planId)) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_PLAN_ID, searchRequest.planId));
        }
        if (StringUtils.isNotEmpty(searchRequest.url)) {
            queryList.add(SearchQueryBuilder.buildTermQuery(KEY_HASH, DigestUtils.md5Hex(searchRequest.url)));
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
     * 初始化更新URL记录
     * 设置不需要更新字段为null
     *
     * @param record 更新URL记录
     */
    private void initRecord(URLRecord record) {
        record.createTime = null;
        record.userDefinedMap = null;
        record.fieldMap = null;
        record.headers = null;
        record.depth = null;
    }

    /**
     * 更新数据
     *
     * @param record 原始数据
     * @param updateRecord 更新数据
     * @return 更新成功返回true，否则返回false
     */
    private boolean update(URLRecord record, URLRecord updateRecord) {
        KVRecord kvRecord = OTSConverter.convertJavaObjectToKVRecord(updateRecord);
        OTSStatus status = otsClient.update(serviceConfig.ots.urlTable, kvRecord, null);
        if (status == OTSStatus.SUCCESS && !StringUtils.isEmpty(record.url)) recordCache.invalidate(record.url);
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
        if (kvRecord == null) logger.warn("url record is not found for key[{}]", key);
        return kvRecord;
    }
}
