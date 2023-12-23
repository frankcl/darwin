package xin.manong.darwin.service.component;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author frankcl
 * @date 2023-12-22 15:58:23
 */
public class ExcelWriterSuite {

    @Test
    public void testWrite() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String path = String.format("%s%s", tmpDir, UUID.randomUUID());
        try {
            ExcelWriter writer = new ExcelWriter();
            {
                List<String> columns = new ArrayList<>();
                columns.add("title");
                columns.add("content");
                Assert.assertTrue(writer.createSheet("New Sheet", columns));
                {
                    Map<String, Object> data = new HashMap<>();
                    data.put("title", "测试标题");
                    data.put("content", "aaaaaa");
                    Assert.assertTrue(writer.write("New Sheet", data));
                }
                {
                    Map<String, Object> data = new HashMap<>();
                    data.put("title", "标题");
                    data.put("content", "bbbbbb");
                    Assert.assertTrue(writer.write("New Sheet", data));
                }
            }
            {
                List<String> columns = new ArrayList<>();
                columns.add("xxx");
                columns.add("yyy");
                Assert.assertTrue(writer.createSheet("New Sheet2", columns));
                {
                    Map<String, Object> data = new HashMap<>();
                    data.put("xxx", "abc");
                    data.put("yyy", "ccc");
                    Assert.assertTrue(writer.write("New Sheet2", data));
                }
            }
            FileOutputStream outputStream = new FileOutputStream(path);
            writer.export(outputStream);
            outputStream.close();
        } finally {
            new File(path).delete();
        }
    }
}
