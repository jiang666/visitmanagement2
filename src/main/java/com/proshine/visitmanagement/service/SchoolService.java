package com.proshine.visitmanagement.service;

import com.proshine.visitmanagement.dto.request.SchoolRequest;
import com.proshine.visitmanagement.dto.response.DepartmentResponse;
import com.proshine.visitmanagement.dto.response.PageResponse;
import com.proshine.visitmanagement.dto.response.SchoolResponse;
import com.proshine.visitmanagement.dto.response.SchoolDepartmentTreeResponse;
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
import org.springframework.data.domain.Sort;
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

        // 转换schoolType字符串为正则表达式
        String schoolTypesRegex = buildSchoolTypesRegex(schoolType);

        Page<School> schoolPage = schoolRepository.findSchoolsWithFilters(keyword, province, city, schoolTypesRegex, pageable);

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
        school.setSchoolTypes(parseSchoolTypesFromRequest(request.getSchoolTypes()));
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
        school.setSchoolTypes(parseSchoolTypesFromRequest(request.getSchoolTypes()));
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

        // 转换schoolType字符串为正则表达式
        String schoolTypesRegex = buildSchoolTypesRegex(schoolType);

        List<School> schools = schoolRepository.findSchoolsForExport(keyword, province, city, schoolTypesRegex);

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

    /**
     * 获取学校-院系树结构
     */
    public List<SchoolDepartmentTreeResponse> getSchoolDepartmentTree(Authentication authentication) {
        log.debug("获取学校-院系树结构");

        // 所有用户均可查询，无需额外权限校验
        List<School> schools = schoolRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        List<Department> departments = departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

        Map<Long, List<Department>> deptMap = departments.stream()
                .collect(Collectors.groupingBy(dept -> dept.getSchool().getId()));

        return schools.stream()
                .map(school -> {
                    List<SchoolDepartmentTreeResponse.DepartmentNode> deptNodes =
                            deptMap.getOrDefault(school.getId(), Collections.emptyList()).stream()
                                    .map(dept -> SchoolDepartmentTreeResponse.DepartmentNode.builder()
                                            .id(dept.getId())
                                            .name(dept.getName())
                                            .build())
                                    .collect(Collectors.toList());

                    return SchoolDepartmentTreeResponse.builder()
                            .id(school.getId())
                            .name(school.getName())
                            .departments(deptNodes)
                            .build();
                })
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
     * 从请求中解析学校类型列表为Set
     */
    private Set<School.SchoolType> parseSchoolTypesFromRequest(List<String> schoolTypeStrings) {
        if (schoolTypeStrings == null || schoolTypeStrings.isEmpty()) {
            return new HashSet<>();
        }

        Set<School.SchoolType> types = new HashSet<>();
        for (String typeString : schoolTypeStrings) {
            if (StringUtils.hasText(typeString)) {
                try {
                    School.SchoolType schoolType = School.SchoolType.valueOf(typeString.toUpperCase());
                    types.add(schoolType);
                } catch (Exception e) {
                    log.warn("无效的学校类型: {}, 忽略", typeString);
                }
            }
        }
        return types;
    }

    /**
     * 构建学校类型正则表达式用于查询
     */
    private String buildSchoolTypesRegex(String schoolTypes) {
        if (!StringUtils.hasText(schoolTypes)) {
            return null; // 返回null表示不过滤学校类型
        }

        String[] types = schoolTypes.split(",");
        List<String> validTypes = new ArrayList<>();
        
        for (String type : types) {
            String trimmedType = type.trim();
            if (StringUtils.hasText(trimmedType)) {
                try {
                    School.SchoolType.valueOf(trimmedType.toUpperCase());
                    validTypes.add(trimmedType.toUpperCase());
                } catch (Exception e) {
                    log.warn("无效的学校类型: {}, 忽略", trimmedType);
                }
            }
        }

        if (validTypes.isEmpty()) {
            return null;
        }

        // 构建正则表达式：(TYPE1|TYPE2|TYPE3)
        return "(" + String.join("|", validTypes) + ")";
    }

    /**
     * 解析学校类型字符串为枚举列表（支持多个类型，用逗号分隔）
     */
    private List<School.SchoolType> parseSchoolTypes(String schoolTypes) {
        if (!StringUtils.hasText(schoolTypes)) {
            return null; // 返回null表示不过滤学校类型
        }

        List<School.SchoolType> typeList = new ArrayList<>();
        String[] types = schoolTypes.split(",");
        
        for (String type : types) {
            String trimmedType = type.trim();
            if (StringUtils.hasText(trimmedType)) {
                try {
                    School.SchoolType schoolType = School.SchoolType.valueOf(trimmedType.toUpperCase());
                    typeList.add(schoolType);
                } catch (Exception e) {
                    log.warn("无效的学校类型: {}, 忽略", trimmedType);
                }
            }
        }

        // 如果解析后的列表为空，返回null表示不过滤
        return typeList.isEmpty() ? null : typeList;
    }

    /**
     * 解析学校类型字符串为枚举（保持向后兼容）
     */
    private School.SchoolType parseSchoolType(String schoolType) {
        if (!StringUtils.hasText(schoolType)) {
            return null; // 返回null表示不过滤学校类型
        }

        try {
            return School.SchoolType.valueOf(schoolType.toUpperCase());
        } catch (Exception e) {
            log.warn("无效的学校类型: {}, 返回null不过滤类型", schoolType);
            return null; // 无效类型时也返回null，不过滤
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

        // 获取所有学校类型
        Set<School.SchoolType> schoolTypes = school.getSchoolTypes();
        List<String> schoolTypeNames = schoolTypes.stream()
                .map(School.SchoolType::name)
                .collect(Collectors.toList());
        List<String> schoolTypeDescriptions = schoolTypes.stream()
                .map(School.SchoolType::getDescription)
                .collect(Collectors.toList());

        // 向后兼容：取第一个类型
        School.SchoolType firstType = school.getSchoolType(); // 这个方法返回第一个类型或REGULAR

        return SchoolResponse.builder()
                .id(school.getId())
                .name(school.getName())
                .address(school.getAddress())
                .province(school.getProvince())
                .city(school.getCity())
                .schoolType(firstType.name()) // 向后兼容
                .schoolTypeDescription(firstType.getDescription()) // 向后兼容
                .schoolTypes(schoolTypeNames) // 新字段：所有类型
                .schoolTypeDescriptions(schoolTypeDescriptions) // 新字段：所有类型描述
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