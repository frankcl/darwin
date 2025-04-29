package xin.manong.darwin.common.model.handler;

import com.alibaba.fastjson.TypeReference;
import xin.manong.darwin.common.model.DashboardValue;

import java.util.List;

/**
 * JSON数据统计值列表转化器
 *
 * @author frankcl
 * @date 2023-03-15 11:59:24
 */
public class JSONDashboardValueListHandler extends JSONGenericListHandler<DashboardValue<?>> {

    @Override
    protected TypeReference<List<DashboardValue<?>>> specificType() {
        return new TypeReference<>() {};
    }
}
