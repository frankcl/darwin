package xin.manong.darwin.common.model.handler;

import com.alibaba.fastjson.TypeReference;
import xin.manong.darwin.common.model.URLRecord;

import java.util.List;

/**
 * JSON URL列表数据转化器
 *
 * @author frankcl
 * @date 2023-03-15 11:59:24
 */
public class JSONListURLRecordTypeHandler extends AbstractJSONListTypeHandler<URLRecord> {

    @Override
    protected TypeReference<List<URLRecord>> specificType() {
        return new TypeReference<>() {};
    }
}
