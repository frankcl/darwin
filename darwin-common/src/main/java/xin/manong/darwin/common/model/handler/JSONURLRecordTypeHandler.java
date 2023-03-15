package xin.manong.darwin.common.model.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import xin.manong.darwin.common.model.URLRecord;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JSON用户信息类型转换
 *
 * @author frankcl
 * @date 2022-08-31 16:01:49
 */
@MappedTypes({ URLRecord.class })
@MappedJdbcTypes({ JdbcType.VARCHAR })
public class JSONURLRecordTypeHandler extends BaseTypeHandler<URLRecord> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, URLRecord record, JdbcType jdbcType) throws SQLException {
        if (record == null) return;
        ps.setString(i, JSON.toJSONString(record, SerializerFeature.DisableCircularReferenceDetect));
    }

    @Override
    public URLRecord getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseURLRecord(rs.getString(columnName));
    }

    @Override
    public URLRecord getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseURLRecord(rs.getString(columnIndex));
    }

    @Override
    public URLRecord getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseURLRecord(cs.getString(columnIndex));
    }

    /**
     * 解析用户信息
     *
     * @param content 字符串
     * @return 用户信息
     */
    private URLRecord parseURLRecord(String content) {
        return StringUtils.isEmpty(content) ? null : JSON.parseObject(content, URLRecord.class);
    }
}
