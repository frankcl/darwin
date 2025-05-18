package xin.manong.darwin.common.model.handler;

import com.alibaba.fastjson.TypeReference;
import xin.manong.darwin.common.model.TrendValue;

import java.util.List;

/**
 * JSON数据统计值列表转化器
 *
 * @author frankcl
 * @date 2023-03-15 11:59:24
 */
public class JSONDashboardValueListHandler extends JSONGenericListHandler<TrendValue<?>> {

    @Override
    protected TypeReference<List<TrendValue<?>>> specificType() {
        return new TypeReference<>() {};
    }
}
