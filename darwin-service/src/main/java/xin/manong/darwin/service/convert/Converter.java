package xin.manong.darwin.service.convert;

import com.baomidou.mybatisplus.core.metadata.IPage;
import xin.manong.darwin.common.model.*;
import xin.manong.darwin.service.lineage.Node;
import xin.manong.weapon.aliyun.ots.OTSConverter;
import xin.manong.weapon.aliyun.ots.OTSSearchResponse;
import xin.manong.weapon.base.record.KVRecord;
import xin.manong.weapon.base.record.KVRecords;
import xin.manong.weapon.base.util.CommonUtil;
import xin.manong.weapon.base.util.RandomID;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 数据转换器
 *
 * @author frankcl
 * @date 2023-03-15 17:08:50
 */
public class Converter {

    private static final String DATE_TIME_FORMAT = "yyyy_MM_dd_HH_mm_ss";

    /**
     * 转换URLRecord为Node
     *
     * @param record 数据
     * @return 血统节点
     */
    public static Node convert(URLRecord record) {
        Node node = new Node(record.key, record.url);
        node.setParentKey(record.parentKey);
        return node;
    }

    /**
     * 转换种子记录为URL记录
     *
     * @param seedRecord 种子记录
     * @return URL记录
     */
    public static URLRecord convert(SeedRecord seedRecord) {
        URLRecord record = new URLRecord(seedRecord.url);
        record.timeout = seedRecord.timeout;
        record.priority = seedRecord.priority;
        record.fetchMethod = seedRecord.fetchMethod;
        record.linkScope = seedRecord.linkScope;
        record.planId = seedRecord.planId;
        record.allowDispatch = seedRecord.allowDispatch;
        record.allowRepeat = true;
        record.normalize = seedRecord.normalize;
        record.httpRequest = seedRecord.httpRequest;
        record.postMediaType = seedRecord.postMediaType;
        record.requestHash = seedRecord.requestHash;
        record.requestBody = seedRecord.requestBody == null ? new HashMap<>() : seedRecord.requestBody;
        record.headers = seedRecord.headers == null ? new HashMap<>() : seedRecord.headers;
        record.customMap = seedRecord.customMap == null ? new HashMap<>() : seedRecord.customMap;
        return record;
    }

    /**
     * 转化数据库分页信息为通用分页信息
     *
     * @param page 数据库分页信息
     * @return 通用分页信息
     * @param <T> 数据类型
     */
    public static <T> Pager<T> convert(IPage<T> page) {
        if (page == null) return null;
        Pager<T> pager = new Pager<>();
        pager.records = page.getRecords();
        pager.pageNum = page.getCurrent();
        pager.pageSize = page.getSize();
        pager.total = page.getTotal();
        return pager;
    }

    /**
     * 转换计划为任务
     *
     * @param plan 计划
     * @return 任务
     */
    public static Job convert(Plan plan) {
        Job job = new Job();
        job.createTime = System.currentTimeMillis();
        job.planId = plan.planId;
        job.appId = plan.appId;
        job.status = true;
        job.jobId = RandomID.build();
        job.name = String.format("%s_%s", plan.name, CommonUtil.timeToString(System.currentTimeMillis(), DATE_TIME_FORMAT));
        return job;
    }

    /**
     * 转换搜索结果为分页结果
     *
     * @param response 搜索响应
     * @param clazz 数据类型
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    public static <T> Pager<T> convert(OTSSearchResponse response, Class<T> clazz,
                                       int pageNum, int pageSize) {
        Pager<T> pager = new Pager<>();
        pager.pageNum = (long) pageNum;
        pager.pageSize = (long) pageSize;
        pager.total = response.totalCount;
        pager.records = new ArrayList<>();
        KVRecords kvRecords = response.records;
        for (int i = 0; i < kvRecords.getRecordCount(); i++) {
            KVRecord kvRecord = kvRecords.getRecord(i);
            T record = OTSConverter.convertKVRecordToJavaObject(kvRecord, clazz);
            pager.records.add(record);
        }
        return pager;
    }
}
