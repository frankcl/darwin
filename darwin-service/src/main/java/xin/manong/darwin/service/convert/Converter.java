package xin.manong.darwin.service.convert;

import com.baomidou.mybatisplus.core.metadata.IPage;
import xin.manong.darwin.common.model.Pager;

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
}
