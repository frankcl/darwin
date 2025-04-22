package xin.manong.darwin.service.component;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel文档导出
 *
 * @author frankcl
 * @date 2023-12-22 15:16:00
 */
public class ExcelDocumentExporter {

    private static final Logger logger = LoggerFactory.getLogger(ExcelDocumentExporter.class);

    private Workbook workbook;
    private Map<String, Sheet> sheetMap;
    private Map<String, List<String>> sheetColumnsMap;

    public ExcelDocumentExporter() throws IOException {
        init();
    }

    /**
     * 初始化excel构建器
     *
     * @throws IOException I/O异常
     */
    public void init() throws IOException {
        if (workbook != null) workbook.close();
        this.workbook = new SXSSFWorkbook();
        this.sheetMap = new HashMap<>();
        this.sheetColumnsMap = new HashMap<>();
        logger.info("Init excel document exporter success");
    }

    /**
     * 导出数据
     *
     * @param output 导出目标流
     * @throws IOException I/O异常
     */
    public void export(OutputStream output) throws IOException {
        assert workbook != null;
        try {
            for (Map.Entry<String, Sheet> entry : sheetMap.entrySet()) {
                String name = entry.getKey();
                Sheet sheet = entry.getValue();
                logger.info("Export rows num:{} for sheet:{}", sheet.getPhysicalNumberOfRows(), name);
            }
            workbook.write(output);
            logger.info("Export Excel document success");
        } finally {
            workbook.close();
            workbook = null;
        }
    }

    /**
     * 构建表单
     * 如果同名表单存在则构建失败
     *
     * @param sheetName 表单名称
     * @param columns 列名列表
     * @return 成功返回true，否则返回false
     */
    public boolean buildSheet(@NotNull String sheetName, List<String> columns) {
        assert workbook != null;
        if (sheetMap.containsKey(sheetName)) {
            logger.error("Sheet:{} has existed", sheetName);
            return false;
        }
        Sheet sheet = workbook.createSheet(sheetName);
        Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columns.get(i));
        }
        sheetMap.put(sheetName, sheet);
        sheetColumnsMap.put(sheetName, columns);
        logger.info("Build sheet:{} success", sheetName);
        return true;
    }

    /**
     * 添加数据
     * 如果表单不存在则添加失败
     *
     * @param sheetName 表单名称
     * @param data 数据
     * @return 成功返回true，否则返回false
     * @throws IOException I/O异常
     */
    public boolean add(String sheetName, Map<String, Object> data) throws IOException {
        assert workbook != null;
        Sheet sheet = sheetMap.getOrDefault(sheetName, null);
        List<String> columns = sheetColumnsMap.getOrDefault(sheetName, null);
        if (sheet == null || columns == null) {
            logger.error("Sheet:{} is not found", sheetName);
            return false;
        }
        Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            Cell cell = row.createCell(i);
            if (!data.containsKey(column)) continue;
            cell.setCellValue(data.get(column).toString());
        }
        int writeRows = sheet.getPhysicalNumberOfRows();
        if (writeRows > 0 && writeRows % 200 == 0) {
            ((SXSSFSheet) sheet).flushRows();
            logger.info("Flush rows num:{} for sheet:{}", writeRows, sheetName);
        }
        return true;
    }
}
