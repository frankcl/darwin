package xin.manong.darwin.common.model.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON泛型字典转换
 *
 * @author frankcl
 * @date 2022-08-29 19:11:08
 */
@MappedTypes({ Map.class })
@MappedJdbcTypes({ JdbcType.VARCHAR })
public abstract class JSONGenericMapHandler<T> extends BaseTypeHandler<Map<String, T>> {

    protected abstract TypeReference<Map<String, T>> specificType();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, T> parameter, JdbcType jdbcType)
            throws SQLException {
        String content = parameter == null || parameter.isEmpty() ? "{}" : JSON.toJSONString(
                parameter, SerializerFeature.DisableCircularReferenceDetect);
        ps.setString(i, content);
    }

    @Override
    public Map<String, T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return this.parseMap(rs.getString(columnName));
    }

    @Override
    public Map<String, T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return this.parseMap(rs.getString(columnIndex));
    }

    @Override
    public Map<String, T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return this.parseMap(cs.getString(columnIndex));
    }

    /**
     * 解析字典
     *
     * @param content 字典字符串
     * @return 字典
     */
    private Map<String, T> parseMap(String content) {
        return StringUtils.isBlank(content) ? new HashMap<>() : JSON.parseObject(content, this.specificType());
    }
}
