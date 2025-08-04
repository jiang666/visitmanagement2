package com.proshine.visitmanagement.service;

import com.proshine.visitmanagement.dto.request.CustomerRequest;
import com.proshine.visitmanagement.dto.request.SchoolRequest;
import com.proshine.visitmanagement.dto.response.CustomerResponse;
import com.proshine.visitmanagement.dto.response.SchoolResponse;
import com.proshine.visitmanagement.dto.response.VisitRecordResponse;
import com.proshine.visitmanagement.entity.Customer;
import com.proshine.visitmanagement.entity.School;
import com.proshine.visitmanagement.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

/**
 * 导出服务类
 *
 * @author System
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 导出拜访记录
     *
     * @param visitRecords 拜访记录列表
     * @param format 导出格式
     * @return 文件字节数组
     */
    public byte[] exportVisitRecords(List<VisitRecordResponse> visitRecords, String format) {
        if ("pdf".equalsIgnoreCase(format)) {
            return exportVisitRecordsToPdf(visitRecords);
        } else {
            return exportVisitRecordsToExcel(visitRecords);
        }
    }

    /**
     * 导出客户数据
     *
     * @param customers 客户列表
     * @param format 导出格式
     * @return 文件字节数组
     */
    public byte[] exportCustomers(List<CustomerResponse> customers, String format) {
        if ("pdf".equalsIgnoreCase(format)) {
            return exportCustomersToPdf(customers);
        } else {
            return exportCustomersToExcel(customers);
        }
    }

    /**
     * 导出学校数据
     *
     * @param schools 学校列表
     * @param format 导出格式
     * @return 文件字节数组
     */
    public byte[] exportSchools(List<SchoolResponse> schools, String format) {
        if ("pdf".equalsIgnoreCase(format)) {
            return exportSchoolsToPdf(schools);
        } else {
            return exportSchoolsToExcel(schools);
        }
    }

    /**
     * 导出拜访记录到Excel
     *
     * @param visitRecords 拜访记录列表
     * @return Excel文件字节数组
     */
    private byte[] exportVisitRecordsToExcel(List<VisitRecordResponse> visitRecords) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("拜访记录");

            // 创建标题样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "序号", "客户姓名", "客户职位", "客户电话", "院系名称", "学校名称", "学校城市",
                    "销售人员", "销售部门", "拜访日期", "拜访时间", "拜访时长(分钟)", "拜访类型",
                    "拜访状态", "意向等级", "可用事项", "需求分析", "竞争对手分析", "下一步计划",
                    "拜访总结", "备注", "创建时间"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowIndex = 1;
            for (VisitRecordResponse record : visitRecords) {
                Row row = sheet.createRow(rowIndex++);
                int cellIndex = 0;

                // 序号
                createCell(row, cellIndex++, rowIndex - 1, dataStyle);
                // 客户信息
                createCell(row, cellIndex++, record.getCustomerName(), dataStyle);
                createCell(row, cellIndex++, record.getCustomerPosition(), dataStyle);
                createCell(row, cellIndex++, record.getCustomerPhone(), dataStyle);
                createCell(row, cellIndex++, record.getDepartmentName(), dataStyle);
                createCell(row, cellIndex++, record.getSchoolName(), dataStyle);
                createCell(row, cellIndex++, record.getSchoolCity(), dataStyle);
                // 销售信息
                createCell(row, cellIndex++, record.getSalesName(), dataStyle);
                createCell(row, cellIndex++, record.getSalesDepartment(), dataStyle);
                // 拜访信息
                createCell(row, cellIndex++, record.getVisitDate() != null ?
                        record.getVisitDate().format(DATE_FORMATTER) : "", dateStyle);
                createCell(row, cellIndex++, record.getVisitTime() != null ?
                        record.getVisitTime().toString() : "", dataStyle);
                createCell(row, cellIndex++, record.getDurationMinutes(), dataStyle);
                createCell(row, cellIndex++, record.getVisitTypeDescription(), dataStyle);
                createCell(row, cellIndex++, record.getStatusDescription(), dataStyle);
                createCell(row, cellIndex++, record.getIntentLevelDescription(), dataStyle);

                // 拜访详情 - 使用VisitRecordResponse中正确的字段名
                createCell(row, cellIndex++, record.getAvailableMatters(), dataStyle);
                createCell(row, cellIndex++, record.getDemandAnalysis(), dataStyle);
                createCell(row, cellIndex++, record.getCompetitorAnalysis(), dataStyle);
                createCell(row, cellIndex++, record.getNextSteps(), dataStyle);
                createCell(row, cellIndex++, record.getVisitSummary(), dataStyle);
                createCell(row, cellIndex++, record.getNotes(), dataStyle);

                createCell(row, cellIndex++, record.getCreatedAt() != null ?
                        record.getCreatedAt().format(DATETIME_FORMATTER) : "", dateStyle);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("导出拜访记录到Excel成功，记录数: {}", visitRecords.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("导出拜访记录到Excel失败", e);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出拜访记录到PDF
     *
     * @param visitRecords 拜访记录列表
     * @return PDF文件字节数组
     */
    private byte[] exportVisitRecordsToPdf(List<VisitRecordResponse> visitRecords) {
        // 简化实现，实际项目中可以使用iText等库
        return exportVisitRecordsToExcel(visitRecords);
    }

    /**
     * 导出客户数据到Excel
     *
     * @param customers 客户列表
     * @return Excel文件字节数组
     */
    private byte[] exportCustomersToExcel(List<CustomerResponse> customers) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("客户信息");

            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "序号", "客户姓名", "职位", "职称", "院系名称", "学校名称", "学校城市", "学校类型",
                    "电话", "微信号", "邮箱", "办公地点", "楼层房间", "研究方向", "影响力等级",
                    "决策权力", "生日", "拜访次数", "最近拜访日期", "最近意向等级", "微信添加",
                    "备注", "创建时间", "更新时间"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowIndex = 1;
            for (CustomerResponse customer : customers) {
                Row row = sheet.createRow(rowIndex++);
                int cellIndex = 0;

                createCell(row, cellIndex++, rowIndex - 1, dataStyle);
                createCell(row, cellIndex++, customer.getName(), dataStyle);
                createCell(row, cellIndex++, customer.getPosition(), dataStyle);
                createCell(row, cellIndex++, customer.getTitle(), dataStyle);
                createCell(row, cellIndex++, customer.getDepartmentName(), dataStyle);
                createCell(row, cellIndex++, customer.getSchoolName(), dataStyle);
                createCell(row, cellIndex++, customer.getSchoolCity(), dataStyle);
                createCell(row, cellIndex++, customer.getSchoolTypeDescription(), dataStyle);
                createCell(row, cellIndex++, customer.getPhone(), dataStyle);
                createCell(row, cellIndex++, customer.getWechat(), dataStyle);
                createCell(row, cellIndex++, customer.getEmail(), dataStyle);
                createCell(row, cellIndex++, customer.getOfficeLocation(), dataStyle);
                createCell(row, cellIndex++, customer.getFloorRoom(), dataStyle);
                createCell(row, cellIndex++, customer.getResearchDirection(), dataStyle);
                createCell(row, cellIndex++, customer.getInfluenceLevelDescription(), dataStyle);
                createCell(row, cellIndex++, customer.getDecisionPowerDescription(), dataStyle);
                createCell(row, cellIndex++, customer.getBirthday() != null ?
                        customer.getBirthday().format(DATE_FORMATTER) : "", dateStyle);
                createCell(row, cellIndex++, customer.getVisitCount(), dataStyle);
                createCell(row, cellIndex++, customer.getLastVisitDate() != null ?
                        customer.getLastVisitDate().format(DATE_FORMATTER) : "", dateStyle);
                createCell(row, cellIndex++, customer.getLastIntentLevel(), dataStyle);
                createCell(row, cellIndex++, Boolean.TRUE.equals(customer.getWechatAdded()) ? "是" : "否", dataStyle);
                createCell(row, cellIndex++, customer.getNotes(), dataStyle);
                createCell(row, cellIndex++, customer.getCreatedAt() != null ?
                        customer.getCreatedAt().format(DATETIME_FORMATTER) : "", dateStyle);
                createCell(row, cellIndex++, customer.getUpdatedAt() != null ?
                        customer.getUpdatedAt().format(DATETIME_FORMATTER) : "", dateStyle);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("导出客户数据到Excel成功，记录数: {}", customers.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("导出客户数据到Excel失败", e);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出客户数据到PDF
     *
     * @param customers 客户列表
     * @return PDF文件字节数组
     */
    private byte[] exportCustomersToPdf(List<CustomerResponse> customers) {
        // 简化实现
        return exportCustomersToExcel(customers);
    }

    /**
     * 导出学校数据到Excel
     *
     * @param schools 学校列表
     * @return Excel文件字节数组
     */
    private byte[] exportSchoolsToExcel(List<SchoolResponse> schools) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("学校信息");

            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "序号", "学校名称", "地址", "省份", "城市", "学校类型", "联系电话", "网站",
                    "院系数量", "客户数量", "拜访数量", "创建时间", "更新时间"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowIndex = 1;
            for (SchoolResponse school : schools) {
                Row row = sheet.createRow(rowIndex++);
                int cellIndex = 0;

                createCell(row, cellIndex++, rowIndex - 1, dataStyle);
                createCell(row, cellIndex++, school.getName(), dataStyle);
                createCell(row, cellIndex++, school.getAddress(), dataStyle);
                createCell(row, cellIndex++, school.getProvince(), dataStyle);
                createCell(row, cellIndex++, school.getCity(), dataStyle);
                createCell(row, cellIndex++, school.getSchoolTypeDescription(), dataStyle);
                createCell(row, cellIndex++, school.getContactPhone(), dataStyle);
                createCell(row, cellIndex++, school.getWebsite(), dataStyle);
                createCell(row, cellIndex++, school.getDepartmentCount(), dataStyle);
                createCell(row, cellIndex++, school.getCustomerCount(), dataStyle);
                createCell(row, cellIndex++, school.getVisitCount(), dataStyle);
                createCell(row, cellIndex++, school.getCreatedAt() != null ?
                        school.getCreatedAt().format(DATETIME_FORMATTER) : "", dateStyle);
                createCell(row, cellIndex++, school.getUpdatedAt() != null ?
                        school.getUpdatedAt().format(DATETIME_FORMATTER) : "", dateStyle);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("导出学校数据到Excel成功，记录数: {}", schools.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("导出学校数据到Excel失败", e);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出学校数据到PDF
     *
     * @param schools 学校列表
     * @return PDF文件字节数组
     */
    private byte[] exportSchoolsToPdf(List<SchoolResponse> schools) {
        // 简化实现
        return exportSchoolsToExcel(schools);
    }

    /**
     * 导入客户数据
     *
     * @param fileData 文件数据
     * @return 导入结果
     */
    public List<CustomerRequest> importCustomers(byte[] fileData) {
        List<CustomerRequest> customers = new ArrayList<>();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 跳过标题行
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) {
                    continue;
                }

                CustomerRequest customer = new CustomerRequest();
                try {
                    int cellIndex = 0;

                    // 跳过序号
                    cellIndex++;

                    customer.setName(getCellValueAsString(row.getCell(cellIndex++)));
                    customer.setPosition(getCellValueAsString(row.getCell(cellIndex++)));
                    customer.setTitle(getCellValueAsString(row.getCell(cellIndex++)));
                    // 这里应该设置departmentId，但从Excel中只能读取到名称
                    // 需要在服务层根据名称查找对应的ID
                    cellIndex++; // 跳过院系名称
                    cellIndex++; // 跳过学校名称
                    cellIndex++; // 跳过学校城市
                    cellIndex++; // 跳过学校类型
                    customer.setPhone(getCellValueAsString(row.getCell(cellIndex++)));
                    customer.setWechat(getCellValueAsString(row.getCell(cellIndex++)));
                    customer.setEmail(getCellValueAsString(row.getCell(cellIndex++)));
                    customer.setOfficeLocation(getCellValueAsString(row.getCell(cellIndex++)));
                    customer.setFloorRoom(getCellValueAsString(row.getCell(cellIndex++)));
                    customer.setResearchDirection(getCellValueAsString(row.getCell(cellIndex++)));
                    customer.setInfluenceLevel(parseInfluenceLevel(getCellValueAsString(row.getCell(cellIndex++))));
                    customer.setDecisionPower(parseDecisionPower(getCellValueAsString(row.getCell(cellIndex++))));
                    customer.setBirthday(parseDateFromCell(row.getCell(cellIndex++)));
                    // 跳过统计字段
                    cellIndex += 3; // 拜访次数、最近拜访日期、最近意向等级

                    String wechatAddedStr = getCellValueAsString(row.getCell(cellIndex++));
                    customer.setWechatAdded("是".equals(wechatAddedStr));

                    customer.setNotes(getCellValueAsString(row.getCell(cellIndex++)));

                    customers.add(customer);
                } catch (Exception e) {
                    log.warn("导入第{}行数据失败: {}", row.getRowNum() + 1, e.getMessage());
                }
            }

            log.info("导入客户数据成功，共导入{}条记录", customers.size());
            return customers;

        } catch (IOException e) {
            log.error("导入客户数据失败", e);
            throw new BusinessException("导入失败：" + e.getMessage());
        }
    }

    /**
     * 导入学校数据
     *
     * @param fileData 文件数据
     * @return 导入结果
     */
    public List<SchoolRequest> importSchools(byte[] fileData) {
        List<SchoolRequest> schools = new ArrayList<>();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 跳过标题行
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) {
                    continue;
                }

                SchoolRequest school = new SchoolRequest();
                try {
                    int cellIndex = 0;

                    // 跳过序号
                    cellIndex++;

                    school.setName(getCellValueAsString(row.getCell(cellIndex++)));
                    school.setAddress(getCellValueAsString(row.getCell(cellIndex++)));
                    school.setProvince(getCellValueAsString(row.getCell(cellIndex++)));
                    school.setCity(getCellValueAsString(row.getCell(cellIndex++)));
                    school.setSchoolTypes(parseSchoolTypes(getCellValueAsString(row.getCell(cellIndex++))));
                    school.setContactPhone(getCellValueAsString(row.getCell(cellIndex++)));
                    school.setWebsite(getCellValueAsString(row.getCell(cellIndex++)));

                    schools.add(school);
                } catch (Exception e) {
                    log.warn("导入第{}行数据失败: {}", row.getRowNum() + 1, e.getMessage());
                }
            }

            log.info("导入学校数据成功，共导入{}条记录", schools.size());
            return schools;

        } catch (IOException e) {
            log.error("导入学校数据失败", e);
            throw new BusinessException("导入失败：" + e.getMessage());
        }
    }

    /**
     * 创建标题样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建日期样式
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper creationHelper = workbook.getCreationHelper();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }

    /**
     * 创建单元格并设置值
     */
    private void createCell(Row row, int cellIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(cellIndex);

        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(style);
    }

    /**
     * 获取单元格值作为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DATETIME_FORMATTER);
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 从单元格解析日期
     */
    private LocalDate parseDateFromCell(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else {
                String dateStr = getCellValueAsString(cell);
                if (StringUtils.hasText(dateStr)) {
                    return LocalDate.parse(dateStr, DATE_FORMATTER);
                }
            }
        } catch (Exception e) {
            log.warn("解析日期失败: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 解析影响力等级
     */
    private String parseInfluenceLevel(String value) {
        if (!StringUtils.hasText(value)) {
            return Customer.InfluenceLevel.MEDIUM.name();
        }

        switch (value.trim()) {
            case "高":
                return Customer.InfluenceLevel.HIGH.name();
            case "中":
                return Customer.InfluenceLevel.MEDIUM.name();
            case "低":
                return Customer.InfluenceLevel.LOW.name();
            default:
                return Customer.InfluenceLevel.MEDIUM.name();
        }
    }

    /**
     * 解析决策权力
     */
    private String parseDecisionPower(String value) {
        if (!StringUtils.hasText(value)) {
            return Customer.DecisionPower.OTHER.name();
        }

        switch (value.trim()) {
            case "决策者":
                return Customer.DecisionPower.DECISION_MAKER.name();
            case "影响者":
                return Customer.DecisionPower.INFLUENCER.name();
            case "使用者":
                return Customer.DecisionPower.USER.name();
            default:
                return Customer.DecisionPower.OTHER.name();
        }
    }

    /**
     * 解析学校类型（修复版本 - 支持多个类型）
     */
    private List<String> parseSchoolTypes(String value) {
        if (!StringUtils.hasText(value)) {
            return Arrays.asList(School.SchoolType.REGULAR.name());
        }

        List<String> types = new ArrayList<>();
        String[] typeArray = value.split("[,，、]"); // 支持多种分隔符
        
        for (String type : typeArray) {
            String trimmedType = type.trim();
            switch (trimmedType) {
                case "985工程":
                    types.add(School.SchoolType.PROJECT_985.name());
                    break;
                case "211工程":
                    types.add(School.SchoolType.PROJECT_211.name());
                    break;
                case "双一流":
                    types.add(School.SchoolType.DOUBLE_FIRST_CLASS.name());
                    break;
                case "普通高校":
                    types.add(School.SchoolType.REGULAR.name());
                    break;
                default:
                    // 尝试直接解析枚举名
                    try {
                        School.SchoolType.valueOf(trimmedType.toUpperCase());
                        types.add(trimmedType.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // 无效类型，忽略或添加默认类型
                        if (types.isEmpty()) {
                            types.add(School.SchoolType.REGULAR.name());
                        }
                    }
                    break;
            }
        }
        
        return types.isEmpty() ? Arrays.asList(School.SchoolType.REGULAR.name()) : types;
    }

    /**
     * 解析学校类型（保持向后兼容）
     */
    private String parseSchoolType(String value) {
        List<String> types = parseSchoolTypes(value);
        return types.get(0); // 返回第一个类型
    }

    /**
     * 检查行是否为空
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK &&
                    StringUtils.hasText(getCellValueAsString(cell))) {
                return false;
            }
        }

        return true;
    }
}