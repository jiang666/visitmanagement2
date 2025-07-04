package com.proshine.visitmanagement.controller;

import com.proshine.visitmanagement.dto.request.CustomerRequest;
import com.proshine.visitmanagement.dto.response.ApiResponse;
import com.proshine.visitmanagement.dto.response.CustomerResponse;
import com.proshine.visitmanagement.dto.response.PageResponse;
import com.proshine.visitmanagement.entity.Customer;
import com.proshine.visitmanagement.service.CustomerService;
import com.proshine.visitmanagement.util.DateUtils;
import com.proshine.visitmanagement.util.ExcelUtils;
import com.proshine.visitmanagement.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 客户控制器
 * 处理客户相关的HTTP请求
 *
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 分页查询客户
     *
     * @param keyword 关键词
     * @param departmentId 院系ID
     * @param schoolId 学校ID
     * @param schoolCity 学校城市
     * @param influenceLevel 影响力等级
     * @param decisionPower 决策权力
     * @param hasWechat 是否有微信号
     * @param pageable 分页参数
     * @param authentication 认证信息
     * @return 分页客户列表
     */
    @GetMapping
    public ApiResponse<PageResponse<CustomerResponse>> getCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String schoolCity,
            @RequestParam(required = false) String influenceLevel,
            @RequestParam(required = false) String decisionPower,
            @RequestParam(required = false) Boolean hasWechat,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        log.debug("分页查询客户: keyword={}, departmentId={}, schoolId={}, page={}, size={}",
                keyword, departmentId, schoolId, pageable.getPageNumber(), pageable.getPageSize());

        PageResponse<CustomerResponse> customers = customerService.getCustomers(
                keyword, departmentId, schoolId, schoolCity, influenceLevel,
                decisionPower, hasWechat, pageable, authentication);

        return ApiResponse.success(customers);
    }

    /**
     * 根据ID获取客户详情
     *
     * @param id 客户ID
     * @param authentication 认证信息
     * @return 客户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> getCustomer(
            @PathVariable @NotNull Long id,
            Authentication authentication) {

        log.debug("获取客户详情: id={}", id);

        CustomerResponse customer = customerService.getCustomer(id, authentication);

        return ApiResponse.success(customer);
    }

    /**
     * 创建客户
     *
     * @param request 客户请求
     * @param authentication 认证信息
     * @return 创建的客户
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request,
            Authentication authentication) {

        log.info("创建客户: name={}", request.getName());

        CustomerResponse customer = customerService.createCustomer(request, authentication);

        log.info("客户创建成功: id={}, name={}", customer.getId(), customer.getName());

        return ApiResponse.success(customer, "客户创建成功");
    }

    /**
     * 更新客户
     *
     * @param id 客户ID
     * @param request 客户请求
     * @param authentication 认证信息
     * @return 更新的客户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<CustomerResponse> updateCustomer(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody CustomerRequest request,
            Authentication authentication) {

        log.info("更新客户: id={}, name={}", id, request.getName());

        CustomerResponse customer = customerService.updateCustomer(id, request, authentication);

        log.info("客户更新成功: id={}, name={}", customer.getId(), customer.getName());

        return ApiResponse.success(customer, "客户更新成功");
    }

    /**
     * 删除客户
     *
     * @param id 客户ID
     * @param authentication 认证信息
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Void> deleteCustomer(
            @PathVariable @NotNull Long id,
            Authentication authentication) {

        log.info("删除客户: id={}", id);

        customerService.deleteCustomer(id, authentication);

        log.info("客户删除成功: id={}", id);

        return ApiResponse.success("客户删除成功");
    }

    /**
     * 批量删除客户
     *
     * @param ids 客户ID列表
     * @param authentication 认证信息
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Void> batchDeleteCustomers(
            @RequestBody List<Long> ids,
            Authentication authentication) {

        log.info("批量删除客户: ids={}", ids);

        ValidationUtils.notEmpty(ids, "ids");
        ValidationUtils.collectionSize(ids, 1, 50, "ids");

        int deletedCount = customerService.batchDeleteCustomers(ids, authentication);

        log.info("批量删除客户成功: 删除数量={}", deletedCount);

        return ApiResponse.success(String.format("成功删除%d个客户", deletedCount));
    }

    /**
     * 导出客户列表
     *
     * @param keyword 关键词
     * @param departmentId 院系ID
     * @param schoolId 学校ID
     * @param schoolCity 学校城市
     * @param influenceLevel 影响力等级
     * @param decisionPower 决策权力
     * @param hasWechat 是否有微信号
     * @param response HTTP响应
     * @param authentication 认证信息
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public void exportCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String schoolCity,
            @RequestParam(required = false) String influenceLevel,
            @RequestParam(required = false) String decisionPower,
            @RequestParam(required = false) Boolean hasWechat,
            HttpServletResponse response,
            Authentication authentication) {

        log.info("导出客户列表: keyword={}, departmentId={}, schoolId={}", keyword, departmentId, schoolId);

        List<CustomerResponse> customers = customerService.getCustomersForExport(
                keyword, departmentId, schoolId, schoolCity,
                influenceLevel, decisionPower, hasWechat, authentication);

        // 定义导出表头
        LinkedHashMap<String, String> headers = createExportHeaders();

        String fileName = String.format("客户列表_%s.xlsx", DateUtils.format(LocalDate.now(), "yyyyMMdd"));

        ExcelUtils.exportToExcel(customers, headers, fileName, response);

        log.info("客户列表导出成功: 客户数量={}", customers.size());
    }

    /**
     * 获取客户统计信息
     *
     * @param authentication 认证信息
     * @return 客户统计
     */
    @GetMapping("/statistics")
    public ApiResponse<Object> getCustomerStatistics(Authentication authentication) {
        log.debug("获取客户统计信息");

        Object statistics = customerService.getCustomerStatistics(authentication);

        return ApiResponse.success(statistics, "获取客户统计成功");
    }

    /**
     * 批量导入客户
     *
     * @param file Excel文件
     * @param authentication 认证信息
     * @return 导入结果
     */
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Object> importCustomers(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        log.info("批量导入客户: fileName={}", file.getOriginalFilename());

        // 验证文件
        ValidationUtils.fileName(file.getOriginalFilename());

        String[] allowedTypes = {".xlsx", ".xls"};
        ValidationUtils.fileExtension(file.getOriginalFilename(), allowedTypes);

        Object importResult = customerService.importCustomersFromExcel(file, authentication);

        log.info("客户批量导入完成");

        return ApiResponse.success(importResult, "客户导入成功");
    }

    /**
     * 下载客户导入模板
     *
     * @param response HTTP响应
     */
    @GetMapping("/import-template")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void downloadImportTemplate(HttpServletResponse response) {
        log.info("下载客户导入模板");

        LinkedHashMap<String, String> headers = createImportTemplateHeaders();
        String fileName = "客户导入模板.xlsx";

        ExcelUtils.createImportTemplate(headers, fileName, response);

        log.info("客户导入模板下载完成");
    }

    /**
     * 客户合并
     *
     * @param sourceId 源客户ID
     * @param targetId 目标客户ID
     * @param authentication 认证信息
     * @return 合并结果
     */
    @PostMapping("/{sourceId}/merge/{targetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Void> mergeCustomers(
            @PathVariable @NotNull Long sourceId,
            @PathVariable @NotNull Long targetId,
            Authentication authentication) {

        log.info("合并客户: sourceId={}, targetId={}", sourceId, targetId);

        customerService.mergeCustomers(sourceId, targetId, authentication);

        log.info("客户合并成功: sourceId={}, targetId={}", sourceId, targetId);

        return ApiResponse.success("客户合并成功");
    }

    /**
     * 创建导出表头映射
     *
     * @return 表头映射
     */
    private LinkedHashMap<String, String> createExportHeaders() {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "客户ID");
        headers.put("name", "客户姓名");
        headers.put("position", "职位");
        headers.put("title", "职称");
        headers.put("departmentName", "院系名称");
        headers.put("schoolName", "学校名称");
        headers.put("schoolCity", "学校城市");
        headers.put("schoolTypeDescription", "学校类型");
        headers.put("phone", "手机号");
        headers.put("wechat", "微信号");
        headers.put("email", "邮箱");
        headers.put("officeLocation", "办公地点");
        headers.put("floorRoom", "楼层房间");
        headers.put("researchDirection", "研究方向");
        headers.put("influenceLevelDescription", "影响力等级");
        headers.put("decisionPowerDescription", "决策权力");
        headers.put("birthday", "生日");
        headers.put("visitCount", "拜访次数");
        headers.put("lastVisitDate", "最后拜访日期");
        headers.put("createdAt", "创建时间");
        return headers;
    }

    /**
     * 创建导入模板表头映射
     *
     * @return 表头映射
     */
    private LinkedHashMap<String, String> createImportTemplateHeaders() {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("name", "客户姓名*");
        headers.put("position", "职位");
        headers.put("title", "职称");
        headers.put("schoolName", "学校名称*");
        headers.put("departmentName", "院系名称*");
        headers.put("phone", "手机号");
        headers.put("wechat", "微信号");
        headers.put("email", "邮箱");
        headers.put("officeLocation", "办公地点");
        headers.put("floorRoom", "楼层房间");
        headers.put("researchDirection", "研究方向");
        headers.put("influenceLevel", "影响力等级(HIGH/MEDIUM/LOW)");
        headers.put("decisionPower", "决策权力(DECISION_MAKER/INFLUENCER/USER/OTHER)");
        headers.put("birthday", "生日(yyyy-MM-dd)");
        headers.put("notes", "备注");
        return headers;
    }
}