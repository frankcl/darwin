package xin.manong.darwin.parser.sdk;

import org.apache.log4j.Layout;
import org.apache.log4j.MDC;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Groovy脚本日志appender
 * 1. 日志记录在内容，不超过1MB
 * 2. 从调用堆栈获取日志打印文件和行号，解决Groovy调用Log4J打印文件行号缺失问题
 *
 * @author frankcl
 * @date 2024-01-05 17:29:32
 */
class GroovyLogAppender extends WriterAppender {

    private static final int MAX_BUFFER_SIZE = 1024 * 1024;
    private static final String MDC_KEY_GROOVY_FILE_NAME = "GF";
    private static final String MDC_KEY_GROOVY_CLASS_NAME = "GC";
    private static final String MDC_KEY_GROOVY_METHOD_NAME = "GM";
    private static final String MDC_KEY_GROOVY_LINE_NUMBER = "GL";
    private static final String LOG4J_LOGGER_CLASS = "org.slf4j.impl.Log4jLoggerAdapter";
    private static final String GROOVY_RUNTIME_CALL_SITE_PACKAGE_PREFIX = "org.codehaus.groovy.runtime.callsite.";

    private ByteArrayOutputStream output;

    public GroovyLogAppender(Layout layout) {
        super();
        this.layout = layout;
        this.output = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
        this.setWriter(writer);
    }

    @Override
    public void append(LoggingEvent event) {
        if (output.size() > MAX_BUFFER_SIZE) return;
        parseGroovyLog();
        super.append(event);
    }

    /**
     * 获取日志内容
     *
     * @return 日志内容
     */
    public String getLogContent() {
        if (output == null) return null;
        return new String(output.toByteArray(), Charset.forName("UTF-8"));
    }

    /**
     * 解析Groovy日志信息
     * 1. 文件名
     * 2. 类信息
     * 3. 方法名
     * 4. 行号
     */
    private void parseGroovyLog() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        int pos = findLog4JStackTraceElement(stackTraceElements);
        StackTraceElement stackTraceElement = findGroovyStackTraceElement(stackTraceElements, pos);
        if (stackTraceElement == null && pos >= 0 && pos + 1 < stackTraceElements.length) {
            stackTraceElement = stackTraceElements[pos+1];
        }
        MDC.put(MDC_KEY_GROOVY_FILE_NAME, stackTraceElement != null ? stackTraceElement.getFileName() : "?");
        MDC.put(MDC_KEY_GROOVY_CLASS_NAME, stackTraceElement != null ? stackTraceElement.getClassName() : "?");
        MDC.put(MDC_KEY_GROOVY_METHOD_NAME, stackTraceElement != null ? stackTraceElement.getMethodName() : "?");
        MDC.put(MDC_KEY_GROOVY_LINE_NUMBER, stackTraceElement != null ? stackTraceElement.getLineNumber() : "?");
    }

    /**
     * 查找Groovy打印日志堆栈元素
     *
     * @param stackTraceElements 调用堆栈
     * @param pos 起始下标
     * @return 成功返回堆栈元素，否则返回null
     */
    private StackTraceElement findGroovyStackTraceElement(StackTraceElement[] stackTraceElements, int pos) {
        for (int i = pos + 1; i < stackTraceElements.length; i++) {
            if (!stackTraceElements[i].getClassName().startsWith(GROOVY_RUNTIME_CALL_SITE_PACKAGE_PREFIX)) continue;
            while (++i < stackTraceElements.length) {
                StackTraceElement stackTraceElement = stackTraceElements[i];
                if (stackTraceElement.getClassName().startsWith(GROOVY_RUNTIME_CALL_SITE_PACKAGE_PREFIX)) continue;
                return stackTraceElement;
            }
        }
        return null;
    }

    /**
     * 获取Log4J堆栈元素下标
     *
     * @param stackTraceElements 调用堆栈
     * @return 成功返回下标，否则返回-1
     */
    private int findLog4JStackTraceElement(StackTraceElement[] stackTraceElements) {
        for (int i = 0; i < stackTraceElements.length; i++) {
            if (stackTraceElements[i].getClassName().equals(LOG4J_LOGGER_CLASS)) return i;
        }
        return -1;
    }
}
