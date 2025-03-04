package xin.manong.darwin.common.model.handler;

import com.alibaba.fastjson.TypeReference;

import java.util.List;

/**
 * JSON整形列表数据转化器
 *
 * @author frankcl
 * @date 2023-03-15 11:59:24
 */
public class JSONListIntegerTypeHandler extends AbstractJSONListTypeHandler<Integer> {

    @Override
    protected TypeReference<List<Integer>> specificType() {
        return new TypeReference<>() {};
    }
}
