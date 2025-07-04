package com.proshine.visitmanagement.controller;

import com.proshine.visitmanagement.dto.request.UserRequest;
import com.proshine.visitmanagement.dto.response.ApiResponse;
import com.proshine.visitmanagement.dto.response.PageResponse;
import com.proshine.visitmanagement.dto.response.UserResponse;
import com.proshine.visitmanagement.entity.User;
import com.proshine.visitmanagement.service.UserService;
import com.proshine.visitmanagement.util.ExcelUtils;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求
 *
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<PageResponse<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("分页查询用户: keyword={}, role={}, department={}, status={}, page={}, size={}",
                keyword, role, department, status, pageable.getPageNumber(), pageable.getPageSize());

        User.UserRole userRole = null;
        if (role != null && !role.isEmpty()) {
            userRole = User.UserRole.valueOf(role);
        }

        User.UserStatus userStatus = null;
        if (status != null && !status.isEmpty()) {
            userStatus = User.UserStatus.valueOf(status);
        }

        PageResponse<UserResponse> users = userService.getUsers(keyword, userRole, department, userStatus, pageable);

        return ApiResponse.success(users);
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @userService.isCurrentUser(#id, authentication)")
    public ApiResponse<UserResponse> getUser(
            @PathVariable @NotNull Long id,
            Authentication authentication) {

        log.debug("获取用户详情: id={}", id);

        UserResponse user = userService.getUserById(id);

        return ApiResponse.success(user);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        log.info("创建用户: username={}", request.getUsername());

        UserResponse user = userService.createUser(request);

        log.info("用户创建成功: id={}, username={}", user.getId(), user.getUsername());

        return ApiResponse.success(user, "用户创建成功");
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id, authentication)")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody UserRequest request,
            Authentication authentication) {

        log.info("更新用户: id={}, username={}", id, request.getUsername());

        UserResponse user = userService.updateUser(id, request);

        log.info("用户更新成功: id={}, username={}", user.getId(), user.getUsername());

        return ApiResponse.success(user, "用户更新成功");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable @NotNull Long id) {
        log.info("删除用户: id={}", id);

        userService.deleteUser(id);

        log.info("用户删除成功: id={}", id);

        return ApiResponse.success("用户删除成功");
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteUsers(@RequestBody List<Long> ids) {
        log.info("批量删除用户: ids={}", ids);

        ValidationUtils.notEmpty(ids, "ids");
        ValidationUtils.collectionSize(ids, 1, 50, "ids");

        int deletedCount = userService.batchDeleteUsers(ids);

        log.info("批量删除用户成功: 删除数量={}", deletedCount);

        return ApiResponse.success(String.format("成功删除%d个用户", deletedCount));
    }

    /**
     * 启用/禁用用户
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<Void> changeUserStatus(
            @PathVariable @NotNull Long id,
            @RequestParam boolean enabled) {

        log.info("修改用户状态: id={}, enabled={}", id, enabled);

        userService.changeUserStatus(id, enabled);

        log.info("用户状态修改成功: id={}, enabled={}", id, enabled);

        return ApiResponse.success(enabled ? "用户已启用" : "用户已禁用");
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> resetPassword(@PathVariable @NotNull Long id) {
        log.info("重置用户密码: id={}", id);

        userService.resetPassword(id, "123456");

        log.info("用户密码重置成功: id={}", id);

        return ApiResponse.success("密码重置成功，默认密码为123456");
    }

    /**
     * 导出用户列表
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {

        log.info("导出用户列表: keyword={}, role={}, department={}, status={}",
                keyword, role, department, status);

        List<UserResponse> users = userService.getUsersForExport(keyword, role, department, status);

        LinkedHashMap<String, String> headers = createExportHeaders();
        String fileName = String.format("用户列表_%s.xlsx",
                LocalDate.now().toString().replace("-", ""));

        ExcelUtils.exportToExcel(users, headers, fileName, response);

        log.info("用户列表导出成功: 用户数量={}", users.size());
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        log.debug("获取用户统计信息");

        Map<String, Object> statistics = userService.getUserStatistics();

        return ApiResponse.success(statistics, "获取用户统计成功");
    }

    /**
     * 批量导入用户
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> importUsers(@RequestParam("file") MultipartFile file) {
        log.info("批量导入用户: fileName={}", file.getOriginalFilename());

        ValidationUtils.fileName(file.getOriginalFilename());

        String[] allowedTypes = {".xlsx", ".xls"};
        ValidationUtils.fileExtension(file.getOriginalFilename(), allowedTypes);

        Map<String, Object> importResult = userService.importUsersFromExcel(file);

        log.info("用户批量导入完成");

        return ApiResponse.success(importResult, "用户导入成功");
    }

    /**
     * 下载用户导入模板
     */
    @GetMapping("/import-template")
    @PreAuthorize("hasRole('ADMIN')")
    public void downloadImportTemplate(HttpServletResponse response) {
        log.info("下载用户导入模板");

        LinkedHashMap<String, String> headers = createImportTemplateHeaders();
        String fileName = "用户导入模板.xlsx";

        ExcelUtils.createImportTemplate(headers, fileName, response);

        log.info("用户导入模板下载完成");
    }

    /**
     * 创建导出表头映射
     */
    private LinkedHashMap<String, String> createExportHeaders() {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "用户ID");
        headers.put("username", "用户名");
        headers.put("realName", "真实姓名");
        headers.put("email", "邮箱");
        headers.put("phone", "手机号");
        headers.put("roleDescription", "角色");
        headers.put("department", "部门");
        headers.put("statusDescription", "状态");
        headers.put("lastLoginAt", "最后登录时间");
        headers.put("createdAt", "创建时间");
        return headers;
    }

    /**
     * 创建导入模板表头映射
     */
    private LinkedHashMap<String, String> createImportTemplateHeaders() {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("username", "用户名*");
        headers.put("password", "密码*");
        headers.put("realName", "真实姓名*");
        headers.put("email", "邮箱");
        headers.put("phone", "手机号");
        headers.put("role", "角色*(ADMIN/MANAGER/SALES)");
        headers.put("department", "部门");
        return headers;
    }
}