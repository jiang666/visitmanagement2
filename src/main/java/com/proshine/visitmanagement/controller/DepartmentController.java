package com.proshine.visitmanagement.controller;

import com.proshine.visitmanagement.dto.request.DepartmentRequest;
import com.proshine.visitmanagement.dto.response.ApiResponse;
import com.proshine.visitmanagement.dto.response.DepartmentResponse;
import com.proshine.visitmanagement.dto.response.PageResponse;
import com.proshine.visitmanagement.service.DepartmentService;
import com.proshine.visitmanagement.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 院系控制器
 * 处理院系相关的HTTP请求
 *
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Validated
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 分页查询院系
     */
    @GetMapping
    public ApiResponse<PageResponse<DepartmentResponse>> getDepartments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long schoolId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        log.debug("分页查询院系: keyword={}, schoolId={}, page={}, size={}",
                keyword, schoolId, pageable.getPageNumber(), pageable.getPageSize());

        PageResponse<DepartmentResponse> departments = departmentService.getDepartments(
                keyword, schoolId, pageable, authentication);

        return ApiResponse.success(departments);
    }

    /**
     * 根据ID获取院系详情
     */
    @GetMapping("/{id}")
    public ApiResponse<DepartmentResponse> getDepartment(
            @PathVariable @NotNull Long id,
            Authentication authentication) {

        log.debug("获取院系详情: id={}", id);

        DepartmentResponse department = departmentService.getDepartment(id, authentication);

        return ApiResponse.success(department);
    }

    /**
     * 根据学校ID获取院系列表
     */
    @GetMapping("/by-school/{schoolId}")
    public ApiResponse<List<DepartmentResponse>> getDepartmentsBySchool(
            @PathVariable @NotNull Long schoolId,
            Authentication authentication) {

        log.debug("获取学校院系列表: schoolId={}", schoolId);

        List<DepartmentResponse> departments = departmentService.getDepartmentsBySchool(schoolId, authentication);

        return ApiResponse.success(departments);
    }

    /**
     * 创建院系
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest request,
            Authentication authentication) {

        log.info("创建院系: name={}, schoolId={}", request.getName(), request.getSchoolId());

        DepartmentResponse department = departmentService.createDepartment(request, authentication);

        log.info("院系创建成功: id={}, name={}", department.getId(), department.getName());

        return ApiResponse.success(department, "院系创建成功");
    }

    /**
     * 更新院系
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<DepartmentResponse> updateDepartment(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody DepartmentRequest request,
            Authentication authentication) {

        log.info("更新院系: id={}, name={}", id, request.getName());

        DepartmentResponse department = departmentService.updateDepartment(id, request, authentication);

        log.info("院系更新成功: id={}, name={}", department.getId(), department.getName());

        return ApiResponse.success(department, "院系更新成功");
    }

    /**
     * 删除院系
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Void> deleteDepartment(
            @PathVariable @NotNull Long id,
            Authentication authentication) {

        log.info("删除院系: id={}", id);

        departmentService.deleteDepartment(id, authentication);

        log.info("院系删除成功: id={}", id);

        return ApiResponse.success("院系删除成功");
    }

    /**
     * 批量删除院系
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Void> batchDeleteDepartments(
            @RequestBody List<Long> ids,
            Authentication authentication) {

        log.info("批量删除院系: ids={}", ids);

        ValidationUtils.notEmpty(ids, "ids");
        ValidationUtils.collectionSize(ids, 1, 50, "ids");

        int deletedCount = departmentService.batchDeleteDepartments(ids, authentication);

        log.info("批量删除院系成功: 删除数量={}", deletedCount);

        return ApiResponse.success(String.format("成功删除%d个院系", deletedCount));
    }
}