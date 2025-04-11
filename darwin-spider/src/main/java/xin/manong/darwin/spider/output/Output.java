package xin.manong.darwin.spider.output;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 数据输出接口
 *
 * @author frankcl
 * @date 2025-04-11 11:58:41
 */
public abstract class Output implements Closeable {

    /**
     * 数据下沉落地
     *
     * @param inputStream 数据输入流
     * @throws IOException I/O异常
     */
    public abstract void sink(InputStream inputStream) throws IOException;

    /**
     * 关闭数据输出
     *
     * @throws IOException I/O异常
     */
    public void close() throws IOException {}

    /**
     * 数据下沉落地
     *
     * @param inputStream 数据输入流
     * @param outputStream 数据输出流
     * @throws IOException I/O异常
     */
    protected void sink(InputStream inputStream, OutputStream outputStream) throws IOException {
        int bufferSize = 4096, n;
        byte[] buffer = new byte[bufferSize];
        while ((n = inputStream.read(buffer, 0, bufferSize)) != -1) {
            outputStream.write(buffer, 0, n);
        }
    }
}
