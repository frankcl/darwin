package xin.manong.darwin.parser.sdk;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggerRepository;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.weapon.base.util.ReflectArgs;
import xin.manong.weapon.base.util.ReflectUtil;

import java.util.Hashtable;
import java.util.Map;

/**
 * Groovy日志：以本地线程方式实现，解决多线程竞争问题
 *
 * @author frankcl
 * @date 2025-05-28 16:18:48
 */
public class GroovyLogger {

    private static final String LOG4J_CATEGORY_KEY_CLASS = "org.apache.log4j.CategoryKey";
    private static final String LOG4J_FIELD_LOGGER_HASH_TABLE = "ht";
    private static final String SLF4J_FIELD_LOGGER_MAP = "loggerMap";

    private static final String LAYOUT_PATTERN = "%-d{yyyy-MM-dd HH:mm:ss,SSS}-%r [%p] [%t] [%X{GC}:%X{GL}] - %m%n";

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyLogger.class);
    private final ThreadLocal<Logger> logger = new ThreadLocal<>();
    private final ThreadLocal<GroovyLogAppender> appender = new ThreadLocal<>();

    /**
     * INFO日志
     *
     * @param format 格式
     * @param vars 参数
     */
    public void info(String format, Object... vars) {
        logger.get().info(format, vars);
    }

    /**
     * WARN日志
     *
     * @param format 格式
     * @param vars 参数
     */
    public void warn(String format, Object... vars) {
        logger.get().warn(format, vars);
    }

    /**
     * ERROR日志
     *
     * @param format 格式
     * @param vars 参数
     */
    public void error(String format, Object... vars) {
        logger.get().error(format, vars);
    }

    /**
     * 获取日志内容
     *
     * @return 日志内容
     */
    String getLogContent() {
        if (appender.get() == null) return null;
        return appender.get().getLogContent();
    }

    /**
     * 打开日志
     *
     */
    void open() {
        if (logger.get() != null) return;
        String name = String.format("%s$%d", GroovyLogger.class.getName(), Thread.currentThread().getId());
        Logger loggerWrapper = LoggerFactory.getLogger(name);
        Layout layout = new PatternLayout(LAYOUT_PATTERN);
        appender.set(new GroovyLogAppender(layout));
        org.apache.log4j.Logger log4jLogger = LogManager.getLogger(loggerWrapper.getName());
        log4jLogger.addAppender(appender.get());
        log4jLogger.setAdditivity(false);
        log4jLogger.setLevel(Level.INFO);
        logger.set(loggerWrapper);
    }

    /**
     * 重置日志
     */
    void reset() {
        if (appender.get() == null) return;
        appender.get().reset();
    }

    /**
     * 关闭日志
     */
    void close() {
        logger.remove();
        sweepLog4JLogger();
        sweepLoggerWrapper();
        if (appender.get() != null) {
            appender.get().close();
            appender.remove();
        }
    }

    /**
     * 清理SLF4J logger
     */
    @SuppressWarnings("unchecked")
    private void sweepLoggerWrapper() {
        try {
            ILoggerFactory factory = LoggerFactory.getILoggerFactory();
            Map<String, Logger> logMap = (Map<String, Logger>) ReflectUtil.getFieldValue(
                    factory, SLF4J_FIELD_LOGGER_MAP);
            if (logMap == null) {
                LOGGER.warn("Field:{} is not found in SLF4J log factory", SLF4J_FIELD_LOGGER_MAP);
                return;
            }
            String name = String.format("%s$%d", GroovyLogger.class.getName(), Thread.currentThread().getId());
            if (logMap.containsKey(name)) logMap.remove(name);
            else LOGGER.warn("Logger:{} is not found in SLF4J log map", name);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 清理Log4J logger
     */
    @SuppressWarnings("unchecked")
    private void sweepLog4JLogger() {
        try {
            LoggerRepository repository = LogManager.getLoggerRepository();
            Hashtable<Object, org.apache.log4j.Logger> logTable = (Hashtable<Object, org.apache.log4j.Logger>)
                    ReflectUtil.getFieldValue(repository, LOG4J_FIELD_LOGGER_HASH_TABLE);
            if (logTable == null) {
                LOGGER.warn("Field:{} is not found in Log4J log manager", LOG4J_FIELD_LOGGER_HASH_TABLE);
                return;
            }
            String name = String.format("%s$%d", GroovyLogger.class.getName(), Thread.currentThread().getId());
            ReflectArgs args = new ReflectArgs(new Class[] { String.class }, new Object[] { name });
            Object key = ReflectUtil.newInstance(LOG4J_CATEGORY_KEY_CLASS, args);
            if (logTable.containsKey(key)) logTable.remove(key);
            else LOGGER.warn("Logger:{} is not found in Log4J hash table", name);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
