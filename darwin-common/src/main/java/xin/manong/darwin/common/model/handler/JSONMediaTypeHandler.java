package xin.manong.darwin.common.model.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import xin.manong.darwin.common.model.MediaType;
import xin.manong.darwin.common.model.URLRecord;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JSON媒体类型转换
 *
 * @author frankcl
 * @date 2025-04-28 16:01:49
 */
@MappedTypes({ URLRecord.class })
@MappedJdbcTypes({ JdbcType.VARCHAR })
public class JSONMediaTypeHandler extends BaseTypeHandler<MediaType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    MediaType mediaType, JdbcType jdbcType) throws SQLException {
        if (mediaType == null) return;
        ps.setString(i, JSON.toJSONString(mediaType, SerializerFeature.DisableCircularReferenceDetect));
    }

    @Override
    public MediaType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseMediaType(rs.getString(columnName));
    }

    @Override
    public MediaType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseMediaType(rs.getString(columnIndex));
    }

    @Override
    public MediaType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseMediaType(cs.getString(columnIndex));
    }

    /**
     * 解析媒体类型
     *
     * @param content 字符串
     * @return 媒体类型
     */
    private MediaType parseMediaType(String content) {
        return StringUtils.isEmpty(content) ? null : JSON.parseObject(content, MediaType.class);
    }
}
