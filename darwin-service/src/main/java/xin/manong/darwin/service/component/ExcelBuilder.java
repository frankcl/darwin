package xin.manong.darwin.service.component;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel文件构建器
 *
 * @author frankcl
 * @date 2023-12-22 15:16:00
 */
public class ExcelBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ExcelBuilder.class);

    private Workbook workbook;
    private Map<String, Sheet> sheetMap;
    private Map<String, List<String>> sheetColumnsMap;

    public ExcelBuilder() throws IOException {
        init();
    }

    /**
     * 初始化excel构建器
     *
     * @throws IOException
     */
    public void init() throws IOException {
        if (workbook != null) workbook.close();
        this.workbook = new SXSSFWorkbook();
        this.sheetMap = new HashMap<>();
        this.sheetColumnsMap = new HashMap<>();
        logger.info("init excel builder success");
    }

    /**
     * 导出数据
     *
     * @param outputStream 导出目标流
     * @throws IOException
     */
    public void export(OutputStream outputStream) throws IOException {
        if (workbook == null) {
            logger.error("excel builder is not init");
            return;
        }
        try {
            for (Map.Entry<String, Sheet> entry : sheetMap.entrySet()) {
                String name = entry.getKey();
                Sheet sheet = entry.getValue();
                logger.info("export rows[{}] for sheet[{}]", sheet.getLastRowNum(), name);
            }
            workbook.write(outputStream);
            logger.info("export excel success");
        } finally {
            workbook.close();
            workbook = null;
        }
    }

    /**
     * 创建sheet
     * 如果同名sheet存在则创建失败
     *
     * @param name sheet名称
     * @param columns 列名列表
     * @return 成功返回true，否则返回false
     */
    public boolean createSheet(String name, List<String> columns) {
        if (workbook == null) {
            logger.error("excel builder is not init");
            return false;
        }
        if (sheetMap.containsKey(name)) {
            logger.error("sheet[{}] has existed", name);
            return false;
        }
        if (StringUtils.isEmpty(name)) name = "unknown";
        Sheet sheet = workbook.createSheet(name);
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columns.get(i));
        }
        sheetMap.put(name, sheet);
        sheetColumnsMap.put(name, columns);
        logger.info("create sheet[{}] success", name);
        return true;
    }

    /**
     * 添加数据
     * sheet不存在则添加失败
     *
     * @param sheetName sheet名称
     * @param data 数据
     * @return 成功返回true，否则返回false
     * @throws IOException
     */
    public boolean add(String sheetName, Map<String, Object> data) throws IOException {
        if (workbook == null) {
            logger.error("excel builder is not init");
            return false;
        }
        Sheet sheet = sheetMap.getOrDefault(sheetName, null);
        List<String> columns = sheetColumnsMap.getOrDefault(sheetName, null);
        if (sheet == null || columns == null) {
            logger.error("sheet[{}] is not found", sheetName);
            return false;
        }
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            Cell cell = row.createCell(i);
            if (!data.containsKey(column)) continue;
            cell.setCellValue(data.get(column).toString());
        }
        int writeRows = sheet.getLastRowNum();
        if (writeRows > 0 && writeRows % 200 == 0) {
            ((SXSSFSheet) sheet).flushRows();
            logger.info("add rows[{}] for sheet[{}]", writeRows, sheetName);
        }
        return true;
    }
}
