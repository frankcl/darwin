package xin.manong.darwin.service.convert;

import com.baomidou.mybatisplus.core.metadata.IPage;
import xin.manong.darwin.common.model.Pager;
import xin.manong.weapon.aliyun.ots.OTSConverter;
import xin.manong.weapon.aliyun.ots.OTSSearchResponse;
import xin.manong.weapon.base.record.KVRecord;
import xin.manong.weapon.base.record.KVRecords;

import java.util.ArrayList;

/**
 * 数据转换器
 *
 * @author frankcl
 * @date 2023-03-15 17:08:50
 */
public class Converter {

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
