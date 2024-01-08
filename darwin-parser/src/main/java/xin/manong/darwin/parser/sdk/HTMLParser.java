package xin.manong.darwin.parser.sdk;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggerRepository;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.parser.appender.MemoryWriterAppender;
import xin.manong.weapon.base.util.ReflectArgs;
import xin.manong.weapon.base.util.ReflectUtil;

import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

/**
 * HTML/JSON解析器
 * 用户自定义解析器需要继承此类，实现parse接口
 *
 * @author frankcl
 * @date 2023-03-16 15:07:13
 */
public abstract class HTMLParser {

    private static final String LOG4J_CATEGORY_KEY_CLASS = "org.apache.log4j.CategoryKey";
    private static final String LOG4J_FIELD_LOGGER_HASH_TABLE = "ht";
    private static final String SLF4J_FIELD_LOGGER_MAP = "loggerMap";

    private static final String LOG_LAYOUT_PATTERN = "%-d{yyyy-MM-dd HH:mm:ss,SSS}-%r [%p] [%t] [%l] - %m%n";

    private Logger selfLogger = LoggerFactory.getLogger(HTMLParser.class);
    private ThreadLocal<Logger> threadLogger = new ThreadLocal<>();

    /**
     * 脚本解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    public final ParseResponse doParse(ParseRequest request) {
        String name = String.format("%s$%s", HTMLParser.class.getName(), UUID.randomUUID());
        Logger logger = LoggerFactory.getLogger(name);
        Layout layout = new PatternLayout(LOG_LAYOUT_PATTERN);
        MemoryWriterAppender appender = new MemoryWriterAppender(layout);
        org.apache.log4j.Logger innerLogger = LogManager.getLogger(logger.getName());
        innerLogger.addAppender(appender);
        innerLogger.setAdditivity(false);
        innerLogger.setLevel(Level.INFO);
        threadLogger.set(logger);
        try {
            ParseResponse response = parse(request);
            String debugLog = appender.getLogContent();
            if (debugLog != null) response.debugLog = debugLog;
            return response;
        } finally {
            sweepSLF4JLogger(name);
            sweepLog4JLogger(name);
            if (appender != null) appender.close();
            threadLogger.remove();
        }
    }

    /**
     * 获取Logger对象
     *
     * @return Logger对象
     */
    protected final Logger getLogger() {
        return threadLogger.get();
    }

    /**
     * 脚本解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    public abstract ParseResponse parse(ParseRequest request);

    /**
     * 清理SLF4J logger
     *
     * @param name logger名称
     */
    private void sweepSLF4JLogger(String name) {
        try {
            ILoggerFactory factory = LoggerFactory.getILoggerFactory();
            Map<String, Logger> logMap = (Map<String, Logger>) ReflectUtil.getFieldValue(
                    factory, SLF4J_FIELD_LOGGER_MAP);
            if (logMap == null) return;
            logMap.remove(name);
        } catch (Exception e) {
            selfLogger.error(e.getMessage(), e);
        }
    }

    /**
     * 清理Log4J logger
     *
     * @param name logger名称
     */
    private void sweepLog4JLogger(String name) {
        try {
            LoggerRepository loggerRepository = LogManager.getLoggerRepository();
            Hashtable<String, org.apache.log4j.Logger> logTable = (Hashtable<String, org.apache.log4j.Logger>)
                    ReflectUtil.getFieldValue(loggerRepository, LOG4J_FIELD_LOGGER_HASH_TABLE);
            if (logTable == null) return;
            ReflectArgs args = new ReflectArgs(new Class[] { String.class }, new Object[] { name });
            Object key = ReflectUtil.newInstance(LOG4J_CATEGORY_KEY_CLASS, args);
            logTable.remove(key);
        } catch (Exception e) {
            selfLogger.error(e.getMessage(), e);
        }
    }
}
