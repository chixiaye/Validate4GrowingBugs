package util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author chixiaye
 */
@Slf4j
public class ProjectIoUtil {
    /**
     * 读取原始excel文件，返回一个列表
     */
    public static List<Object> readExcel(String filePath, Class<?> targetClass) throws IOException, InstantiationException, IllegalAccessException {
        FileInputStream file = new FileInputStream(new File(filePath));
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        List<Object> result = new ArrayList<>();
        Map<String, Integer> headerMap = new HashMap<>();
        boolean isHeaderRow = true;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (isHeaderRow) {
                // 解析表头
                Iterator<Cell> cellIterator = row.cellIterator();
                int columnIndex = 0;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    headerMap.put(cell.getStringCellValue(), columnIndex);
                    columnIndex++;
                }
                isHeaderRow = false;
            } else {
                // 解析数据行
                Object obj = targetClass.newInstance();
                Field[] fields = targetClass.getDeclaredFields();
                for (Field field : fields) {
                    String fieldName = field.getName();
                    int columnIndex = headerMap.get(fieldName);
                    Cell cell = row.getCell(columnIndex);
                    field.setAccessible(true);
                    if(cell==null || cell.getCellType() ==null){
                        continue;
                    }
                    if (CellType.valueOf(cell.getCellType().name())==CellType.STRING ) {
                        field.set(obj, cell.getStringCellValue());
                    } else if (CellType.valueOf(cell.getCellType().name())==CellType.NUMERIC) {
                        field.set(obj,String.valueOf((int)cell.getNumericCellValue())) ;
                    }
                }
                result.add(obj);
            }
        }
        file.close();
        return result;
    }

    public static void convertToCSV(List dataList, String filePath) throws IOException {
        FileWriter writer = new FileWriter(filePath);
        // 获取POJO类的所有字段名称
        Class<?> clazz = dataList.get(0).getClass();
        Field[] fields = clazz.getDeclaredFields();
        String[] headers = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            headers[i] = fields[i].getName();
        }

        // 定义CSV格式和表头
        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);
        CSVPrinter printer = new CSVPrinter(writer, format);

        // 写入数据行
        for (Object data : dataList) {
            Object[] values = new Object[fields.length];
            for (int i = 0; i < fields.length; i++) {
                try {
                    fields[i].setAccessible(true);
                    values[i] = fields[i].get(data);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            printer.printRecord(values);
        }
        printer.close();
        writer.close();
    }

}
