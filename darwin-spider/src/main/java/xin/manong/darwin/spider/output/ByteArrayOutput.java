package xin.manong.darwin.spider.output;

import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 字节数组数据输出
 *
 * @author frankcl
 * @date 2025-04-11 14:03:33
 */
@Getter
public class ByteArrayOutput extends Output {

    private byte[] bytes;

    @Override
    public void sink(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            sink(inputStream, outputStream);
            bytes = outputStream.toByteArray();
        }
    }
}
