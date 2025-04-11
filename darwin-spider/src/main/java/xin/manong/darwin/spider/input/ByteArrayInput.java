package xin.manong.darwin.spider.input;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 字节数组数据源输入
 *
 * @author frankcl
 * @date 2025-04-11 15:35:52
 */
public class ByteArrayInput extends Input {

    private final byte[] byteArray;

    public ByteArrayInput(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    @Override
    public void open() throws IOException {
        close();
        inputStream = new ByteArrayInputStream(byteArray);
    }
}
