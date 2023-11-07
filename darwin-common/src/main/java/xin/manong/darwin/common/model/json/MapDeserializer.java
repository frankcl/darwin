package xin.manong.darwin.common.model.json;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * hash map反序列化
 * 解决hash map反序列化为json object问题
 *
 * @author frankcl
 * @date 2023-11-06 20:47:57
 */
public class MapDeserializer implements ObjectReader<Map<String, Object>> {

    @Override
    public Map<String, Object> readObject(JSONReader jsonReader, Type type, Object o, long l) {
        Map<String, Object> map = jsonReader.readObject();
        return map == null ? null : new HashMap<>(map);
    }
}
