package com.proshine.visitmanagement.util;

import com.proshine.visitmanagement.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Excel工具类
 * 提供Excel文件的读取、写入、导出等功能
 *
 * @author System
 * @since 2024-01-01
 */
@Slf4j
public class ExcelUtils {

    /**
     * 支持的Excel文件扩展名
     */
    private static final String[] EXCEL_EXTENSIONS = {".xlsx", ".xls"};

    /**
     * 默认sheet名称
     */
    private static final String DEFAULT_SHEET_NAME = "Sheet1";

    /**
     * 最大行数限制
     */
    private static final int MAX_ROWS = 100000;

    /**
     * 日期格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 日期时间格式
     */
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private ExcelUtils() {
        // 工具类不允许实例化
    }

    // ==================== 导出功能 ====================

    /**
     * 导出数据到Excel
     *
     * @param data 数据列表
     * @param headers 表头映射（字段名 -> 显示名）
     * @param fileName 文件名
     * @param response HTTP响应
     */
    public static <T> void exportToExcel(List<T> data,
                                         LinkedHashMap<String, String> headers,
                                         String fileName,
                                         HttpServletResponse response) {
        try {
            Workbook workbook = createWorkbook(data, headers, DEFAULT_SHEET_NAME);
            writeToResponse(workbook, fileName, response);
        } catch (Exception e) {
            log.error("导出Excel失败: fileName={}", fileName, e);
            throw ValidationException.of("export", "Excel导出失败: " + e.getMessage());
        }
    }

    /**
     * 导出数据到Excel（带sheet名称）
     */
    public static <T> void exportToExcel(List<T> data,
                                         LinkedHashMap<String, String> headers,
                                         String fileName,
                                         String sheetName,
                                         HttpServletResponse response) {
        try {
            Workbook workbook = createWorkbook(data, headers, sheetName);
            writeToResponse(workbook, fileName, response);
        } catch (Exception e) {
            log.error("导出Excel失败: fileName={}, sheetName={}", fileName, sheetName, e);
            throw ValidationException.of("export", "Excel导出失败: " + e.getMessage());
        }
    }

    /**
     * 导出多个sheet到Excel
     */
    public static void exportMultiSheetToExcel(List<ExcelSheetData<?>> sheetDataList,
                                               String fileName,
                                               HttpServletResponse response) {
        try {
            Workbook workbook = new XSSFWorkbook();

            for (int i = 0; i < sheetDataList.size(); i++) {
                ExcelSheetData<?> sheetData = sheetDataList.get(i);
                String sheetName = "Sheet" + (i + 1);
                createSheet(workbook, sheetData.getData(), sheetData.getHeaders(), sheetName);
            }

            writeToResponse(workbook, fileName, response);

        } catch (Exception e) {
            log.error("导出多sheet Excel失败: fileName={}", fileName, e);
            throw ValidationException.of("export", "Excel导出失败: " + e.getMessage());
        }
    }

