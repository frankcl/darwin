package xin.manong.darwin.spider.input;

import xin.manong.darwin.spider.output.Output;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * 数据输入接口
 *
 * @author frankcl
 * @date 2025-04-11 11:57:26
 */
public abstract class Input implements Closeable {

    protected InputStream inputStream;

    /**
     * 开启数据输入
     *
     * @throws IOException I/O异常
     */
    public abstract void open() throws IOException;

    /**
     * 关闭数据输入
     *
     * @throws IOException I/O异常
     */
    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
    }

    /**
     * 传输数据到数据输出
     *
     * @param output 数据输出端
     * @throws IOException I/O异常
     */
    public void transport(Output output) throws IOException {
        output.sink(inputStream);
    }
}
