package com.proshine.visitmanagement.controller;

import com.proshine.visitmanagement.dto.request.VisitRecordRequest;
import com.proshine.visitmanagement.dto.response.ApiResponse;
import com.proshine.visitmanagement.dto.response.PageResponse;
import com.proshine.visitmanagement.dto.response.VisitRecordResponse;
import com.proshine.visitmanagement.service.VisitRecordService;
import com.proshine.visitmanagement.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 拜访记录控制器
 * 处理拜访记录的增删改查等操作
 *
 * @author System
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/visit-records")
@RequiredArgsConstructor
@Validated
@Slf4j
public class VisitRecordController {

    private final VisitRecordService visitRecordService;

    /**
     * 分页查询拜访记录
     */
    @GetMapping
    public ApiResponse<PageResponse<VisitRecordResponse>> getVisitRecords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long salesId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String intentLevel,
            @PageableDefault(size = 20, sort = "visitDate", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication) {

        log.debug("分页查询拜访记录: keyword={}, salesId={}, customerId={}, page={}, size={}",
                keyword, salesId, customerId, pageable.getPageNumber(), pageable.getPageSize());

        PageResponse<VisitRecordResponse> visitRecords = visitRecordService.getVisitRecords(
                keyword, salesId, customerId, startDate, endDate, status, intentLevel, pageable, authentication);

        return ApiResponse.success(visitRecords);
    }

    /**
     * 根据ID获取拜访记录详情
     */
    @GetMapping("/{id}")
    public ApiResponse<VisitRecordResponse> getVisitRecord(
            @PathVariable @NotNull Long id,
            Authentication authentication) {

        log.debug("获取拜访记录详情: id={}", id);

        VisitRecordResponse visitRecord = visitRecordService.getVisitRecordById(id, authentication);

        return ApiResponse.success(visitRecord, "获取拜访记录详情成功");
    }

    /**
     * 创建拜访记录
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<VisitRecordResponse> createVisitRecord(
            @Valid @RequestBody VisitRecordRequest request,
            Authentication authentication) {

        log.info("创建拜访记录: customerId={}, visitDate={}, visitType={}",
                request.getCustomerId(), request.getVisitDate(), request.getVisitType());

        VisitRecordResponse visitRecord = visitRecordService.createVisitRecord(request, authentication);

        log.info("拜访记录创建成功: id={}, customerId={}", visitRecord.getId(), request.getCustomerId());

        return ApiResponse.success(visitRecord, "拜访记录创建成功");
    }

    /**
     * 更新拜访记录
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<VisitRecordResponse> updateVisitRecord(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody VisitRecordRequest request,
            Authentication authentication) {

        log.info("更新拜访记录: id={}, customerId={}", id, request.getCustomerId());

        VisitRecordResponse visitRecord = visitRecordService.updateVisitRecord(id, request, authentication);

        log.info("拜访记录更新成功: id={}", id);

        return ApiResponse.success(visitRecord, "拜访记录更新成功");
    }

    /**
     * 删除拜访记录
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<Void> deleteVisitRecord(
            @PathVariable @NotNull Long id,
            Authentication authentication) {

        log.info("删除拜访记录: id={}", id);

        visitRecordService.deleteVisitRecord(id, authentication);

        log.info("拜访记录删除成功: id={}", id);

        return ApiResponse.success("拜访记录删除成功");
    }

    /**
     * 批量删除拜访记录
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Void> batchDeleteVisitRecords(
            @RequestBody List<Long> ids,
            Authentication authentication) {

        log.info("批量删除拜访记录: ids={}", ids);

        ValidationUtils.notEmpty(ids, "ids");
        ValidationUtils.collectionSize(ids, 1, 50, "ids");

        int deletedCount = visitRecordService.batchDeleteVisitRecords(ids, authentication);

        log.info("批量删除拜访记录成功: 删除数量={}", deletedCount);

        return ApiResponse.success(String.format("成功删除%d条拜访记录", deletedCount));
    }

    /**
     * 导出拜访记录
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public void exportVisitRecords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long salesId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String intentLevel,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletResponse response,
            Authentication authentication) {

        log.info("导出拜访记录: keyword={}, salesId={}, customerId={}, format={}",
                keyword, salesId, customerId, format);

        ValidationUtils.exportFormat(format, Arrays.asList("excel", "pdf"));

        visitRecordService.exportVisitRecords(keyword, salesId, customerId, startDate,
                endDate, status, intentLevel, format, response, authentication);

        log.info("拜访记录导出完成");
    }

    /**
     * 获取拜访记录统计
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getVisitRecordStatistics(
            @RequestParam(required = false) Long salesId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Authentication authentication) {

        log.debug("获取拜访记录统计: salesId={}, startDate={}, endDate={}", salesId, startDate, endDate);

        Map<String, Object> statistics = visitRecordService.getVisitRecordStatistics(
                salesId, startDate, endDate, authentication);

        return ApiResponse.success(statistics, "获取拜访记录统计成功");
    }

    /**
     * 更新拜访状态
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<Void> updateVisitStatus(
            @PathVariable @NotNull Long id,
            @RequestParam String status,
            Authentication authentication) {

        log.info("更新拜访状态: id={}, status={}", id, status);

        ValidationUtils.visitStatus(status);

        visitRecordService.updateVisitStatus(id, status, authentication);

        log.info("拜访状态更新成功: id={}, status={}", id, status);

        return ApiResponse.success("拜访状态更新成功");
    }

    /**
     * 复制拜访记录
     */
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public ApiResponse<VisitRecordResponse> copyVisitRecord(
            @PathVariable @NotNull Long id,
            Authentication authentication) {

        log.info("复制拜访记录: sourceId={}", id);

        VisitRecordResponse copiedRecord = visitRecordService.copyVisitRecord(id, authentication);

        log.info("拜访记录复制成功: sourceId={}, newId={}", id, copiedRecord.getId());

        return ApiResponse.success(copiedRecord, "拜访记录复制成功");
    }

    /**
     * 根据客户ID获取拜访记录
     */
    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<VisitRecordResponse>> getVisitRecordsByCustomer(
            @PathVariable @NotNull Long customerId,
            Authentication authentication) {

        log.debug("获取客户拜访记录: customerId={}", customerId);

        List<VisitRecordResponse> visitRecords = visitRecordService.getVisitRecordsByCustomer(customerId, authentication);

        return ApiResponse.success(visitRecords, "获取客户拜访记录成功");
    }
}