package com.proshine.visitmanagement.service;

import com.proshine.visitmanagement.dto.request.VisitRecordRequest;
import com.proshine.visitmanagement.dto.response.PageResponse;
import com.proshine.visitmanagement.dto.response.VisitRecordResponse;
import com.proshine.visitmanagement.entity.Customer;
import com.proshine.visitmanagement.entity.User;
import com.proshine.visitmanagement.entity.VisitRecord;
import com.proshine.visitmanagement.exception.BusinessException;
import com.proshine.visitmanagement.exception.ResourceNotFoundException;
import com.proshine.visitmanagement.repository.CustomerRepository;
import com.proshine.visitmanagement.repository.UserRepository;
import com.proshine.visitmanagement.repository.VisitRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 拜访记录服务类
 *
 * @author System
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VisitRecordService {

    private final VisitRecordRepository visitRecordRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    /**
     * 分页查询拜访记录
     *
     * @param keyword 关键词
     * @param salesId 销售人员ID
     * @param customerId 客户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param status 状态
     * @param intentLevel 意向等级
     * @param pageable 分页参数
     * @param authentication 认证信息
     * @return 分页拜访记录列表
     */
    public PageResponse<VisitRecordResponse> getVisitRecords(String keyword, Long salesId, Long customerId,
                                                             LocalDate startDate, LocalDate endDate,
                                                             String status, String intentLevel,
                                                             Pageable pageable, Authentication authentication) {
        // 权限控制
        Long currentUserId = getCurrentUserId(authentication);
        User currentUser = getCurrentUser(authentication);

        // 非管理员只能查看自己的拜访记录
        if (currentUser.getRole() == User.UserRole.SALES && (salesId == null || !salesId.equals(currentUserId))) {
            salesId = currentUserId;
        }

        // 转换状态和意向等级
        VisitRecord.VisitStatus visitStatus = StringUtils.hasText(status) ?
                VisitRecord.VisitStatus.valueOf(status) : null;
        VisitRecord.IntentLevel visitIntentLevel = StringUtils.hasText(intentLevel) ?
                VisitRecord.IntentLevel.valueOf(intentLevel) : null;

        // 使用正确的参数顺序调用Repository方法
        Page<VisitRecord> visitPage = visitRecordRepository.findVisitRecordsWithFilters(
                keyword, salesId, customerId, startDate, endDate, visitStatus, visitIntentLevel, pageable);

        List<VisitRecordResponse> visitResponses = visitPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResponse.<VisitRecordResponse>builder()
                .content(visitResponses)
                .page(visitPage.getNumber())
                .size(visitPage.getSize())
                .totalElements(visitPage.getTotalElements())
                .totalPages(visitPage.getTotalPages())
                .first(visitPage.isFirst())
                .last(visitPage.isLast())
                .empty(visitPage.isEmpty())
                .build();
    }

    /**
     * 根据ID获取拜访记录详情
     *
     * @param id 拜访记录ID
     * @param authentication 认证信息
     * @return 拜访记录详情
     */
    public VisitRecordResponse getVisitRecordById(Long id, Authentication authentication) {
        VisitRecord visitRecord = visitRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("拜访记录不存在"));

        // 权限检查
        checkVisitRecordPermission(visitRecord, authentication);

        return convertToResponse(visitRecord);
    }

    /**
     * 创建拜访记录
     *
     * @param request 拜访记录请求
     * @param authentication 认证信息
     * @return 创建的拜访记录
     */
    @Transactional
    public VisitRecordResponse createVisitRecord(VisitRecordRequest request, Authentication authentication) {
        // 验证客户是否存在
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("客户不存在"));

        // 获取当前用户作为销售人员
        User currentUser = getCurrentUser(authentication);

        VisitRecord visitRecord = new VisitRecord();
        visitRecord.setCustomer(customer);
        visitRecord.setSales(currentUser);
        visitRecord.setVisitDate(request.getVisitDate());
        visitRecord.setVisitTime(request.getVisitTime());
        visitRecord.setDurationMinutes(request.getDurationMinutes());
        visitRecord.setVisitType(VisitRecord.VisitType.valueOf(request.getVisitType()));
        visitRecord.setStatus(VisitRecord.VisitStatus.valueOf(request.getStatus()));
        visitRecord.setIntentLevel(VisitRecord.IntentLevel.valueOf(request.getIntentLevel()));

        // 设置拜访详细内容 - 使用VisitRecord实体中实际存在的字段
        visitRecord.setBusinessItems(request.getBusinessItems());
        visitRecord.setPainPoints(request.getPainPoints());
        visitRecord.setCompetitors(request.getCompetitors());
        visitRecord.setBudgetRange(request.getBudgetRange());
        visitRecord.setDecisionTimeline(request.getDecisionTimeline());
        visitRecord.setNextStep(request.getNextStep());
        visitRecord.setFollowUpDate(request.getFollowUpDate());
        visitRecord.setNotes(request.getNotes());
        visitRecord.setMaterialsLeft(Boolean.TRUE.equals(request.getMaterialsLeft()));
        visitRecord.setWechatAdded(request.getWechatAdded());
        visitRecord.setRating(request.getRating());
        visitRecord.setLocation(request.getLocation());
        visitRecord.setWeather(request.getWeather());

        VisitRecord savedRecord = visitRecordRepository.save(visitRecord);
        log.info("创建拜访记录成功: ID={}, 客户ID={}", savedRecord.getId(), request.getCustomerId());

        return convertToResponse(savedRecord);
    }

    /**
     * 更新拜访记录
     *
     * @param id 拜访记录ID
     * @param request 拜访记录请求
     * @param authentication 认证信息
     * @return 更新的拜访记录
     */
    @Transactional
    public VisitRecordResponse updateVisitRecord(Long id, VisitRecordRequest request, Authentication authentication) {
        VisitRecord visitRecord = visitRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("拜访记录不存在"));

        // 权限检查
        checkVisitRecordPermission(visitRecord, authentication);

        // 验证客户是否存在
        if (!visitRecord.getCustomer().getId().equals(request.getCustomerId())) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("客户不存在"));
            visitRecord.setCustomer(customer);
        }

        visitRecord.setVisitDate(request.getVisitDate());
        visitRecord.setVisitTime(request.getVisitTime());
        visitRecord.setDurationMinutes(request.getDurationMinutes());
        visitRecord.setVisitType(VisitRecord.VisitType.valueOf(request.getVisitType()));
        visitRecord.setStatus(VisitRecord.VisitStatus.valueOf(request.getStatus()));
        visitRecord.setIntentLevel(VisitRecord.IntentLevel.valueOf(request.getIntentLevel()));

        // 更新拜访详细内容 - 使用VisitRecord实体中实际存在的字段
        visitRecord.setBusinessItems(request.getBusinessItems());
        visitRecord.setPainPoints(request.getPainPoints());
        visitRecord.setCompetitors(request.getCompetitors());
        visitRecord.setBudgetRange(request.getBudgetRange());
        visitRecord.setDecisionTimeline(request.getDecisionTimeline());
        visitRecord.setNextStep(request.getNextStep());
        visitRecord.setFollowUpDate(request.getFollowUpDate());
        visitRecord.setNotes(request.getNotes());
        visitRecord.setMaterialsLeft(Boolean.TRUE.equals(request.getMaterialsLeft()));
        visitRecord.setWechatAdded(request.getWechatAdded());
        visitRecord.setRating(request.getRating());
        visitRecord.setLocation(request.getLocation());
        visitRecord.setWeather(request.getWeather());

        VisitRecord savedRecord = visitRecordRepository.save(visitRecord);
        log.info("更新拜访记录成功: ID={}", id);

        return convertToResponse(savedRecord);
    }

    /**
     * 删除拜访记录
     *
     * @param id 拜访记录ID
     * @param authentication 认证信息
     */
    @Transactional
    public void deleteVisitRecord(Long id, Authentication authentication) {
        VisitRecord visitRecord = visitRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("拜访记录不存在"));

        // 权限检查
        checkVisitRecordPermission(visitRecord, authentication);

        visitRecordRepository.delete(visitRecord);
        log.info("删除拜访记录成功: ID={}", id);
    }

    /**
     * 批量删除拜访记录
     *
     * @param ids 拜访记录ID列表
     * @param authentication 认证信息
     * @return 删除的记录数量
     */
    @Transactional
    public int batchDeleteVisitRecords(List<Long> ids, Authentication authentication) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("删除ID列表不能为空");
        }

        List<VisitRecord> visitRecords = visitRecordRepository.findAllById(ids);
        if (visitRecords.isEmpty()) {
            throw new BusinessException("未找到要删除的拜访记录");
        }

        // 权限检查
        for (VisitRecord visitRecord : visitRecords) {
            checkVisitRecordPermission(visitRecord, authentication);
        }

        visitRecordRepository.deleteAll(visitRecords);
        log.info("批量删除拜访记录成功，数量: {}", visitRecords.size());

        return visitRecords.size();
    }

    /**
     * 导出拜访记录
     *
     * @param keyword 关键词
     * @param salesId 销售人员ID
     * @param customerId 客户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param status 状态
     * @param intentLevel 意向等级
     * @param format 导出格式
     * @param response HTTP响应
     * @param authentication 认证信息
     */
    public void exportVisitRecords(String keyword, Long salesId, Long customerId, LocalDate startDate,
                                   LocalDate endDate, String status, String intentLevel, String format,
                                   HttpServletResponse response, Authentication authentication) {
        try {
            // 权限控制
            Long currentUserId = getCurrentUserId(authentication);
            User currentUser = getCurrentUser(authentication);

            // 非管理员只能导出自己的拜访记录
            if (currentUser.getRole() == User.UserRole.SALES && (salesId == null || !salesId.equals(currentUserId))) {
                salesId = currentUserId;
            }

            // 转换状态和意向等级
            VisitRecord.VisitStatus visitStatus = StringUtils.hasText(status) ?
                    VisitRecord.VisitStatus.valueOf(status) : null;
            VisitRecord.IntentLevel visitIntentLevel = StringUtils.hasText(intentLevel) ?
                    VisitRecord.IntentLevel.valueOf(intentLevel) : null;

            // 查询拜访记录数据 - 使用无分页查询获取所有数据
            Page<VisitRecord> visitPage = visitRecordRepository.findVisitRecordsWithFilters(
                    keyword, salesId, customerId, startDate, endDate, visitStatus, visitIntentLevel,
                    PageRequest.of(0, Integer.MAX_VALUE));

            // 转换为响应对象
            List<VisitRecordResponse> visitRecordResponses = visitPage.getContent().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            // 设置响应头
            String fileName = String.format("拜访记录_%s.xlsx",
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setCharacterEncoding("UTF-8");

            // 生成Excel文件并写入响应
            exportVisitRecordsToExcel(visitRecordResponses, response);

            log.info("导出拜访记录成功: 记录数={}, 格式={}", visitRecordResponses.size(), format);

        } catch (Exception e) {
            log.error("导出拜访记录失败", e);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 更新拜访状态
     *
     * @param id 拜访记录ID
     * @param status 新状态
     * @param authentication 认证信息
     */
    @Transactional
    public void updateVisitStatus(Long id, String status, Authentication authentication) {
        VisitRecord visitRecord = visitRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("拜访记录不存在"));

        // 权限检查
        checkVisitRecordPermission(visitRecord, authentication);

        visitRecord.setStatus(VisitRecord.VisitStatus.valueOf(status));
        visitRecordRepository.save(visitRecord);

        log.info("更新拜访状态成功: ID={}, 新状态={}", id, status);
    }

    /**
     * 复制拜访记录
     *
     * @param id 原拜访记录ID
     * @param authentication 认证信息
     * @return 复制的拜访记录
     */
    @Transactional
    public VisitRecordResponse copyVisitRecord(Long id, Authentication authentication) {
        VisitRecord originalRecord = visitRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("拜访记录不存在"));

        // 权限检查
        checkVisitRecordPermission(originalRecord, authentication);

        VisitRecord newRecord = new VisitRecord();
        newRecord.setCustomer(originalRecord.getCustomer());
        newRecord.setSales(originalRecord.getSales());
        newRecord.setVisitDate(LocalDate.now()); // 设置为当前日期
        newRecord.setVisitTime(originalRecord.getVisitTime());
        newRecord.setDurationMinutes(originalRecord.getDurationMinutes());
        newRecord.setVisitType(originalRecord.getVisitType());
        newRecord.setStatus(VisitRecord.VisitStatus.SCHEDULED); // 设置为已安排状态
        newRecord.setIntentLevel(originalRecord.getIntentLevel());

        // 复制拜访详细内容 - 使用VisitRecord实体中实际存在的字段
        newRecord.setBusinessItems(originalRecord.getBusinessItems());
        newRecord.setPainPoints(originalRecord.getPainPoints());
        newRecord.setCompetitors(originalRecord.getCompetitors());
        newRecord.setBudgetRange(originalRecord.getBudgetRange());
        newRecord.setDecisionTimeline(originalRecord.getDecisionTimeline());
        newRecord.setNextStep(originalRecord.getNextStep());
        newRecord.setFollowUpDate(null); // 清空跟进日期
        newRecord.setNotes(null); // 清空备注
        newRecord.setMaterialsLeft(originalRecord.getMaterialsLeft());
        newRecord.setWechatAdded(originalRecord.getWechatAdded());
        newRecord.setRating(null); // 清空评分
        newRecord.setLocation(originalRecord.getLocation());
        newRecord.setWeather(null); // 清空天气

        VisitRecord savedRecord = visitRecordRepository.save(newRecord);
        log.info("复制拜访记录成功: 原记录ID={}, 新记录ID={}", id, savedRecord.getId());

        return convertToResponse(savedRecord);
    }

    /**
     * 根据客户ID获取拜访记录
     *
     * @param customerId 客户ID
     * @param authentication 认证信息
     * @return 拜访记录列表
     */
    public List<VisitRecordResponse> getVisitRecordsByCustomer(Long customerId, Authentication authentication) {
        // 验证客户是否存在
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("客户不存在");
        }

        // 权限控制
        Long currentUserId = getCurrentUserId(authentication);
        User currentUser = getCurrentUser(authentication);

        List<VisitRecord> visitRecords;
        if (currentUser.getRole() == User.UserRole.SALES) {
            visitRecords = visitRecordRepository.findByCustomerIdAndSalesIdOrderByVisitDateDesc(customerId, currentUserId);
        } else {
            visitRecords = visitRecordRepository.findByCustomerIdOrderByVisitDateDesc(customerId);
        }

        return visitRecords.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取拜访记录统计
     *
     * @param salesId 销售人员ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param authentication 认证信息
     * @return 统计数据
     */
    public Map<String, Object> getVisitRecordStatistics(Long salesId, LocalDate startDate, LocalDate endDate,
                                                        Authentication authentication) {
        // 权限控制
        Long currentUserId = getCurrentUserId(authentication);
        User currentUser = getCurrentUser(authentication);

        // 非管理员只能查看自己的统计
        if (currentUser.getRole() == User.UserRole.SALES && (salesId == null || !salesId.equals(currentUserId))) {
            salesId = currentUserId;
        }

        Map<String, Object> statistics = new HashMap<>();

        // 如果没有指定日期范围，默认查询当月
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        }

        // 总拜访记录数
        long totalVisits = salesId != null ?
                visitRecordRepository.countBySalesIdAndVisitDateBetween(salesId, startDate, endDate) :
                visitRecordRepository.countByVisitDateBetween(startDate, endDate);
        statistics.put("totalVisits", totalVisits);

        // 各状态拜访记录统计
        for (VisitRecord.VisitStatus status : VisitRecord.VisitStatus.values()) {
            long count = salesId != null ?
                    visitRecordRepository.countBySalesIdAndStatusAndVisitDateBetween(salesId, status, startDate, endDate) :
                    visitRecordRepository.countByStatusAndVisitDateBetween(status, startDate, endDate);
            statistics.put(status.name().toLowerCase() + "Visits", count);
        }

        // 各意向等级拜访记录统计
        for (VisitRecord.IntentLevel intentLevel : VisitRecord.IntentLevel.values()) {
            long count = salesId != null ?
                    visitRecordRepository.countBySalesIdAndIntentLevelAndVisitDateBetween(salesId, intentLevel, startDate, endDate) :
                    visitRecordRepository.countByIntentLevelAndVisitDateBetween(intentLevel, startDate, endDate);
            statistics.put(intentLevel.name().toLowerCase() + "Intent", count);
        }

        return statistics;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("当前用户不存在"));
        return user.getId();
    }

    /**
     * 获取当前用户
     */
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("当前用户不存在"));
    }

    /**
     * 检查拜访记录权限
     */
    private void checkVisitRecordPermission(VisitRecord visitRecord, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        // 管理员有所有权限
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            return;
        }

        // 经理可以查看同部门销售的记录
        if (currentUser.getRole() == User.UserRole.MANAGER) {
            User sales = visitRecord.getSales();
            if (StringUtils.hasText(currentUser.getDepartment()) &&
                    currentUser.getDepartment().equals(sales.getDepartment()) &&
                    sales.getRole() == User.UserRole.SALES) {
                return;
            }
        }

        // 销售人员只能操作自己的记录
        if (!visitRecord.getSales().getId().equals(currentUser.getId())) {
            throw new BusinessException("无权限操作此拜访记录");
        }
    }

    /**
     * 转换为响应对象
     */
    private VisitRecordResponse convertToResponse(VisitRecord visitRecord) {
        Customer customer = visitRecord.getCustomer();
        User sales = visitRecord.getSales();

        return VisitRecordResponse.builder()
                // 基础信息
                .id(visitRecord.getId())

                // 客户信息
                .customerId(customer.getId())
                .customerName(customer.getName())
                .customerPosition(customer.getPosition())
                .customerPhone(customer.getPhone())
                .customerEmail(customer.getEmail())
                .customerInfluenceLevel(customer.getInfluenceLevel() != null ? customer.getInfluenceLevel().name() : null)
                .customerDecisionPower(customer.getDecisionPower() != null ? customer.getDecisionPower().name() : null)

                // 院系和学校信息
                .departmentId(customer.getDepartment() != null ? customer.getDepartment().getId() : null)
                .departmentName(customer.getDepartment() != null ? customer.getDepartment().getName() : null)
                .schoolId(customer.getDepartment() != null && customer.getDepartment().getSchool() != null ?
                        customer.getDepartment().getSchool().getId() : null)
                .schoolName(customer.getDepartment() != null && customer.getDepartment().getSchool() != null ?
                        customer.getDepartment().getSchool().getName() : null)
                .schoolCity(customer.getDepartment() != null && customer.getDepartment().getSchool() != null ?
                        customer.getDepartment().getSchool().getCity() : null)

                // 销售人员信息
                .salesId(sales.getId())
                .salesName(sales.getRealName())
                .salesDepartment(sales.getDepartment())

                // 拜访基础信息
                .visitDate(visitRecord.getVisitDate())
                .visitTime(visitRecord.getVisitTime())
                .durationMinutes(visitRecord.getDurationMinutes())
                .visitType(visitRecord.getVisitType() != null ? visitRecord.getVisitType().name() : null)
                .visitTypeDescription(visitRecord.getVisitType() != null ? visitRecord.getVisitType().getDescription() : null)
                .status(visitRecord.getStatus() != null ? visitRecord.getStatus().name() : null)
                .statusDescription(visitRecord.getStatus() != null ? visitRecord.getStatus().getDescription() : null)
                .intentLevel(visitRecord.getIntentLevel() != null ? visitRecord.getIntentLevel().name() : null)
                .intentLevelDescription(visitRecord.getIntentLevel() != null ? visitRecord.getIntentLevel().getDescription() : null)

                // 拜访详细内容 - 映射VisitRecord字段到VisitRecordResponse字段
                .availableMatters(visitRecord.getBusinessItems()) // businessItems -> availableMatters
                .demandAnalysis(visitRecord.getPainPoints()) // painPoints -> demandAnalysis
                .competitorAnalysis(visitRecord.getCompetitors()) // competitors -> competitorAnalysis
                .nextSteps(visitRecord.getNextStep()) // nextStep -> nextSteps
                .visitSummary(visitRecord.getNotes()) // notes -> visitSummary (暂时映射)
                .notes(visitRecord.getNotes())

                // 新增字段 - 直接映射VisitRecord的原始字段
                .businessItems(visitRecord.getBusinessItems())
                .painPoints(visitRecord.getPainPoints())
                .competitors(visitRecord.getCompetitors())
                .budgetRange(visitRecord.getBudgetRange())
                .decisionTimeline(visitRecord.getDecisionTimeline())
                .nextStep(visitRecord.getNextStep())
                .followUpDate(visitRecord.getFollowUpDate())
                .materialsLeft(visitRecord.getMaterialsLeft())
                .wechatAdded(visitRecord.getWechatAdded())
                .rating(visitRecord.getRating())
                .location(visitRecord.getLocation())
                .weather(visitRecord.getWeather())
                // 时间戳
                .createdAt(visitRecord.getCreatedAt())
                .updatedAt(visitRecord.getUpdatedAt())
                .build();
    }

    /**
     * 导出拜访记录到Excel
     *
     * @param visitRecords 拜访记录列表
     * @param response HTTP响应
     */
    private void exportVisitRecordsToExcel(List<VisitRecordResponse> visitRecords, HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("拜访记录");

            // 创建样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 创建表头 - 使用VisitRecordResponse中实际存在的字段
            String[] headers = {
                    "ID", "客户姓名", "客户职位", "销售人员", "院系", "学校", "城市",
                    "拜访日期", "拜访时间", "时长(分钟)", "拜访类型", "状态", "意向等级",
                    "可用事项", "需求分析", "竞争对手分析", "下一步计划", "拜访总结", "备注", "创建时间"
            };

            Row headerRow = sheet.createRow(0);
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

                // 基础信息
                createCell(row, cellIndex++, record.getId(), dataStyle);
                createCell(row, cellIndex++, record.getCustomerName(), dataStyle);
                createCell(row, cellIndex++, record.getCustomerPosition(), dataStyle);
                createCell(row, cellIndex++, record.getSalesName(), dataStyle);
                createCell(row, cellIndex++, record.getDepartmentName(), dataStyle);
                createCell(row, cellIndex++, record.getSchoolName(), dataStyle);
                createCell(row, cellIndex++, record.getSchoolCity(), dataStyle);

                // 拜访信息
                createCell(row, cellIndex++, record.getVisitDate() != null ?
                        record.getVisitDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "", dataStyle);
                createCell(row, cellIndex++, record.getVisitTime() != null ? record.getVisitTime().toString() : "", dataStyle);
                createCell(row, cellIndex++, record.getDurationMinutes(), dataStyle);
                createCell(row, cellIndex++, record.getVisitTypeDescription(), dataStyle);
                createCell(row, cellIndex++, record.getStatusDescription(), dataStyle);
                createCell(row, cellIndex++, record.getIntentLevelDescription(), dataStyle);

                // 拜访详情
                createCell(row, cellIndex++, record.getAvailableMatters(), dataStyle);
                createCell(row, cellIndex++, record.getDemandAnalysis(), dataStyle);
                createCell(row, cellIndex++, record.getCompetitorAnalysis(), dataStyle);
                createCell(row, cellIndex++, record.getNextSteps(), dataStyle);
                createCell(row, cellIndex++, record.getVisitSummary(), dataStyle);
                createCell(row, cellIndex++, record.getNotes(), dataStyle);
                createCell(row, cellIndex++, record.getCreatedAt() != null ?
                        record.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "", dataStyle);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 设置最大列宽
                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            // 写入响应
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();

        } catch (IOException e) {
            log.error("导出拜访记录到Excel失败", e);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 创建单元格并设置值和样式
     */
    private void createCell(Row row, int cellIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(cellIndex);
        if (value != null) {
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                cell.setCellValue(value.toString());
            }
        }
        cell.setCellStyle(style);
    }
}
