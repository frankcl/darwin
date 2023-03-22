package xin.manong.darwin.service.impl.ots;

import xin.manong.darwin.common.model.Job;
import xin.manong.darwin.common.model.Pager;
import xin.manong.weapon.aliyun.ots.OTSConverter;
import xin.manong.weapon.aliyun.ots.OTSSearchResponse;
import xin.manong.weapon.base.record.KVRecord;
import xin.manong.weapon.base.record.KVRecords;

import java.util.ArrayList;

/**
 * 搜索结果转换
 *
 * @author frankcl
 * @date 2023-03-22 14:58:46
 */
public class SearchResponseConverter {

    /**
     * 转换搜索结果为分页结果
     *
     * @param current 当前页码
     * @param size 每页数量
     * @param response 搜索响应
     * @param clazz 数据类型
     * @return 分页结果
     */
    public static <T> Pager<T> convertSearchResponseToPager(int current, int size, OTSSearchResponse response,
                                                            Class<T> clazz) {
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
