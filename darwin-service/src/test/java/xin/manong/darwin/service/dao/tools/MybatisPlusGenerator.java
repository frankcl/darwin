package xin.manong.darwin.service.dao.tools;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.keywords.MySqlKeyWordsHandler;

/**
 * MybatisPlus代码生成器
 * 按需修改配置生成代码
 *
 * @author frankcl
 * @date 2022-08-15 20:56:22
 */
public class MybatisPlusGenerator {

    private final static String AUTHOR = "MGC";
    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final static String USER_DIR = System.getProperty("user.dir");
    private final static String MODULE = "darwin-service";
    private final static String OUTPUT_DIR = String.format("%s/%s/src/main/java", USER_DIR, MODULE);
    private final static String DATABASE = "darwin";
    private final static String SCHEMA = "plan";
    private final static String JDBC_URL = "jdbc:mysql://localhost:3306/%s?useUnicode=true&characterEncoding=utf-8";
    private final static String USERNAME = "root";
    private final static String PASSWORD = "xmjjyhy28p";
    private final static String PARENT_PACKAGE = "xin.manong.darwin.service.dao";
    private final static String MODEL_PACKAGE = "model";
    private final static String MAPPER_PACKAGE = "mapper";

    /**
     * 构建全局配置
     *
     * @return 全局配置
     */
    private static GlobalConfig buildGlobalConfig() {
        return new GlobalConfig.Builder().author(AUTHOR).outputDir(OUTPUT_DIR).
                dateType(DateType.TIME_PACK).commentDate(DATE_FORMAT).build();
    }

    /**
     * 构建数据源配置
     *
     * @return 数据源配置
     */
    private static DataSourceConfig buildDataSourceConfig() {
        return new DataSourceConfig.Builder(String.format(JDBC_URL, DATABASE), USERNAME, PASSWORD).
                schema(SCHEMA).dbQuery(new MySqlQuery()).typeConvert(new MySQLTypeConvert()).
                keyWordsHandler(new MySqlKeyWordsHandler()).build();
    }

    /**
     * 构建包配置
     *
     * @return 包配置
     */
    private static PackageConfig buildPackageConfig() {
        return new PackageConfig.Builder().parent(PARENT_PACKAGE).
                entity(MODEL_PACKAGE).mapper(MAPPER_PACKAGE).build();
    }

    /**
     * 构建策略配置
     *
     * @return 策略配置
     */
    private static StrategyConfig buildStrategyConfig() {
        return new StrategyConfig.Builder().enableCapitalMode().enableSkipView().disableSqlFilter().addInclude(SCHEMA).
                entityBuilder().superClass(Model.class).enableLombok().enableChainModel().fileOverride().idType(IdType.AUTO).
                disableSerialVersionUID().enableTableFieldAnnotation().naming(NamingStrategy.underline_to_camel).
                columnNaming(NamingStrategy.underline_to_camel).mapperBuilder().superClass(BaseMapper.class).
                enableMapperAnnotation().enableBaseResultMap().enableBaseColumnList().fileOverride().build();
    }

    /**
     * 构建模板配置
     *
     * @return 模板配置
     */
    private static TemplateConfig buildTemplateConfig() {
        return new TemplateConfig.Builder().disable(TemplateType.CONTROLLER).disable(TemplateType.SERVICE).
                disable(TemplateType.SERVICEIMPL).disable(TemplateType.XML).build();
    }

    /**
     * 代码生成
     *
     * @param args
     */
    public static void main(String[] args) {
        AutoGenerator generator = new AutoGenerator(buildDataSourceConfig());
        generator.global(buildGlobalConfig()).packageInfo(buildPackageConfig()).
                strategy(buildStrategyConfig()).template(buildTemplateConfig());
        generator.execute();
    }
}
