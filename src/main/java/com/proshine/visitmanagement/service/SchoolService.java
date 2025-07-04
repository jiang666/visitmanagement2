package com.proshine.visitmanagement.service;

import com.proshine.visitmanagement.dto.request.SchoolRequest;
import com.proshine.visitmanagement.dto.response.DepartmentResponse;
import com.proshine.visitmanagement.dto.response.PageResponse;
import com.proshine.visitmanagement.dto.response.SchoolResponse;
import com.proshine.visitmanagement.entity.Customer;
import com.proshine.visitmanagement.entity.Department;
import com.proshine.visitmanagement.entity.School;
import com.proshine.visitmanagement.entity.User;
import com.proshine.visitmanagement.exception.BusinessException;
import com.proshine.visitmanagement.exception.ResourceNotFoundException;
import com.proshine.visitmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学校服务类
 *
 * @author System
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final DepartmentRepository departmentRepository;
    private final CustomerRepository customerRepository;
    private final VisitRecordRepository visitRecordRepository;
    private final UserRepository userRepository;

    // ==================== Controller调用的核心方法 ====================

    /**
     * 分页查询学校（修复版本 - 添加authentication参数并处理schoolType转换）
     */
    public PageResponse<SchoolResponse> getSchools(String keyword, String province, String city,
                                                   String schoolType, Pageable pageable, Authentication authentication) {
        log.debug("分页查询学校: keyword={}, province={}, city={}, schoolType={}",
                keyword, province, city, schoolType);

        // 转换schoolType字符串为枚举
        School.SchoolType schoolTypeEnum = parseSchoolType(schoolType);

        Page<School> schoolPage = schoolRepository.findSchoolsWithFilters(keyword, province, city, schoolTypeEnum, pageable);

        List<SchoolResponse> schoolResponses = schoolPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResponse.<SchoolResponse>builder()
                .content(schoolResponses)
                .page(schoolPage.getNumber())
                .size(schoolPage.getSize())
                .totalElements(schoolPage.getTotalElements())
                .totalPages(schoolPage.getTotalPages())
                .first(schoolPage.isFirst())
                .last(schoolPage.isLast())
                .empty(schoolPage.isEmpty())
                .build();
    }

    /**
     * 根据ID获取学校详情（Controller调用的方法名）
     */
    public SchoolResponse getSchool(Long id, Authentication authentication) {
        log.debug("获取学校详情: id={}", id);
        return getSchoolById(id);
    }

    /**
     * 根据ID获取学校详情（内部方法）
     */
    public SchoolResponse getSchoolById(Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("学校不存在"));
        return convertToResponse(school);
    }

    /**
     * 创建学校
     */
    @Transactional
    public SchoolResponse createSchool(SchoolRequest request, Authentication authentication) {
        // 检查权限
        checkAdminPermission(authentication);

        // 检查学校名称是否已存在
        if (schoolRepository.existsByName(request.getName())) {
            throw new BusinessException("学校名称已存在");
        }

        School school = new School();
        school.setName(request.getName());
        school.setAddress(request.getAddress());
        school.setProvince(request.getProvince());
        school.setCity(request.getCity());
        school.setSchoolType(parseSchoolType(request.getSchoolType()));
        school.setContactPhone(request.getContactPhone());
        school.setWebsite(request.getWebsite());

        School savedSchool = schoolRepository.save(school);
        log.info("创建学校成功: {}", savedSchool.getName());

        return convertToResponse(savedSchool);
    }

    /**
     * 更新学校
     */
    @Transactional
    public SchoolResponse updateSchool(Long id, SchoolRequest request, Authentication authentication) {
        // 检查权限
        checkAdminPermission(authentication);

        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("学校不存在"));

        // 检查学校名称是否已被其他学校使用
        if (!school.getName().equals(request.getName()) &&
                schoolRepository.existsByName(request.getName())) {
            throw new BusinessException("学校名称已存在");
        }

        school.setName(request.getName());
        school.setAddress(request.getAddress());
        school.setProvince(request.getProvince());
        school.setCity(request.getCity());
        school.setSchoolType(parseSchoolType(request.getSchoolType()));
        school.setContactPhone(request.getContactPhone());
        school.setWebsite(request.getWebsite());

        School savedSchool = schoolRepository.save(school);
        log.info("更新学校成功: {}", savedSchool.getName());

        return convertToResponse(savedSchool);
    }

    /**
     * 删除学校
     */
    @Transactional
    public void deleteSchool(Long id, Authentication authentication) {
        // 检查权限
        checkAdminPermission(authentication);

        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("学校不存在"));

        // 检查是否有关联的院系
        List<Department> departments = departmentRepository.findBySchoolId(id);
        if (!departments.isEmpty()) {
            throw new BusinessException("该学校存在关联的院系，无法删除");
        }

        schoolRepository.delete(school);
        log.info("删除学校成功: {}", school.getName());
    }

    /**
     * 批量删除学校
     */
    @Transactional
    public int batchDeleteSchools(List<Long> ids, Authentication authentication) {
        checkAdminPermission(authentication);

        List<School> schools = schoolRepository.findAllById(ids);
        if (schools.isEmpty()) {
            throw new BusinessException("未找到要删除的学校");
        }

        // 检查是否有关联的院系
        for (School school : schools) {
            List<Department> departments = departmentRepository.findBySchoolId(school.getId());
            if (!departments.isEmpty()) {
                throw new BusinessException("学校 " + school.getName() + " 存在关联的院系，无法删除");
            }
        }

        schoolRepository.deleteAll(schools);
        log.info("批量删除学校成功，数量: {}", schools.size());

        return schools.size();
    }

    /**
     * 获取学校列表用于导出（修复版本 - 添加authentication参数）
     */
    public List<SchoolResponse> getSchoolsForExport(String keyword, String province,
                                                    String city, String schoolType, Authentication authentication) {
        log.info("导出学校列表: keyword={}, province={}, city={}, schoolType={}",
                keyword, province, city, schoolType);

        // 检查权限（仅管理员可以导出）
        checkAdminPermission(authentication);

        // 转换schoolType字符串为枚举
        School.SchoolType schoolTypeEnum = parseSchoolType(schoolType);

        List<School> schools = schoolRepository.findSchoolsForExport(keyword, province, city, schoolTypeEnum);

        return schools.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取学校统计信息（修复版本 - 添加authentication参数）
     */
    public Map<String, Object> getSchoolStatistics(Authentication authentication) {
        log.debug("获取学校统计信息");

        // 检查权限（仅管理员可以查看统计）
        checkAdminPermission(authentication);

        Map<String, Object> statistics = new HashMap<>();

        // 总学校数
        long totalCount = schoolRepository.count();
        statistics.put("totalCount", totalCount);

        // 按类型统计（修复：使用正确的枚举值）
        Map<String, Long> typeStats = new HashMap<>();
        typeStats.put("985工程", schoolRepository.countBySchoolType(School.SchoolType.PROJECT_985));
        typeStats.put("211工程", schoolRepository.countBySchoolType(School.SchoolType.PROJECT_211));
        typeStats.put("双一流", schoolRepository.countBySchoolType(School.SchoolType.DOUBLE_FIRST_CLASS));
        typeStats.put("普通高校", schoolRepository.countBySchoolType(School.SchoolType.REGULAR));
        statistics.put("typeStatistics", typeStats);

        // 按省份统计前10
        Map<String, Long> provinceCountMap = schoolRepository.findAll().stream()
                .filter(school -> StringUtils.hasText(school.getProvince()))
                .collect(Collectors.groupingBy(School::getProvince, Collectors.counting()));

        List<Map<String, Object>> provinceStats = provinceCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("province", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        statistics.put("provinceStatistics", provinceStats);

        // 按城市统计前10
        Map<String, Long> cityCountMap = schoolRepository.findAll().stream()
                .filter(school -> StringUtils.hasText(school.getCity()))
                .collect(Collectors.groupingBy(School::getCity, Collectors.counting()));

        List<Map<String, Object>> cityStats = cityCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("city", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        statistics.put("cityStatistics", cityStats);

        return statistics;
    }

    /**
     * 从Excel文件批量导入学校
     */
    @Transactional
    public Map<String, Object> importSchoolsFromExcel(MultipartFile file, Authentication authentication) {
        // 检查权限
        checkAdminPermission(authentication);

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", 0);
        result.put("failCount", 0);
        result.put("errors", Arrays.asList("Excel导入功能需要完整实现"));
        result.put("totalCount", 0);
        result.put("message", "Excel导入功能需要使用POI库具体实现");

        return result;
    }

    /**
     * 获取省份城市列表（新增方法）
     */
    public Map<String, List<String>> getProvincesCities() {
        log.debug("获取省份城市列表");

        Map<String, List<String>> provincesCities = new HashMap<>();

        // 获取所有省份
        List<String> provinces = getAllProvinces();

        // 为每个省份获取城市列表
        for (String province : provinces) {
            List<String> cities = getCitiesByProvince(province);
            provincesCities.put(province, cities);
        }

        return provincesCities;
    }

    // ==================== 业务辅助方法 ====================

    /**
     * 根据省份获取学校列表
     */
    public List<SchoolResponse> getSchoolsByProvince(String province) {
        List<School> schools = schoolRepository.findByProvince(province);
        return schools.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据城市获取学校列表
     */
    public List<SchoolResponse> getSchoolsByCity(String city) {
        List<School> schools = schoolRepository.findByCity(city);
        return schools.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据学校类型获取学校列表
     */
    public List<SchoolResponse> getSchoolsByType(School.SchoolType schoolType) {
        List<School> schools = schoolRepository.findBySchoolType(schoolType);
        return schools.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 搜索学校
     */
    public List<SchoolResponse> searchSchools(String keyword, Integer limit) {
        List<School> schools = schoolRepository.findByNameContainingIgnoreCase(keyword);
        return schools.stream()
                .limit(limit != null ? limit : Integer.MAX_VALUE)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有省份
     */
    public List<String> getAllProvinces() {
        return schoolRepository.findAll().stream()
                .map(School::getProvince)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 根据省份获取城市列表
     */
    public List<String> getCitiesByProvince(String province) {
        return schoolRepository.findByProvince(province).stream()
                .map(School::getCity)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 检查管理员权限
     */
    private void checkAdminPermission(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() != User.UserRole.ADMIN) {
            throw new BusinessException("只有管理员才能执行此操作");
        }
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
     * 解析学校类型字符串为枚举
     */
    private School.SchoolType parseSchoolType(String schoolType) {
        if (!StringUtils.hasText(schoolType)) {
            return School.SchoolType.REGULAR;
        }

        try {
            return School.SchoolType.valueOf(schoolType.toUpperCase());
        } catch (Exception e) {
            log.warn("无效的学校类型: {}, 使用默认值REGULAR", schoolType);
            return School.SchoolType.REGULAR;
        }
    }

    /**
     * 转换为响应对象
     */
    private SchoolResponse convertToResponse(School school) {
        // 统计相关数据
        List<Department> departments = departmentRepository.findBySchoolId(school.getId());
        int departmentCount = departments.size();

        // 客户数量
        long customerCountLong = departments.stream()
                .mapToLong(dept -> customerRepository.countByDepartmentId(dept.getId()))
                .sum();
        int customerCount = (int) customerCountLong;

        // 拜访记录数量
        long visitCountLong = departments.stream()
                .flatMap(dept -> customerRepository.findByDepartmentId(dept.getId()).stream())
                .mapToLong(customer -> visitRecordRepository.countByCustomerId(customer.getId()))
                .sum();
        int visitCount = (int) visitCountLong;

        // 最后拜访时间
        LocalDateTime lastVisitDate = departments.stream()
                .flatMap(dept -> customerRepository.findByDepartmentId(dept.getId()).stream())
                .flatMap(customer -> visitRecordRepository.findByCustomerIdOrderByVisitDateDesc(customer.getId()).stream())
                .filter(visit -> visit.getVisitDate() != null)
                .map(visit -> visit.getVisitDate().atStartOfDay())
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return SchoolResponse.builder()
                .id(school.getId())
                .name(school.getName())
                .address(school.getAddress())
                .province(school.getProvince())
                .city(school.getCity())
                .schoolType(school.getSchoolType().name())
                .schoolTypeDescription(school.getSchoolType().getDescription())
                .contactPhone(school.getContactPhone())
                .website(school.getWebsite())
                .departmentCount(departmentCount)
                .customerCount(customerCount)
                .visitCount(visitCount)
                .lastVisitDate(lastVisitDate)
                .createdAt(school.getCreatedAt())
                .updatedAt(school.getUpdatedAt())
                .build();
    }
}