package com.proshine.visitmanagement.service;

import com.proshine.visitmanagement.dto.request.CustomerRequest;
import com.proshine.visitmanagement.dto.response.CustomerResponse;
import com.proshine.visitmanagement.dto.response.PageResponse;
import com.proshine.visitmanagement.entity.Customer;
import com.proshine.visitmanagement.entity.Department;
import com.proshine.visitmanagement.entity.School;
import com.proshine.visitmanagement.entity.User;
import com.proshine.visitmanagement.entity.VisitRecord;
import com.proshine.visitmanagement.exception.BusinessException;
import com.proshine.visitmanagement.exception.ResourceNotFoundException;
import com.proshine.visitmanagement.repository.CustomerRepository;
import com.proshine.visitmanagement.repository.DepartmentRepository;
import com.proshine.visitmanagement.repository.SchoolRepository;
import com.proshine.visitmanagement.repository.UserRepository;
import com.proshine.visitmanagement.repository.VisitRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户服务类
 *
 * @author System
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final DepartmentRepository departmentRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final VisitRecordRepository visitRecordRepository;

    // ==================== Controller调用的核心方法 ====================

    /**
     * 分页查询客户（Controller使用）
     */
    public PageResponse<CustomerResponse> getCustomers(String keyword, Long departmentId, Long schoolId,
                                                       String schoolCity, String influenceLevel, String decisionPower,
                                                       Boolean hasWechat, Pageable pageable, Authentication authentication) {
        log.debug("分页查询客户: keyword={}, departmentId={}, schoolId={}, schoolCity={}",
                keyword, departmentId, schoolId, schoolCity);

        // 转换枚举参数
        Customer.InfluenceLevel influenceLevelEnum = parseInfluenceLevel(influenceLevel);
        Customer.DecisionPower decisionPowerEnum = parseDecisionPower(decisionPower);

        // 根据用户权限查询
        User currentUser = getCurrentUser(authentication);
        Page<Customer> customerPage;

        if (isAdminOrManager(currentUser)) {
            // 管理员和经理可以查看所有客户
            customerPage = customerRepository.findCustomersWithAllFilters(
                    keyword, departmentId, schoolId, schoolCity,
                    influenceLevelEnum, decisionPowerEnum, hasWechat, pageable);
        } else {
            // 销售人员只能查看自己创建的客户
            customerPage = customerRepository.findCustomersWithAllFiltersByCreatedBy(
                    keyword, departmentId, schoolId, schoolCity,
                    influenceLevelEnum, decisionPowerEnum, hasWechat, currentUser.getId(), pageable);
        }

        List<CustomerResponse> customerResponses = customerPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResponse.<CustomerResponse>builder()
                .content(customerResponses)
                .page(customerPage.getNumber())
                .size(customerPage.getSize())
                .totalElements(customerPage.getTotalElements())
                .totalPages(customerPage.getTotalPages())
                .first(customerPage.isFirst())
                .last(customerPage.isLast())
                .empty(customerPage.isEmpty())
                .build();
    }

    /**
     * 根据ID获取客户详情（Controller使用）
     */
    public CustomerResponse getCustomer(Long id, Authentication authentication) {
        log.debug("获取客户详情: id={}", id);
        return getCustomerById(id, authentication);
    }

    /**
     * 根据ID获取客户详情（内部方法）
     */
    public CustomerResponse getCustomerById(Long id, Authentication authentication) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("客户不存在"));

        // 权限检查
        checkCustomerPermission(customer, authentication);

        return convertToResponse(customer);
    }

    /**
     * 创建客户
     */
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request, Authentication authentication) {
        log.info("创建客户: name={}", request.getName());

        // 获取当前用户
        User currentUser = getCurrentUser(authentication);

        // 验证学校是否存在
        School school = null;
        if (request.getSchoolId() != null) {
            school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("学校不存在"));
        }
        
        // 验证院系是否存在
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("院系不存在"));
            
            // 如果选择了院系，验证院系是否属于选择的学校
            if (school != null && !department.getSchool().getId().equals(school.getId())) {
                throw new BusinessException("所选院系不属于所选学校");
            }
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(request.getEmail()) &&
                customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("邮箱已存在");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setPosition(request.getPosition());
        customer.setTitle(request.getTitle());
        customer.setSchool(school);
        customer.setDepartment(department);
        customer.setPhone(request.getPhone());
        customer.setWechat(request.getWechat());
        customer.setEmail(request.getEmail());
        customer.setOfficeLocation(request.getOfficeLocation());
        customer.setFloorRoom(request.getFloorRoom());
        customer.setResearchDirection(request.getResearchDirection());
        customer.setInfluenceLevel(StringUtils.hasText(request.getInfluenceLevel()) ?
                Customer.InfluenceLevel.valueOf(request.getInfluenceLevel()) : Customer.InfluenceLevel.MEDIUM);
        customer.setDecisionPower(StringUtils.hasText(request.getDecisionPower()) ?
                Customer.DecisionPower.valueOf(request.getDecisionPower()) : Customer.DecisionPower.OTHER);
        customer.setBirthday(request.getBirthday());
        customer.setNotes(request.getNotes());

        // 设置创建人和修改人
        customer.setCreatedBy(currentUser);
        customer.setUpdatedBy(currentUser);

        Customer savedCustomer = customerRepository.save(customer);
        log.info("创建客户成功: {}", savedCustomer.getName());

        return convertToResponse(savedCustomer);
    }

    /**
     * 更新客户
     */
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request, Authentication authentication) {
        log.info("更新客户: id={}, name={}", id, request.getName());

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("客户不存在"));

        // 权限检查
        checkCustomerPermission(customer, authentication);

        // 获取当前用户
        User currentUser = getCurrentUser(authentication);

        // 验证学校是否存在
        School school = null;
        if (request.getSchoolId() != null) {
            school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("学校不存在"));
        }
        
        // 验证院系是否存在
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("院系不存在"));
            
            // 如果选择了院系，验证院系是否属于选择的学校
            if (school != null && !department.getSchool().getId().equals(school.getId())) {
                throw new BusinessException("所选院系不属于所选学校");
            }
        }

        // 检查邮箱是否已被其他客户使用
        if (StringUtils.hasText(request.getEmail()) &&
                !request.getEmail().equals(customer.getEmail())) {
            customerRepository.findByEmail(request.getEmail()).ifPresent(existingCustomer -> {
                if (!existingCustomer.getId().equals(id)) {
                    throw new BusinessException("邮箱已存在");
                }
            });
        }

        customer.setName(request.getName());
        customer.setPosition(request.getPosition());
        customer.setTitle(request.getTitle());
        customer.setSchool(school);
        customer.setDepartment(department);
        customer.setPhone(request.getPhone());
        customer.setWechat(request.getWechat());
        customer.setEmail(request.getEmail());
        customer.setOfficeLocation(request.getOfficeLocation());
        customer.setFloorRoom(request.getFloorRoom());
        customer.setResearchDirection(request.getResearchDirection());
        customer.setInfluenceLevel(StringUtils.hasText(request.getInfluenceLevel()) ?
                Customer.InfluenceLevel.valueOf(request.getInfluenceLevel()) : customer.getInfluenceLevel());
        customer.setDecisionPower(StringUtils.hasText(request.getDecisionPower()) ?
                Customer.DecisionPower.valueOf(request.getDecisionPower()) : customer.getDecisionPower());
        customer.setBirthday(request.getBirthday());
        customer.setNotes(request.getNotes());

        // 设置修改人
        customer.setUpdatedBy(currentUser);

        Customer savedCustomer = customerRepository.save(customer);
        log.info("更新客户成功: {}", savedCustomer.getName());

        return convertToResponse(savedCustomer);
    }

    /**
     * 删除客户
     */
    @Transactional
    public void deleteCustomer(Long id, Authentication authentication) {
        log.info("删除客户: id={}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("客户不存在"));

        // 权限检查
        checkCustomerPermission(customer, authentication);

        // 检查是否有关联的拜访记录
        long visitCount = visitRecordRepository.countByCustomerId(id);
        if (visitCount > 0) {
            throw new BusinessException("该客户存在 " + visitCount + " 条拜访记录，无法删除");
        }

        customerRepository.delete(customer);
        log.info("删除客户成功: {}", customer.getName());
    }

    /**
     * 批量删除客户
     */
    @Transactional
    public int batchDeleteCustomers(List<Long> ids, Authentication authentication) {
        log.info("批量删除客户: ids={}", ids);

        User currentUser = getCurrentUser(authentication);
        if (!isAdminOrManager(currentUser)) {
            throw new BusinessException("只有管理员和经理才能批量删除客户");
        }

        List<Customer> customers = customerRepository.findAllById(ids);
        if (customers.isEmpty()) {
            throw new BusinessException("未找到要删除的客户");
        }

        // 检查是否有关联的拜访记录
        for (Customer customer : customers) {
            long visitCount = visitRecordRepository.countByCustomerId(customer.getId());
            if (visitCount > 0) {
                throw new BusinessException("客户 " + customer.getName() + " 存在 " + visitCount + " 条拜访记录，无法删除");
            }
        }

        customerRepository.deleteAll(customers);
        log.info("批量删除客户成功，数量: {}", customers.size());

        return customers.size();
    }

    /**
     * 获取客户列表用于导出
     */
    public List<CustomerResponse> getCustomersForExport(String keyword, Long departmentId, Long schoolId,
                                                        String schoolCity, String influenceLevel, String decisionPower,
                                                        Boolean hasWechat, Authentication authentication) {
        log.info("导出客户列表: keyword={}, departmentId={}, schoolId={}", keyword, departmentId, schoolId);

        // 转换枚举参数
        Customer.InfluenceLevel influenceLevelEnum = parseInfluenceLevel(influenceLevel);
        Customer.DecisionPower decisionPowerEnum = parseDecisionPower(decisionPower);

        // 根据用户权限获取客户列表
        User currentUser = getCurrentUser(authentication);
        List<Customer> customers;

        if (isAdminOrManager(currentUser)) {
            customers = customerRepository.findCustomersForExport(
                    keyword, departmentId, schoolId, schoolCity,
                    influenceLevelEnum, decisionPowerEnum, hasWechat);
        } else {
            customers = customerRepository.findCustomersForExportByCreatedBy(
                    keyword, departmentId, schoolId, schoolCity,
                    influenceLevelEnum, decisionPowerEnum, hasWechat, currentUser.getId());
        }

        return customers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取客户统计信息
     */
    public Object getCustomerStatistics(Authentication authentication) {
        log.debug("获取客户统计信息");

        User currentUser = getCurrentUser(authentication);
        Map<String, Object> statistics = new HashMap<>();

        if (isAdminOrManager(currentUser)) {
            // 管理员和经理可以查看全部统计
            statistics.put("totalCount", customerRepository.count());
            statistics.put("todayCount", getTodayCustomerCount());
            statistics.put("monthCount", getMonthCustomerCount());
            statistics.put("yearCount", getYearCustomerCount());
            statistics.put("influenceLevelStats", getInfluenceLevelStatistics());
            statistics.put("decisionPowerStats", getDecisionPowerStatistics());
            statistics.put("schoolDistribution", getSchoolDistribution());
            statistics.put("departmentDistribution", getDepartmentDistribution());
            statistics.put("birthdayThisMonth", getCustomersWithBirthdayThisMonth().size());
            statistics.put("recentCustomers", getRecentCustomers(5));
        } else {
            // 销售人员只能查看自己的统计
            Long salesId = currentUser.getId();
            statistics.put("myTotalCount", customerRepository.countByCreatedById(salesId));
            statistics.put("myTodayCount", getTodayCustomerCountBySales(salesId));
            statistics.put("myMonthCount", getMonthCustomerCountBySales(salesId));
            statistics.put("myHighInfluence", customerRepository.countByCreatedByIdAndInfluenceLevel(salesId, Customer.InfluenceLevel.HIGH));
            statistics.put("myRecentVisits", getMyRecentVisitsCount(salesId));
            statistics.put("myCustomerSummary", getMyCustomerSummary(salesId));
        }

        return statistics;
    }

    /**
     * 从Excel导入客户
     */
    @Transactional
    public Object importCustomersFromExcel(MultipartFile file, Authentication authentication) {
        log.info("开始导入客户: fileName={}", file.getOriginalFilename());

        User currentUser = getCurrentUser(authentication);
        if (!isAdminOrManager(currentUser)) {
            throw new BusinessException("只有管理员和经理才能批量导入客户");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", 0);
        result.put("failCount", 0);
        result.put("errors", Arrays.asList("Excel导入功能需要完整实现"));
        result.put("totalCount", 0);
        result.put("message", "Excel导入功能需要使用POI库具体实现");

        return result;
    }

    /**
     * 客户合并
     */
    @Transactional
    public void mergeCustomers(Long sourceId, Long targetId, Authentication authentication) {
        log.info("合并客户: sourceId={}, targetId={}", sourceId, targetId);

        User currentUser = getCurrentUser(authentication);
        if (!isAdminOrManager(currentUser)) {
            throw new BusinessException("只有管理员和经理才能合并客户");
        }

        Customer sourceCustomer = customerRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("源客户不存在"));
        Customer targetCustomer = customerRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("目标客户不存在"));

        // 将源客户的拜访记录转移到目标客户
        List<VisitRecord> sourceVisitRecords = visitRecordRepository.findByCustomerIdOrderByVisitDateDesc(sourceId);
        for (VisitRecord record : sourceVisitRecords) {
            record.setCustomer(targetCustomer);
            visitRecordRepository.save(record);
        }

        // 删除源客户
        customerRepository.delete(sourceCustomer);
        log.info("客户合并成功: {} -> {}", sourceCustomer.getName(), targetCustomer.getName());
    }

    // ==================== 业务辅助方法 ====================

    /**
     * 根据院系ID获取客户列表
     */
    public List<CustomerResponse> getCustomersByDepartment(Long departmentId) {
        List<Customer> customers = customerRepository.findByDepartmentId(departmentId);
        return customers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据影响力等级获取客户列表
     */
    public List<CustomerResponse> getCustomersByInfluenceLevel(Customer.InfluenceLevel influenceLevel) {
        List<Customer> customers = customerRepository.findByInfluenceLevel(influenceLevel);
        return customers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取本月生日的客户
     */
    public List<CustomerResponse> getCustomersWithBirthdayThisMonth() {
        int currentMonth = LocalDate.now().getMonthValue();
        List<Customer> customers = customerRepository.findCustomersByBirthdayMonth(currentMonth);
        return customers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 搜索客户
     */
    public List<CustomerResponse> searchCustomers(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }

        List<Customer> customers = customerRepository.findByNameContaining(keyword);
        return customers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取客户的拜访记录摘要
     */
    public List<VisitRecord> getCustomerVisitSummary(Long customerId, int limit) {
        List<VisitRecord> visitRecords = visitRecordRepository.findByCustomerIdOrderByVisitDateDesc(customerId);
        return visitRecords.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 检查客户是否存在
     */
    public boolean existsById(Long id) {
        return customerRepository.existsById(id);
    }

    /**
     * 获取客户总数
     */
    public long getTotalCustomerCount() {
        return customerRepository.count();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取当前用户
     */
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("当前用户不存在"));
    }

    /**
     * 判断是否是管理员或经理
     */
    private boolean isAdminOrManager(User user) {
        return user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.MANAGER;
    }

    /**
     * 解析影响力等级
     */
    private Customer.InfluenceLevel parseInfluenceLevel(String influenceLevel) {
        if (StringUtils.hasText(influenceLevel)) {
            try {
                return Customer.InfluenceLevel.valueOf(influenceLevel);
            } catch (Exception e) {
                log.warn("无效的影响力等级: {}", influenceLevel);
            }
        }
        return null;
    }

    /**
     * 解析决策权力
     */
    private Customer.DecisionPower parseDecisionPower(String decisionPower) {
        if (StringUtils.hasText(decisionPower)) {
            try {
                return Customer.DecisionPower.valueOf(decisionPower);
            } catch (Exception e) {
                log.warn("无效的决策权力: {}", decisionPower);
            }
        }
        return null;
    }

    /**
     * 获取今日客户数量
     */
    private long getTodayCustomerCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        return customerRepository.countByCreatedAtBetween(startOfDay, endOfDay);
    }

    /**
     * 获取本月客户数量
     */
    private long getMonthCustomerCount() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);
        return customerRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);
    }

    /**
     * 获取本年客户数量
     */
    private long getYearCustomerCount() {
        LocalDateTime startOfYear = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime endOfYear = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear()).atTime(23, 59, 59);
        return customerRepository.countByCreatedAtBetween(startOfYear, endOfYear);
    }

    /**
     * 获取销售人员今日客户数量
     */
    private long getTodayCustomerCountBySales(Long salesId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        return customerRepository.countByCreatedByIdAndCreatedAtBetween(salesId, startOfDay, endOfDay);
    }

    /**
     * 获取销售人员本月客户数量
     */
    private long getMonthCustomerCountBySales(Long salesId) {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);
        return customerRepository.countByCreatedByIdAndCreatedAtBetween(salesId, startOfMonth, endOfMonth);
    }

    /**
     * 获取影响力等级统计
     */
    private Map<String, Long> getInfluenceLevelStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("HIGH", customerRepository.countByInfluenceLevel(Customer.InfluenceLevel.HIGH));
        stats.put("MEDIUM", customerRepository.countByInfluenceLevel(Customer.InfluenceLevel.MEDIUM));
        stats.put("LOW", customerRepository.countByInfluenceLevel(Customer.InfluenceLevel.LOW));
        return stats;
    }

    /**
     * 获取决策权力统计
     */
    private Map<String, Long> getDecisionPowerStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("DECISION_MAKER", customerRepository.countByDecisionPower(Customer.DecisionPower.DECISION_MAKER));
        stats.put("INFLUENCER", customerRepository.countByDecisionPower(Customer.DecisionPower.INFLUENCER));
        stats.put("USER", customerRepository.countByDecisionPower(Customer.DecisionPower.USER));
        stats.put("OTHER", customerRepository.countByDecisionPower(Customer.DecisionPower.OTHER));
        return stats;
    }

    /**
     * 获取学校分布统计
     */
    private List<Map<String, Object>> getSchoolDistribution() {
        List<Object[]> rawData = customerRepository.findSchoolCustomerDistribution();
        return rawData.stream()
                .limit(10)
                .map(row -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("schoolName", row[0]);
                    item.put("customerCount", row[1]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取院系分布统计
     */
    private List<Map<String, Object>> getDepartmentDistribution() {
        List<Object[]> rawData = customerRepository.findDepartmentCustomerDistribution();
        return rawData.stream()
                .limit(10)
                .map(row -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("departmentName", row[0]);
                    item.put("customerCount", row[1]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取最近客户
     */
    private List<CustomerResponse> getRecentCustomers(int limit) {
        List<Customer> customers = customerRepository.findTop10ByOrderByCreatedAtDesc();
        return customers.stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取我最近的拜访次数
     */
    private long getMyRecentVisitsCount(Long salesId) {
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        return visitRecordRepository.countBySalesIdAndVisitDateAfter(salesId, weekAgo);
    }

    /**
     * 获取我的客户摘要
     */
    private Map<String, Object> getMyCustomerSummary(Long salesId) {
        Map<String, Object> summary = new HashMap<>();
        long totalCustomers = customerRepository.countByCreatedById(salesId);
        long highInfluence = (long) customerRepository.countByCreatedByIdAndInfluenceLevel(salesId, Customer.InfluenceLevel.HIGH);
        long recentVisits = getMyRecentVisitsCount(salesId);

        summary.put("totalCustomers", totalCustomers);
        summary.put("highInfluence", highInfluence);
        summary.put("recentVisits", recentVisits);
        summary.put("monthlyTarget", 20); // 示例目标
        summary.put("completion", totalCustomers > 0 ? (totalCustomers * 100 / 20) : 0); // 完成率
        return summary;
    }

    // ==================== 权限检查方法 ====================

    /**
     * 检查客户访问权限
     */
    private void checkCustomerPermission(Customer customer, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        // 管理员和经理有所有权限
        if (isAdminOrManager(currentUser)) {
            return;
        }

        // 销售人员只能访问自己创建的客户
        if (customer.getCreatedBy() == null || !customer.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new BusinessException("无权访问该客户信息");
        }
    }

    /**
     * 转换为响应对象
     */
    private CustomerResponse convertToResponse(Customer customer) {
        Department department = customer.getDepartment();
        School school = customer.getSchool();
        
        // 如果没有直接关联学校，尝试从院系获取学校信息
        if (school == null && department != null) {
            school = department.getSchool();
        }

        // 统计拜访相关信息
        long visitCount = visitRecordRepository.countByCustomerId(customer.getId());
        LocalDate lastVisitDate = getLastVisitDate(customer.getId());
        String lastIntentLevel = getLastIntentLevel(customer.getId());

        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .position(customer.getPosition())
                .title(customer.getTitle())
                .departmentId(department != null ? department.getId() : null)
                .departmentName(department != null ? department.getName() : null)
                .schoolId(school != null ? school.getId() : null)
                .schoolName(school != null ? school.getName() : null)
                .schoolCity(school != null ? school.getCity() : null)
                .schoolType(school != null ? school.getSchoolType().name() : null)
                .schoolTypeDescription(school != null ? school.getSchoolType().getDescription() : null)
                .phone(customer.getPhone())
                .wechat(customer.getWechat())
                .email(customer.getEmail())
                .officeLocation(customer.getOfficeLocation())
                .floorRoom(customer.getFloorRoom())
                .researchDirection(customer.getResearchDirection())
                .influenceLevel(customer.getInfluenceLevel().name())
                .influenceLevelDescription(customer.getInfluenceLevel().getDescription())
                .decisionPower(customer.getDecisionPower().name())
                .decisionPowerDescription(customer.getDecisionPower().getDescription())
                .birthday(customer.getBirthday())
                .notes(customer.getNotes())
                .visitCount((int) visitCount)
                .lastVisitDate(lastVisitDate)
                .lastIntentLevel(lastIntentLevel)
                .wechatAdded(StringUtils.hasText(customer.getWechat()))
                .createdById(customer.getCreatedBy() != null ? customer.getCreatedBy().getId() : null)
                .createdByName(customer.getCreatedBy() != null ? customer.getCreatedBy().getRealName() : null)
                .updatedById(customer.getUpdatedBy() != null ? customer.getUpdatedBy().getId() : null)
                .updatedByName(customer.getUpdatedBy() != null ? customer.getUpdatedBy().getRealName() : null)
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    /**
     * 获取最后拜访日期
     */
    private LocalDate getLastVisitDate(Long customerId) {
        List<VisitRecord> visitRecords = visitRecordRepository.findByCustomerIdOrderByVisitDateDesc(customerId);
        return visitRecords.isEmpty() ? null : visitRecords.get(0).getVisitDate();
    }

    /**
     * 获取最后意向等级
     */
    private String getLastIntentLevel(Long customerId) {
        List<VisitRecord> visitRecords = visitRecordRepository.findByCustomerIdOrderByVisitDateDesc(customerId);
        if (visitRecords.isEmpty()) {
            return null;
        }
        return visitRecords.get(0).getIntentLevel() != null ?
                visitRecords.get(0).getIntentLevel().name() : null;
    }
}