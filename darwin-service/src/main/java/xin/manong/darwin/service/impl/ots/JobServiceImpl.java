package xin.manong.darwin.service.impl.ots;

import com.alicloud.openservices.tablestore.model.ColumnValue;
import com.alicloud.openservices.tablestore.model.search.query.BoolQuery;
import com.alicloud.openservices.tablestore.model.search.query.Query;
import com.alicloud.openservices.tablestore.model.search.query.TermQuery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.darwin.service.config.ServiceConfig;
import xin.manong.darwin.service.convert.Converter;
import xin.manong.darwin.service.iface.JobService;
import xin.manong.darwin.service.request.JobSearchRequest;
import xin.manong.weapon.aliyun.ots.*;
import xin.manong.weapon.base.record.KVRecord;

import javax.annotation.Resource;
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

    private static final Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

    private static final String KEY_JOB_ID = "job_id";
    private static final String KEY_PLAN_ID = "plan_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PRIORITY = "priority";

    @Resource
    protected ServiceConfig serviceConfig;
    @Resource
    protected OTSClient otsClient;

    @Override
    public Job get(String jobId) {
        if (StringUtils.isEmpty(jobId)) {
            logger.error("job id is empty");
            throw new RuntimeException("任务ID为空");
        }
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_JOB_ID, jobId);
        KVRecord kvRecord = otsClient.get(serviceConfig.getJobTable(), keyMap);
        if (kvRecord == null) return null;
        return OTSConverter.convertKVRecordToJavaObject(kvRecord, Job.class);
    }

    @Override
    public Boolean add(Job job) {
        TermQuery termQuery = new TermQuery();
        termQuery.setFieldName(KEY_NAME);
        termQuery.setTerm(ColumnValue.fromString(job.name));
        OTSSearchRequest request = new OTSSearchRequest.Builder().indexName(serviceConfig.jobTable).
                tableName(serviceConfig.jobIndexName).query(termQuery).build();
        OTSSearchResponse response = otsClient.search(request);
        if (!response.status) {
            logger.error("search failed for table[{}] and index[{}]",
                    serviceConfig.jobTable, serviceConfig.jobIndexName);
            throw new RuntimeException(String.format("搜索OTS表[%s]及索引[%s]失败",
                    serviceConfig.jobTable, serviceConfig.jobIndexName));
        }
        if (response.records.getRecordCount() > 0) {
            logger.error("job has existed for same name[%s]", job.name);
            throw new RuntimeException(String.format("同名任务[%s]存在", job.name));
        }
        KVRecord kvRecord = OTSConverter.convertJavaObjectToKVRecord(job);
        return otsClient.put(serviceConfig.jobTable, kvRecord, null) == OTSStatus.SUCCESS;
    }

    @Override
    public Boolean update(Job job) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_JOB_ID, job.jobId);
        KVRecord kvRecord = otsClient.get(serviceConfig.jobTable, keyMap);
        if (kvRecord == null) {
            logger.error("job[{}] is not found", job.jobId);
            return false;
        }
        job.updateTime = System.currentTimeMillis();
        kvRecord = OTSConverter.convertJavaObjectToKVRecord(job);
        return otsClient.update(serviceConfig.jobTable, kvRecord, null) == OTSStatus.SUCCESS;
    }

    @Override
    public Boolean delete(String jobId) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(KEY_JOB_ID, jobId);
        KVRecord kvRecord = otsClient.get(serviceConfig.jobTable, keyMap);
        if (kvRecord == null) {
            logger.error("job[{}] is not found", jobId);
            return false;
        }
        return otsClient.delete(serviceConfig.jobTable, keyMap, null) == OTSStatus.SUCCESS;
    }

    @Override
    public Pager<Job> search(JobSearchRequest searchRequest, int current, int size) {
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
            if (!StringUtils.isEmpty(searchRequest.planId)) {
                queryList.add(SearchQueryBuilder.buildTermQuery(KEY_PLAN_ID, searchRequest.planId));
            }
            if (!StringUtils.isEmpty(searchRequest.name)) {
                queryList.add(SearchQueryBuilder.buildMatchPhraseQuery(KEY_NAME, searchRequest.name));
            }
            boolQuery.setFilterQueries(queryList);
        }
        OTSSearchRequest request = new OTSSearchRequest.Builder().offset(offset).limit(size).
                tableName(serviceConfig.jobTable).indexName(serviceConfig.jobIndexName).query(boolQuery).build();
        OTSSearchResponse response = otsClient.search(request);
        if (!response.status) {
            logger.error("search job failed from table[{}] and index[{}]",
                    serviceConfig.jobTable, serviceConfig.jobIndexName);
            throw new RuntimeException("搜索任务失败");
        }
        return Converter.convert(response, Job.class, current, size);
    }
}
