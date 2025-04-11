package xin.manong.darwin.spider.output;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 本地文件输出
 *
 * @author frankcl
 * @date 2025-04-11 13:43:07
 */
public class DiskOutput extends Output {

    private final String outputFile;

    public DiskOutput(String outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public void sink(InputStream inputStream) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            sink(inputStream, outputStream);
        }
    }
}
