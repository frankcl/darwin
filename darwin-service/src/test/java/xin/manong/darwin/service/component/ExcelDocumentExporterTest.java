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
public class ExcelDocumentExporterTest {

    @Test
    public void testWrite() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String path = String.format("%s%s", tmpDir, UUID.randomUUID());
        try {
            ExcelDocumentExporter exporter = new ExcelDocumentExporter();
            {
                List<String> columns = new ArrayList<>();
                columns.add("title");
                columns.add("content");
                Assert.assertTrue(exporter.buildSheet("New Sheet", columns));
                {
                    Map<String, Object> data = new HashMap<>();
                    data.put("title", "测试标题");
                    data.put("content", "aaaaaa");
                    Assert.assertTrue(exporter.add("New Sheet", data));
                }
                {
                    Map<String, Object> data = new HashMap<>();
                    data.put("title", "标题");
                    data.put("content", "bbbbbb");
                    Assert.assertTrue(exporter.add("New Sheet", data));
                }
            }
            {
                List<String> columns = new ArrayList<>();
                columns.add("xxx");
                columns.add("yyy");
                Assert.assertTrue(exporter.buildSheet("New Sheet2", columns));
                {
                    Map<String, Object> data = new HashMap<>();
                    data.put("xxx", "abc");
                    data.put("yyy", "ccc");
                    Assert.assertTrue(exporter.add("New Sheet2", data));
                }
            }
            FileOutputStream outputStream = new FileOutputStream(path);
            exporter.export(outputStream);
            outputStream.close();
        } finally {
            if (!new File(path).delete()) System.out.println("delete fail");
        }
    }
}
