package xin.manong.darwin.service.impl.ots;

import com.alicloud.openservices.tablestore.model.search.query.BoolQuery;
import com.alicloud.openservices.tablestore.model.search.query.Query;
import jakarta.annotation.Resource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.common.model.URLRecord;
import xin.manong.darwin.service.config.CacheConfig;
import xin.manong.darwin.service.config.ServiceConfig;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.request.JobSearchRequest;
import xin.manong.darwin.service.request.URLSearchRequest;
import xin.manong.weapon.aliyun.ots.*;
import xin.manong.weapon.base.record.KVRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OTS任务服务实现
 *
 * @author frankcl
 * @date 2023-03-21 20:23:03
 */
public class JobServiceImpl extends JobService {

    private static final String KEY_JOB_ID = "job_id";
    private static final String KEY_PLAN_ID = "plan_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PRIORITY = "priority";
    private static final String KEY_CREATE_TIME = "create_time";

    @Resource
    protected ServiceConfig serviceConfig;
    @Resource
    protected OTSClient otsClient;

    @Autowired
    public JobServiceImpl(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    public Job get(String jobId) {
        if (StringUtils.isEmpty(jobId)) throw new BadRequestException("任务ID为空");
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_JOB_ID, jobId);
        KVRecord kvRecord = otsClient.get(serviceConfig.ots.jobTable, keyMap);
        if (kvRecord == null) return null;
        return OTSConverter.convertKVRecordToJavaObject(kvRecord, Job.class);
    }

    @Override
    public boolean add(Job job) {
        BoolQuery boolQuery = new BoolQuery();
        List<Query> queryList = new ArrayList<>();
        queryList.add(SearchQueryBuilder.buildTermQuery(KEY_NAME, job.name));
        queryList.add(SearchQueryBuilder.buildTermQuery(KEY_PLAN_ID, job.planId));
        boolQuery.setFilterQueries(queryList);
        OTSSearchRequest request = new OTSSearchRequest.Builder().indexName(serviceConfig.ots.jobTable).
                tableName(serviceConfig.ots.jobIndexName).query(boolQuery).build();
        OTSSearchResponse response = otsClient.search(request);
        if (!response.status) throw new InternalServerErrorException("搜索OTS异常");
        if (response.records.getRecordCount() > 0) throw new IllegalStateException("同名任务存在");
        KVRecord kvRecord = OTSConverter.convertJavaObjectToKVRecord(job);
        return otsClient.put(serviceConfig.ots.jobTable, kvRecord, null) == OTSStatus.SUCCESS;
    }

    @Override
    public boolean update(Job job) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_JOB_ID, job.jobId);
        KVRecord kvRecord = otsClient.get(serviceConfig.ots.jobTable, keyMap);
        if (kvRecord == null) throw new NotFoundException("任务不存在");
        job.updateTime = System.currentTimeMillis();
        kvRecord = OTSConverter.convertJavaObjectToKVRecord(job);
        OTSStatus status = otsClient.update(serviceConfig.ots.jobTable, kvRecord, null);
        if (status == OTSStatus.SUCCESS) jobCache.invalidate(job.jobId);
        return status == OTSStatus.SUCCESS;
    }

    @Override
    public boolean delete(String jobId) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_JOB_ID, jobId);
        KVRecord kvRecord = otsClient.get(serviceConfig.ots.jobTable, keyMap);
        if (kvRecord == null) throw new NotFoundException("任务不存在");
        URLSearchRequest searchRequest = new URLSearchRequest();
        searchRequest.jobId = jobId;
        Pager<URLRecord> pager = urlService.search(searchRequest);
        if (pager.total > 0) throw new IllegalStateException("任务URL记录不为空");
        OTSStatus status = otsClient.delete(serviceConfig.ots.jobTable, keyMap, null);
        if (status == OTSStatus.SUCCESS) jobCache.invalidate(jobId);
        return status == OTSStatus.SUCCESS;
    }

    @Override
    public Pager<Job> search(JobSearchRequest searchRequest) {
        searchRequest = prepareSearchRequest(searchRequest);
        int offset = (searchRequest.current - 1) * searchRequest.size;
        BoolQuery boolQuery = new BoolQuery();
        List<Query> queryList = new ArrayList<>();
        if (searchRequest.status != null) queryList.add(SearchQueryBuilder.buildTermQuery(KEY_STATUS, searchRequest.status));
        if (searchRequest.priority != null) queryList.add(SearchQueryBuilder.buildTermQuery(KEY_PRIORITY, searchRequest.priority));
        if (!StringUtils.isEmpty(searchRequest.planId)) queryList.add(SearchQueryBuilder.buildTermQuery(KEY_PLAN_ID, searchRequest.planId));
        if (!StringUtils.isEmpty(searchRequest.name)) queryList.add(SearchQueryBuilder.buildMatchPhraseQuery(KEY_NAME, searchRequest.name));
        if (searchRequest.createTimeRange != null) queryList.add(SearchQueryBuilder.buildRangeQuery(KEY_CREATE_TIME, searchRequest.createTimeRange));
        if (!queryList.isEmpty()) boolQuery.setFilterQueries(queryList);
        OTSSearchRequest request = new OTSSearchRequest.Builder().offset(offset).limit(searchRequest.size).
                tableName(serviceConfig.ots.jobTable).indexName(serviceConfig.ots.jobIndexName).query(boolQuery).build();
        OTSSearchResponse response = otsClient.search(request);
        if (!response.status) throw new InternalServerErrorException("搜索OTS失败");
        return Converter.convert(response, Job.class, searchRequest.current, searchRequest.size);
    }
}
