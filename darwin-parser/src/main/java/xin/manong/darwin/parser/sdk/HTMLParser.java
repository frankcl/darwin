package xin.manong.darwin.parser.sdk;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggerRepository;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.parser.appender.MemoryWriterAppender;
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

    private static final String LOG_LAYOUT_PATTERN = "%-d{yyyy-MM-dd HH:mm:ss,SSS}-%r [%p] [%t] [%l] - %m%n";

    private Logger selfLogger = LoggerFactory.getLogger(HTMLParser.class);
    protected Logger logger;

    /**
     * 脚本解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    public ParseResponse doParse(ParseRequest request) {
        String name = String.format("%s$%s", HTMLParser.class.getName(), UUID.randomUUID());
        logger = LoggerFactory.getLogger(name);
        Layout layout = new PatternLayout(LOG_LAYOUT_PATTERN);
        MemoryWriterAppender appender = new MemoryWriterAppender(layout);
        org.apache.log4j.Logger innerLogger = LogManager.getLogger(logger.getName());
        innerLogger.addAppender(appender);
        innerLogger.setAdditivity(false);
        innerLogger.setLevel(Level.INFO);
        try {
            ParseResponse response = parse(request);
            String logContent = appender.getLogContent();
            if (logContent != null) response.debugLog = logContent;
            return response;
        } finally {
            if (appender != null) appender.close();
            sweepLoggerFactory(name);
            sweepLogManager(name);
        }
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
    private void sweepLoggerFactory(String name) {
        try {
            ILoggerFactory factory = LoggerFactory.getILoggerFactory();
            Map<String, Logger> logMap = (Map<String, Logger>) ReflectUtil.getFieldValue(factory, "loggerMap");
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
    private void sweepLogManager(String name) {
        try {
            LoggerRepository loggerRepository = LogManager.getLoggerRepository();
            Hashtable<String, org.apache.log4j.Logger> logTable = (Hashtable<String, org.apache.log4j.Logger>)
                    ReflectUtil.getFieldValue(loggerRepository, "ht");
            if (logTable == null) return;
            logTable.remove(name);
        } catch (Exception e) {
            selfLogger.error(e.getMessage(), e);
        }
    }
}
