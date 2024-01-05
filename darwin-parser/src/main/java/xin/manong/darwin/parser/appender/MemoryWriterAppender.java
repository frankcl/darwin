package xin.manong.darwin.parser.appender;

import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * 内存日志appender
 *
 * @author frankcl
 * @date 2024-01-05 17:29:32
 */
public class MemoryWriterAppender extends WriterAppender {

    private static final int MAX_BUFFER_SIZE = 1024 * 1024;

    private ByteArrayOutputStream output;

    public MemoryWriterAppender(Layout layout) {
        super();
        this.layout = layout;
        this.output = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
        this.setWriter(writer);
    }

    @Override
    public void append(LoggingEvent event) {
        if (output.size() > MAX_BUFFER_SIZE) return;
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
}
