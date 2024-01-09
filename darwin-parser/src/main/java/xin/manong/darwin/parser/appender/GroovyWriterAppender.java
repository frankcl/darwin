package xin.manong.darwin.parser.appender;

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
public class GroovyWriterAppender extends WriterAppender {

    private static final int MAX_BUFFER_SIZE = 1024 * 1024;
    private static final String LOG_KEY_GROOVY_FILE_LINE = "groovyFileLine";
    private static final String GROOVY_RUNTIME_CALL_SITE_PACKAGE_PREFIX = "org.codehaus.groovy.runtime.callsite.";

    private ByteArrayOutputStream output;

    public GroovyWriterAppender(Layout layout) {
        super();
        this.layout = layout;
        this.output = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
        this.setWriter(writer);
    }

    @Override
    public void append(LoggingEvent event) {
        if (output.size() > MAX_BUFFER_SIZE) return;
        parseGroovyFileLine();
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
     * 解析Groovy打印日志文件和行号
     */
    private void parseGroovyFileLine() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = findGroovyStackTraceElement(stackTraceElements);
        if (stackTraceElement == null) MDC.remove(LOG_KEY_GROOVY_FILE_LINE);
        else MDC.put(LOG_KEY_GROOVY_FILE_LINE, String.format("%s:%d", stackTraceElement.getFileName(), stackTraceElement.getLineNumber()));
    }

    /**
     * 查找Groovy打印日志堆栈元素
     *
     * @param stackTraceElements 调用堆栈
     * @return 成功返回堆栈元素，否则返回null
     */
    private StackTraceElement findGroovyStackTraceElement(StackTraceElement[] stackTraceElements) {
        for (int i = 0; i < stackTraceElements.length; i++) {
            if (!stackTraceElements[i].getClassName().startsWith(GROOVY_RUNTIME_CALL_SITE_PACKAGE_PREFIX)) continue;
            while (++i < stackTraceElements.length) {
                StackTraceElement stackTraceElement = stackTraceElements[i];
                if (stackTraceElement.getClassName().startsWith(GROOVY_RUNTIME_CALL_SITE_PACKAGE_PREFIX)) continue;
                return stackTraceElement;
            }
        }
        return null;
    }
}
