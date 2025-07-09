package com.proshine.visitmanagement.controller;

import com.proshine.visitmanagement.dto.request.SchoolRequest;
import com.proshine.visitmanagement.dto.response.ApiResponse;
import com.proshine.visitmanagement.dto.response.PageResponse;
import com.proshine.visitmanagement.dto.response.SchoolResponse;
import com.proshine.visitmanagement.dto.response.SchoolDepartmentTreeResponse;
import com.proshine.visitmanagement.service.SchoolService;
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
 * 学校控制器
 * 处理学校相关的HTTP请求
 *
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
@Validated
public class SchoolController {

    private final SchoolService schoolService;

    /**
     * 分页查询学校
     */
    @GetMapping
    public ApiResponse<PageResponse<SchoolResponse>> getSchools(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String schoolType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        log.debug("分页查询学校: keyword={}, province={}, city={}, schoolType={}, page={}, size={}",
                keyword, province, city, schoolType, pageable.getPageNumber(), pageable.getPageSize());

        PageResponse<SchoolResponse> schools = schoolService.getSchools(
                keyword, province, city, schoolType, pageable, authentication);

        return ApiResponse.success(schools);
    }

    /**
     * 根据ID获取学校详情
     */
    @GetMapping("/{id}")
    public ApiResponse<SchoolResponse> getSchool(
            @PathVariable @NotNull Long id,
            Authentication authentication) {

        log.debug("获取学校详情: id={}", id);

        SchoolResponse school = schoolService.getSchool(id, authentication);

        return ApiResponse.success(school);
    }

    /**
     * 创建学校
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SchoolResponse> createSchool(
            @Valid @RequestBody SchoolRequest request,
            Authentication authentication) {

        log.info("创建学校: name={}", request.getName());

        SchoolResponse school = schoolService.createSchool(request, authentication);

        log.info("学校创建成功: id={}, name={}", school.getId(), school.getName());

        return ApiResponse.success(school, "学校创建成功");
    }

    /**
     * 更新学校
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SchoolResponse> updateSchool(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody SchoolRequest request,
            Authentication authentication) {

        log.info("更新学校: id={}, name={}", id, request.getName());

        SchoolResponse school = schoolService.updateSchool(id, request, authentication);

        log.info("学校更新成功: id={}, name={}", school.getId(), school.getName());

        return ApiResponse.success(school, "学校更新成功");
    }

    /**
     * 删除学校
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteSchool(
            @PathVariable @NotNull Long id,
            Authentication authentication) {

        log.info("删除学校: id={}", id);

        schoolService.deleteSchool(id, authentication);

        log.info("学校删除成功: id={}", id);

        return ApiResponse.success("学校删除成功");
    }

    /**
     * 批量删除学校
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteSchools(
            @RequestBody List<Long> ids,
            Authentication authentication) {

        log.info("批量删除学校: ids={}", ids);

        ValidationUtils.notEmpty(ids, "ids");
        ValidationUtils.collectionSize(ids, 1, 50, "ids");

        int deletedCount = schoolService.batchDeleteSchools(ids, authentication);

        log.info("批量删除学校成功: 删除数量={}", deletedCount);

        return ApiResponse.success(String.format("成功删除%d个学校", deletedCount));
    }

    /**
     * 导出学校列表
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportSchools(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String schoolType,
            HttpServletResponse response,
            Authentication authentication) {

        log.info("导出学校列表: keyword={}, province={}, city={}, schoolType={}",
                keyword, province, city, schoolType);

        List<SchoolResponse> schools = schoolService.getSchoolsForExport(
                keyword, province, city, schoolType, authentication);

        LinkedHashMap<String, String> headers = createExportHeaders();
        String fileName = String.format("学校列表_%s.xlsx",
                LocalDate.now().toString().replace("-", ""));

        ExcelUtils.exportToExcel(schools, headers, fileName, response);

        log.info("学校列表导出成功: 学校数量={}", schools.size());
    }

    /**
     * 获取学校统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getSchoolStatistics(Authentication authentication) {
        log.debug("获取学校统计信息");

        Map<String, Object> statistics = schoolService.getSchoolStatistics(authentication);

        return ApiResponse.success(statistics, "获取学校统计成功");
    }

    /**
     * 批量导入学校
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> importSchools(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        log.info("批量导入学校: fileName={}", file.getOriginalFilename());

        ValidationUtils.fileName(file.getOriginalFilename());

        String[] allowedTypes = {".xlsx", ".xls"};
        ValidationUtils.fileExtension(file.getOriginalFilename(), allowedTypes);

        Map<String, Object> importResult = schoolService.importSchoolsFromExcel(file, authentication);

        log.info("学校批量导入完成");

        return ApiResponse.success(importResult, "学校导入成功");
    }

    /**
     * 下载学校导入模板
     */
    @GetMapping("/import-template")
    @PreAuthorize("hasRole('ADMIN')")
    public void downloadImportTemplate(HttpServletResponse response) {
        log.info("下载学校导入模板");

        LinkedHashMap<String, String> headers = createImportTemplateHeaders();
        String fileName = "学校导入模板.xlsx";

        ExcelUtils.createImportTemplate(headers, fileName, response);

        log.info("学校导入模板下载完成");
    }

    /**
     * 获取省份城市列表
     */
    @GetMapping("/provinces-cities")
    public ApiResponse<Map<String, List<String>>> getProvincesCities() {
        log.debug("获取省份城市列表");

        Map<String, List<String>> provincesCities = schoolService.getProvincesCities();

        return ApiResponse.success(provincesCities, "获取省份城市列表成功");
    }

    /**
     * 获取学校-院系树结构
     */
    @GetMapping("/tree")
    public ApiResponse<List<SchoolDepartmentTreeResponse>> getSchoolDepartmentTree(Authentication authentication) {
        log.debug("获取学校-院系树结构");

        List<SchoolDepartmentTreeResponse> tree = schoolService.getSchoolDepartmentTree(authentication);

        return ApiResponse.success(tree, "获取学校院系树成功");
    }

    /**
     * 创建导出表头映射
     */
    private LinkedHashMap<String, String> createExportHeaders() {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("id", "学校ID");
        headers.put("name", "学校名称");
        headers.put("address", "学校地址");
        headers.put("province", "省份");
        headers.put("city", "城市");
        headers.put("schoolTypeDescription", "学校类型");
        headers.put("contactPhone", "联系电话");
        headers.put("website", "学校网站");
        headers.put("departmentCount", "院系数量");
        headers.put("customerCount", "客户数量");
        headers.put("createdAt", "创建时间");
        headers.put("updatedAt", "更新时间");
        return headers;
    }

    /**
     * 创建导入模板表头映射
     */
    private LinkedHashMap<String, String> createImportTemplateHeaders() {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("name", "学校名称*");
        headers.put("address", "学校地址");
        headers.put("province", "省份*");
        headers.put("city", "城市*");
        headers.put("schoolType", "学校类型(PROJECT_985/PROJECT_211/DOUBLE_FIRST_CLASS/REGULAR)*");
        headers.put("contactPhone", "联系电话");
        headers.put("website", "学校网站");
        return headers;
    }
}