    /**
     * 生成Excel字节数组
     */
    public static <T> byte[] generateExcelBytes(List<T> data,
                                                LinkedHashMap<String, String> headers) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Workbook workbook = createWorkbook(data, headers, DEFAULT_SHEET_NAME);
            workbook.write(baos);
            workbook.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("生成Excel字节数组失败", e);
            throw ValidationException.of("export", "Excel生成失败: " + e.getMessage());
        }
    }

    // ==================== 导入功能 ====================

    /**
     * 从Excel文件读取数据
     */
    public static List<Map<String, Object>> readFromExcel(MultipartFile file) {
        return readFromExcel(file, 0, true);
    }

    /**
     * 从Excel文件读取数据（指定sheet和是否包含标题行）
     */
    public static List<Map<String, Object>> readFromExcel(MultipartFile file,
                                                          int sheetIndex,
                                                          boolean hasHeader) {
        validateExcelFile(file);

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(sheetIndex);
            return readSheetData(sheet, hasHeader);

        } catch (Exception e) {
            log.error("读取Excel文件失败: fileName={}", file.getOriginalFilename(), e);
            throw ValidationException.of("import", "Excel文件读取失败: " + e.getMessage());
        }
    }

    /**
     * 从Excel文件读取数据并转换为指定类型
     */
    public static <T> List<T> readFromExcel(MultipartFile file,
                                            Class<T> clazz,
                                            Map<String, String> fieldMapping) {
        List<Map<String, Object>> dataList = readFromExcel(file);
        return convertToObjects(dataList, clazz, fieldMapping);
    }

    /**
     * 验证Excel模板格式
     */
    public static boolean validateTemplate(MultipartFile file, List<String> requiredHeaders) {
        try {
            List<Map<String, Object>> dataList = readFromExcel(file, 0, true);
            if (dataList.isEmpty()) {
                return false;
            }

            Set<String> actualHeaders = dataList.get(0).keySet();
            return actualHeaders.containsAll(requiredHeaders);

        } catch (Exception e) {
            log.warn("验证Excel模板失败: fileName={}", file.getOriginalFilename(), e);
            return false;
        }
    }

    /**
     * 创建导入模板
     */
    public static void createImportTemplate(LinkedHashMap<String, String> headers,
                                            String fileName,
                                            HttpServletResponse response) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("导入模板");

            // 创建标题样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            int colIndex = 0;

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(entry.getValue());
                cell.setCellStyle(headerStyle);
            }

            // 自动调整列宽
            autoSizeColumns(sheet, headers.size());

            writeToResponse(workbook, fileName, response);

        } catch (Exception e) {
            log.error("创建导入模板失败: fileName={}", fileName, e);
            throw ValidationException.of("template", "导入模板创建失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 创建工作簿
     */
    private static <T> Workbook createWorkbook(List<T> data,
                                               LinkedHashMap<String, String> headers,
                                               String sheetName) {
        Workbook workbook = new XSSFWorkbook();
        createSheet(workbook, data, headers, sheetName);
        return workbook;
    }

    /**
     * 创建工作表
     */
    private static <T> void createSheet(Workbook workbook,
                                        List<T> data,
                                        LinkedHashMap<String, String> headers,
                                        String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);

        // 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        int colIndex = 0;

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(entry.getValue());
            cell.setCellStyle(headerStyle);
        }

        // 创建数据行
        int rowIndex = 1;
        for (T item : data) {
            Row row = sheet.createRow(rowIndex++);
            colIndex = 0;

            for (String fieldName : headers.keySet()) {
                Cell cell = row.createCell(colIndex++);
                Object value = getFieldValue(item, fieldName);

                if (value != null) {
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                        cell.setCellStyle(dataStyle);
                    } else if (value instanceof Date) {
                        cell.setCellValue((Date) value);
                        cell.setCellStyle(dateStyle);
                    } else if (value instanceof LocalDate) {
                        cell.setCellValue(value.toString());
                        cell.setCellStyle(dataStyle);
                    } else if (value instanceof LocalDateTime) {
                        cell.setCellValue(((LocalDateTime) value).format(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
                        cell.setCellStyle(dataStyle);
                    } else {
                        cell.setCellValue(value.toString());
                        cell.setCellStyle(dataStyle);
                    }
                } else {
                    cell.setCellValue("");
                    cell.setCellStyle(dataStyle);
                }
            }
        }

        // 自动调整列宽
        autoSizeColumns(sheet, headers.size());
    }

    /**
     * 获取字段值（支持嵌套字段）
     */
    private static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || fieldName == null) {
            return null;
        }

        try {
            String[] fieldParts = fieldName.split("\\.");
            Object currentObj = obj;

            for (String part : fieldParts) {
                if (currentObj == null) {
                    return null;
                }

                Field field = findField(currentObj.getClass(), part);
                if (field == null) {
                    return null;
                }

                field.setAccessible(true);
                currentObj = field.get(currentObj);
            }

            return currentObj;
        } catch (Exception e) {
            log.warn("获取字段值失败: fieldName={}", fieldName, e);
            return null;
        }
    }

    /**
     * 查找字段（支持继承）
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 创建标题样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        return style;
    }

    /**
     * 创建数据样式
     */
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 对齐
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * 创建日期样式
     */
    private static CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);

        // 日期格式
        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat(DATETIME_FORMAT));

        return style;
    }

    /**
     * 自动调整列宽
     */
    private static void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            // 设置最大宽度限制
            int columnWidth = sheet.getColumnWidth(i);
            if (columnWidth > 6000) {
                sheet.setColumnWidth(i, 6000);
            }
        }
    }

    /**
     * 读取工作表数据
     */
    private static List<Map<String, Object>> readSheetData(Sheet sheet, boolean hasHeader) {
        List<Map<String, Object>> dataList = new ArrayList<>();

        if (sheet.getPhysicalNumberOfRows() == 0) {
            return dataList;
        }

        // 获取标题行
        List<String> headers = new ArrayList<>();
        Row headerRow = sheet.getRow(hasHeader ? 0 : -1);

        if (hasHeader && headerRow != null) {
            for (Cell cell : headerRow) {
                headers.add(getCellStringValue(cell));
            }
        } else {
            // 如果没有标题行，使用列索引作为键
            Row firstRow = sheet.getRow(0);
            if (firstRow != null) {
                for (int i = 0; i < firstRow.getLastCellNum(); i++) {
                    headers.add("Column" + (i + 1));
                }
            }
        }

        // 读取数据行
        int startRow = hasHeader ? 1 : 0;
        for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            Map<String, Object> rowData = new LinkedHashMap<>();
            boolean hasData = false;

            for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
                Cell cell = row.getCell(colIndex);
                Object value = getCellValue(cell);
                rowData.put(headers.get(colIndex), value);

                if (value != null && !value.toString().trim().isEmpty()) {
                    hasData = true;
                }
            }

            // 只添加非空行
            if (hasData) {
                dataList.add(rowData);
            }
        }

        return dataList;
    }

    /**
     * 获取单元格值
     */
    private static Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // 判断是否为整数
                    if (numericValue == Math.floor(numericValue)) {
                        return (long) numericValue;
                    } else {
                        return numericValue;
                    }
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return null;
        }
    }

    /**
     * 获取单元格字符串值
     */
    private static String getCellStringValue(Cell cell) {
        Object value = getCellValue(cell);
        return value != null ? value.toString().trim() : "";
    }

    /**
     * 写入HTTP响应
     */
    private static void writeToResponse(Workbook workbook, String fileName, HttpServletResponse response)
            throws IOException {

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");

        // 处理文件名编码
        String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

        // 写入响应流
        try (OutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
            workbook.close();
        }
    }

    /**
     * 验证Excel文件
     */
    private static void validateExcelFile(MultipartFile file) {
        // 验证文件是否为空
        if (file == null || file.isEmpty()) {
            throw ValidationException.File.emptyFile();
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw ValidationException.of("file", "文件名不能为空");
        }

        // 验证文件类型
        boolean isExcelFile = Arrays.stream(EXCEL_EXTENSIONS)
                .anyMatch(ext -> fileName.toLowerCase().endsWith(ext));

        if (!isExcelFile) {
            throw ValidationException.File.unsupportedType(fileName);
        }

        // 检查文件大小（10MB限制）
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw ValidationException.File.sizeTooLarge(fileName, maxSize);
        }

        log.info("Excel文件验证通过: fileName={}, size={}", fileName, file.getSize());
    }

    /**
     * 转换为对象列表
     */
    private static <T> List<T> convertToObjects(List<Map<String, Object>> dataList,
                                                Class<T> clazz,
                                                Map<String, String> fieldMapping) {
        List<T> resultList = new ArrayList<>();

        for (Map<String, Object> rowData : dataList) {
            try {
                T obj = clazz.getDeclaredConstructor().newInstance();

                for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
                    String excelColumn = entry.getKey();
                    String fieldName = entry.getValue();
                    Object value = rowData.get(excelColumn);

                    if (value != null) {
                        setFieldValue(obj, fieldName, value);
                    }
                }

                resultList.add(obj);

            } catch (Exception e) {
                log.warn("转换对象失败: {}", e.getMessage());
            }
        }

        return resultList;
    }

    /**
     * 设置字段值
     */
    private static void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);

                // 类型转换
                Object convertedValue = convertValue(value, field.getType());
                field.set(obj, convertedValue);
            }
        } catch (Exception e) {
            log.warn("设置字段值失败: fieldName={}, value={}", fieldName, value, e);
        }
    }

    /**
     * 值类型转换
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        String stringValue = value.toString().trim();
        if (stringValue.isEmpty()) {
            return null;
        }

        try {
            if (targetType == String.class) {
                return stringValue;
            } else if (targetType == Integer.class || targetType == int.class) {
                return Integer.valueOf(stringValue.split("\\.")[0]); // 处理Excel中的小数
            } else if (targetType == Long.class || targetType == long.class) {
                return Long.valueOf(stringValue.split("\\.")[0]);
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.valueOf(stringValue);
            } else if (targetType == BigDecimal.class) {
                return new BigDecimal(stringValue);
            } else if (targetType == LocalDate.class) {
                return LocalDate.parse(stringValue, DateTimeFormatter.ofPattern(DATE_FORMAT));
            } else if (targetType == LocalDateTime.class) {
                return LocalDateTime.parse(stringValue, DateTimeFormatter.ofPattern(DATETIME_FORMAT));
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.valueOf(stringValue);
            }
        } catch (Exception e) {
            log.warn("类型转换失败: value={}, targetType={}", value, targetType.getSimpleName());
        }

        return value;
    }

    /**
     * 解析Excel文件
     *
     * @param file Excel文件
     * @return 解析后的数据列表
     */
    public static List<Map<String, Object>> parseExcel(MultipartFile file) {
        List<Map<String, Object>> result = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() <= 1) {
                log.warn("Excel文件无数据行");
                return result;
            }

            // 获取表头行
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("Excel文件格式错误：缺少表头行");
            }

            // 解析表头
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                String headerValue = getCellStringValue(cell);
                headers.add(parseHeaderName(headerValue));
            }

            // 解析数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow == null || isRowEmpty(dataRow)) {
                    continue;
                }

                Map<String, Object> rowData = new HashMap<>();
                for (int j = 0; j < headers.size() && j < dataRow.getLastCellNum(); j++) {
                    Cell cell = dataRow.getCell(j);
                    String fieldName = headers.get(j);
                    Object cellValue = getCellValue(cell);
                    rowData.put(fieldName, cellValue);
                }

                result.add(rowData);
            }

            log.info("Excel解析成功: 数据行数={}", result.size());

        } catch (Exception e) {
            log.error("Excel解析失败", e);
            throw new RuntimeException("Excel解析失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Excel Sheet数据包装类
     */
    public static class ExcelSheetData<T> {
        private List<T> data;
        private LinkedHashMap<String, String> headers;

        public ExcelSheetData(List<T> data, LinkedHashMap<String, String> headers) {
            this.data = data;
            this.headers = headers;
        }

        public List<T> getData() {
            return data;
        }

        public LinkedHashMap<String, String> getHeaders() {
            return headers;
        }
    }

    /**
     * 判断行是否为空
     */
    private static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String cellValue = getCellStringValue(cell);
                if (!cellValue.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 解析表头名称（去掉*等特殊字符）
     */
    private static String parseHeaderName(String headerValue) {
        if (headerValue == null) {
            return "";
        }

        // 映射中文表头到字段名
        Map<String, String> headerMapping = new HashMap<>();
        headerMapping.put("用户名", "username");
        headerMapping.put("用户名*", "username");
        headerMapping.put("密码", "password");
        headerMapping.put("密码*", "password");
        headerMapping.put("真实姓名", "realName");
        headerMapping.put("真实姓名*", "realName");
        headerMapping.put("邮箱", "email");
        headerMapping.put("手机号", "phone");
        headerMapping.put("角色", "role");
        headerMapping.put("角色*(ADMIN/MANAGER/SALES)", "role");
        headerMapping.put("部门", "department");

        return headerMapping.getOrDefault(headerValue.trim(), headerValue.trim());
    }
}