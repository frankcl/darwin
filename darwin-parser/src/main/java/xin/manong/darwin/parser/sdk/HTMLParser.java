package xin.manong.darwin.parser.sdk;

import org.apache.log4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.parser.log.MemoryWriterAppender;

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
            logger = null;
        }
    }

    /**
     * 脚本解析
     *
     * @param request 解析请求
     * @return 解析响应
     */
    public abstract ParseResponse parse(ParseRequest request);
}
