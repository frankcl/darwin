package xin.manong.darwin.service.dao.tools;

import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import org.apache.commons.lang3.StringUtils;

/**
 * 支持特殊字段类型转换
 * 1. 时间类型datetime
 * 2. 大数类型decimal
 *
 * @author frankcl
 * @date 2022-08-16 12:57:29
 */
public class MySQLTypeConvert extends MySqlTypeConvert {

    private final static String FIELD_TYPE_DATETIME = "datetime";
    private final static String FIELD_TYPE_DECIMAL = "decimal";

    @Override
    public IColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
        if (StringUtils.containsIgnoreCase(fieldType, FIELD_TYPE_DATETIME)) return DbColumnType.DATE;
        else if (StringUtils.containsIgnoreCase(fieldType, FIELD_TYPE_DECIMAL)) return DbColumnType.DOUBLE;
        return super.processTypeConvert(globalConfig, fieldType);
    }
}
