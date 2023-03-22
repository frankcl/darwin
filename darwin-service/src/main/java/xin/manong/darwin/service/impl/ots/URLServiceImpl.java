package xin.manong.darwin.service.impl.ots;

import com.alicloud.openservices.tablestore.model.search.query.BoolQuery;
import com.alicloud.openservices.tablestore.model.search.query.Query;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.FetchRecord;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.config.ServiceConfig;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.iface.URLService;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.weapon.aliyun.ots.*;
import xin.manong.weapon.base.record.KVRecord;

import javax.annotation.Resource;
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
public class URLServiceImpl implements URLService {

    private static final Logger logger = LoggerFactory.getLogger(URLServiceImpl.class);

    private static final String KEY_KEY = "key";
    private static final String KEY_JOB_ID = "job_id";
    private static final String KEY_HASH = "hash";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PRIORITY = "priority";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_FETCH_TIME = "fetch_time";

    @Resource
    protected ServiceConfig serviceConfig;
    @Resource
    protected OTSClient otsClient;

    @Override
    public Boolean add(URLRecord record) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_KEY, record.key);
        KVRecord kvRecord = otsClient.get(serviceConfig.urlTable, keyMap);
        if (kvRecord != null) {
            logger.error("url record has existed for key[{}]", record.key);
            throw new RuntimeException(String.format("URL记录[%s]已存在", record.key));
        }
        kvRecord = OTSConverter.convertJavaObjectToKVRecord(record);
        return otsClient.put(serviceConfig.urlTable, kvRecord, null) == OTSStatus.SUCCESS;
    }

    @Override
    public Boolean updateWithFetchRecord(FetchRecord fetchRecord) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_KEY, fetchRecord.key);
        KVRecord kvRecord = otsClient.get(serviceConfig.urlTable, keyMap);
        if (kvRecord == null) {
            logger.error("url record[{}] is not found", fetchRecord.key);
            throw new RuntimeException(String.format("URL记录[%s]不存在", fetchRecord.key));
        }
        URLRecord updateRecord = new URLRecord();
        updateRecord.createTime = null;
        updateRecord.userDefinedMap = null;
        updateRecord.updateTime = System.currentTimeMillis();
        updateRecord.key = fetchRecord.key;
        updateRecord.status = fetchRecord.status;
        updateRecord.fetchTime = fetchRecord.fetchTime;
        updateRecord.fetchContentURL = fetchRecord.fetchContentURL;
        updateRecord.structureMap = fetchRecord.structureMap;
        kvRecord = OTSConverter.convertJavaObjectToKVRecord(updateRecord);
        return otsClient.update(serviceConfig.urlTable, kvRecord, null) == OTSStatus.SUCCESS;
    }

    @Override
    public Boolean updateQueueTime(URLRecord record) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_KEY, record.key);
        KVRecord kvRecord = otsClient.get(serviceConfig.urlTable, keyMap);
        if (kvRecord == null) {
            logger.error("url record[{}] is not found", record.key);
            throw new RuntimeException(String.format("URL记录[%s]不存在", record.key));
        }
        URLRecord updateRecord = new URLRecord();
        updateRecord.createTime = null;
        updateRecord.userDefinedMap = null;
        updateRecord.updateTime = System.currentTimeMillis();
        updateRecord.key = record.key;
        updateRecord.inQueueTime = record.inQueueTime;
        updateRecord.outQueueTime = record.outQueueTime;
        updateRecord.status = record.status;
        kvRecord = OTSConverter.convertJavaObjectToKVRecord(updateRecord);
        return otsClient.update(serviceConfig.urlTable, kvRecord, null) == OTSStatus.SUCCESS;
    }

    @Override
    public Boolean updateStatus(String key, int status) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_KEY, key);
        KVRecord kvRecord = otsClient.get(serviceConfig.urlTable, keyMap);
        if (kvRecord == null) {
            logger.error("url record[{}] is not found", key);
            throw new RuntimeException(String.format("URL记录[%s]不存在", key));
        }
        URLRecord updateRecord = new URLRecord();
        updateRecord.createTime = null;
        updateRecord.userDefinedMap = null;
        updateRecord.updateTime = System.currentTimeMillis();
        updateRecord.key = key;
        updateRecord.status = status;
        kvRecord = OTSConverter.convertJavaObjectToKVRecord(updateRecord);
        return otsClient.update(serviceConfig.urlTable, kvRecord, null) == OTSStatus.SUCCESS;
    }

    @Override
    public URLRecord get(String key) {
        if (StringUtils.isEmpty(key)) {
            logger.error("url key is empty");
            throw new RuntimeException("URL记录key为空");
        }
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_KEY, key);
        KVRecord kvRecord = otsClient.get(serviceConfig.getJobTable(), keyMap);
        if (kvRecord == null) return null;
        return OTSConverter.convertKVRecordToJavaObject(kvRecord, URLRecord.class);
    }

    @Override
    public Boolean delete(String key) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_KEY, key);
        KVRecord kvRecord = otsClient.get(serviceConfig.jobTable, keyMap);
        if (kvRecord == null) {
            logger.error("url record[{}] is not found", key);
            throw new RuntimeException(String.format("URL记录[%s]不存在", key));
        }
        return otsClient.delete(serviceConfig.jobTable, keyMap, null) == OTSStatus.SUCCESS;
    }

    @Override
    public Pager<URLRecord> search(URLSearchRequest searchRequest, int current, int size) {
        if (current < 1) current = 1;
        if (size <= 0) size = 20;
        int offset = (current - 1) * size;
        BoolQuery boolQuery = new BoolQuery();
        if (searchRequest != null) {
            List<Query> queryList = new ArrayList<>();
            if (searchRequest.status != null) {
                queryList.add(SearchQueryBuilder.buildTermQuery(KEY_STATUS, searchRequest.status));
            }
            if (searchRequest.priority != null) {
                queryList.add(SearchQueryBuilder.buildTermQuery(KEY_PRIORITY, searchRequest.priority));
            }
            if (searchRequest.category != null) {
                queryList.add(SearchQueryBuilder.buildTermQuery(KEY_CATEGORY, searchRequest.category));
            }
            if (!StringUtils.isEmpty(searchRequest.jobId)) {
                queryList.add(SearchQueryBuilder.buildTermQuery(KEY_JOB_ID, searchRequest.jobId));
            }
            if (!StringUtils.isEmpty(searchRequest.url)) {
                queryList.add(SearchQueryBuilder.buildTermQuery(KEY_HASH, DigestUtils.md5Hex(searchRequest.url)));
            }
            if (searchRequest.fetchTime != null) {
                queryList.add(SearchQueryBuilder.buildRangeQuery(KEY_FETCH_TIME, searchRequest.fetchTime));
            }
            boolQuery.setFilterQueries(queryList);
        }
        OTSSearchRequest request = new OTSSearchRequest.Builder().offset(offset).limit(size).
                tableName(serviceConfig.urlTable).indexName(serviceConfig.urlIndexName).query(boolQuery).build();
        OTSSearchResponse response = otsClient.search(request);
        if (!response.status) {
            logger.error("search url record failed from table[{}] and index[{}]",
                    serviceConfig.urlTable, serviceConfig.urlIndexName);
            throw new RuntimeException("搜索URL记录失败");
        }
        return Converter.convert(response, URLRecord.class, current, size);
    }
}
