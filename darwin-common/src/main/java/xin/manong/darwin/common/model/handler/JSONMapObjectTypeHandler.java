package xin.manong.darwin.common.model.handler;

import com.alibaba.fastjson.TypeReference;

import java.util.Map;

/**
 * JSON对象字段数据转化器
 *
 * @author frankcl
 * @date 2023-03-15 11:59:24
 */
public class JSONMapObjectTypeHandler extends AbstractJSONMapTypeHandler<Object> {

    @Override
    protected TypeReference<Map<String, Object>> specificType() {
        return new TypeReference<Map<String, Object>>() {};
    }
}
