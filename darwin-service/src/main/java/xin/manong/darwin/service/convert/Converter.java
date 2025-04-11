package xin.manong.darwin.service.convert;

import com.baomidou.mybatisplus.core.metadata.IPage;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.*;
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
        record.category = seedRecord.category;
        record.concurrentLevel = seedRecord.concurrentLevel;
        record.scope = seedRecord.scope;
        record.planId = seedRecord.planId;
        record.headers = seedRecord.headers == null ? new HashMap<>() : seedRecord.headers;
        record.userDefinedMap = seedRecord.userDefinedMap == null ? new HashMap<>() : seedRecord.userDefinedMap;
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
        pager.current = page.getCurrent();
        pager.size = page.getSize();
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
        job.allowRepeat = plan.allowRepeat != null && plan.allowRepeat;
        job.priority = plan.priority == null ? Constants.PRIORITY_NORMAL : plan.priority;
        job.status = true;
        job.fetchMethod = plan.fetchMethod;
        job.jobId = RandomID.build();
        job.name = String.format("%s_%s", plan.name, CommonUtil.timeToString(System.currentTimeMillis(), DATE_TIME_FORMAT));
        return job;
    }

    /**
     * 转换搜索结果为分页结果
     *
     * @param response 搜索响应
     * @param clazz 数据类型
     * @param current 当前页码
     * @param size 每页数量
     * @return 分页结果
     */
    public static <T> Pager<T> convert(OTSSearchResponse response, Class<T> clazz,
                                       int current, int size) {
        Pager<T> pager = new Pager<>();
        pager.current = (long) current;
        pager.size = (long) size;
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
