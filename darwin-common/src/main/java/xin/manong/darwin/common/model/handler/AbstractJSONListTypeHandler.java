package xin.manong.darwin.common.model.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON列表数据类型转换
 *
 * @author frankcl
 * @date 2022-08-29 19:11:08
 */
@MappedTypes({ List.class })
@MappedJdbcTypes({ JdbcType.VARCHAR })
public abstract class AbstractJSONListTypeHandler<T> extends BaseTypeHandler<List<T>> {

    protected abstract TypeReference<List<T>> specificType();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType)
            throws SQLException {
        String content = CollectionUtils.isEmpty(parameter) ? "[]" : JSON.toJSONString(
                parameter, SerializerFeature.DisableCircularReferenceDetect);
        ps.setString(i, content);
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return this.parseList(rs.getString(columnName));
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return this.parseList(rs.getString(columnIndex));
    }

    @Override
    public List<T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return this.parseList(cs.getString(columnIndex));
    }

    /**
     * 解析列表
     *
     * @param content 列表字符串
     * @return 列表
     */
    private List<T> parseList(String content) {
        return StringUtils.isBlank(content) ? new ArrayList<>() : JSON.parseObject(content, this.specificType());
    }
}
