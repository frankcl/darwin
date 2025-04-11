package xin.manong.darwin.spider.input;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * 本地文件源输入
 *
 * @author frankcl
 * @date 2025-04-11 13:40:24
 */
public class DiskInput extends Input {

    private final String inputFile;

    public DiskInput(String inputFile) {
        this.inputFile = inputFile;
    }

    @Override
    public void open() throws IOException {
        close();
        inputStream = new FileInputStream(inputFile);
    }
}
