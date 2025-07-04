package com.proshine.visitmanagement.service;

import com.proshine.visitmanagement.dto.request.DepartmentRequest;
import com.proshine.visitmanagement.dto.response.DepartmentResponse;
import com.proshine.visitmanagement.dto.response.PageResponse;
import com.proshine.visitmanagement.entity.Customer;
import com.proshine.visitmanagement.entity.Department;
import com.proshine.visitmanagement.entity.School;
import com.proshine.visitmanagement.entity.User;
import com.proshine.visitmanagement.exception.BusinessException;
import com.proshine.visitmanagement.exception.ResourceNotFoundException;
import com.proshine.visitmanagement.repository.CustomerRepository;
import com.proshine.visitmanagement.repository.DepartmentRepository;
import com.proshine.visitmanagement.repository.SchoolRepository;
import com.proshine.visitmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 院系服务类
 *
 * @author System
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final SchoolRepository schoolRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    /**
     * 分页查询院系
     *
     * @param keyword 关键词（院系名称）
     * @param schoolId 学校ID
     * @param pageable 分页参数
     * @param authentication 认证信息
     * @return 分页院系列表
     */
    public PageResponse<DepartmentResponse> getDepartments(String keyword, Long schoolId, 
                                                          Pageable pageable, Authentication authentication) {
        log.debug("分页查询院系: keyword={}, schoolId={}, page={}, size={}", 
                keyword, schoolId, pageable.getPageNumber(), pageable.getPageSize());

        // 检查权限 - 只有管理员和经理可以查看所有院系，销售只能查看相关院系
        User currentUser = getCurrentUser(authentication);
        
        Page<Department> departmentPage;
        
        if (currentUser.getRole() == User.UserRole.ADMIN || currentUser.getRole() == User.UserRole.MANAGER) {
            // 管理员和经理可以查看所有院系
            departmentPage = departmentRepository.findDepartmentsWithFilters(keyword, schoolId, null, pageable);
        } else {
            // 销售人员只能查看有客户的院系（简化处理）
            departmentPage = departmentRepository.findDepartmentsWithFilters(keyword, schoolId, true, pageable);
        }

        List<DepartmentResponse> departmentResponses = departmentPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResponse.<DepartmentResponse>builder()
                .content(departmentResponses)
                .page(departmentPage.getNumber())
                .size(departmentPage.getSize())
                .totalElements(departmentPage.getTotalElements())
                .totalPages(departmentPage.getTotalPages())
                .first(departmentPage.isFirst())
                .last(departmentPage.isLast())
                .empty(departmentPage.isEmpty())
                .build();
    }

    /**
     * 根据ID获取院系详情
     *
     * @param id 院系ID
     * @param authentication 认证信息
     * @return 院系详情
     */
    public DepartmentResponse getDepartment(Long id, Authentication authentication) {
        log.debug("获取院系详情: id={}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("院系不存在"));

        // 检查权限
        checkDepartmentPermission(department, authentication);

        return convertToResponse(department);
    }

    /**
     * 根据学校ID获取院系列表
     *
     * @param schoolId 学校ID
     * @param authentication 认证信息
     * @return 院系列表
     */
    public List<DepartmentResponse> getDepartmentsBySchool(Long schoolId, Authentication authentication) {
        log.debug("获取学校院系列表: schoolId={}", schoolId);

        // 验证学校是否存在
        schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("学校不存在"));

        User currentUser = getCurrentUser(authentication);
        List<Department> departments;

        if (currentUser.getRole() == User.UserRole.ADMIN || currentUser.getRole() == User.UserRole.MANAGER) {
            // 管理员和经理可以查看所有院系
            departments = departmentRepository.findBySchoolIdOrderByName(schoolId);
        } else {
            // 销售人员可以查看所有院系（用于创建客户时选择）
            departments = departmentRepository.findBySchoolIdOrderByName(schoolId);
        }

        return departments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 创建院系
     *
     * @param request 院系请求
     * @param authentication 认证信息
     * @return 创建的院系
     */
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request, Authentication authentication) {
        log.info("创建院系: name={}, schoolId={}", request.getName(), request.getSchoolId());

        // 检查权限 - 只有管理员和经理可以创建院系
        checkAdminOrManagerPermission(authentication);

        // 验证学校是否存在
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("学校不存在"));

        // 检查院系名称在该学校中是否已存在
        if (departmentRepository.existsBySchoolIdAndName(request.getSchoolId(), request.getName())) {
            throw new BusinessException("该学校中已存在同名院系");
        }

        // 检查联系电话是否已被使用（如果提供）
        if (StringUtils.hasText(request.getContactPhone()) &&
            departmentRepository.findByContactPhone(request.getContactPhone()).isPresent()) {
            throw new BusinessException("该联系电话已被其他院系使用");
        }

        Department department = new Department();
        department.setSchool(school);
        department.setName(request.getName());
        department.setContactPhone(request.getContactPhone());
        department.setAddress(request.getAddress());
        department.setDescription(request.getDescription());

        Department savedDepartment = departmentRepository.save(department);
        log.info("院系创建成功: id={}, name={}", savedDepartment.getId(), savedDepartment.getName());

        return convertToResponse(savedDepartment);
    }

    /**
     * 更新院系
     *
     * @param id 院系ID
     * @param request 院系请求
     * @param authentication 认证信息
     * @return 更新的院系
     */
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request, Authentication authentication) {
        log.info("更新院系: id={}, name={}", id, request.getName());

        // 检查权限 - 只有管理员和经理可以更新院系
        checkAdminOrManagerPermission(authentication);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("院系不存在"));

        // 如果要更改学校，验证新学校是否存在
        if (!department.getSchool().getId().equals(request.getSchoolId())) {
            School newSchool = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("新学校不存在"));
            department.setSchool(newSchool);
        }

        // 检查院系名称在该学校中是否已被其他院系使用
        if (!department.getName().equals(request.getName()) &&
            departmentRepository.existsBySchoolIdAndNameAndIdNot(request.getSchoolId(), request.getName(), id)) {
            throw new BusinessException("该学校中已存在同名院系");
        }

        // 检查联系电话是否已被其他院系使用（如果提供且有变更）
        if (StringUtils.hasText(request.getContactPhone()) &&
            !request.getContactPhone().equals(department.getContactPhone())) {
            departmentRepository.findByContactPhone(request.getContactPhone())
                    .ifPresent(existingDept -> {
                        if (!existingDept.getId().equals(id)) {
                            throw new BusinessException("该联系电话已被其他院系使用");
                        }
                    });
        }

        department.setName(request.getName());
        department.setContactPhone(request.getContactPhone());
        department.setAddress(request.getAddress());
        department.setDescription(request.getDescription());

        Department savedDepartment = departmentRepository.save(department);
        log.info("院系更新成功: id={}, name={}", savedDepartment.getId(), savedDepartment.getName());

        return convertToResponse(savedDepartment);
    }

    /**
     * 删除院系
     *
     * @param id 院系ID
     * @param authentication 认证信息
     */
    @Transactional
    public void deleteDepartment(Long id, Authentication authentication) {
        log.info("删除院系: id={}", id);

        // 检查权限 - 只有管理员和经理可以删除院系
        checkAdminOrManagerPermission(authentication);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("院系不存在"));

        // 检查是否有关联的客户
        long customerCount = customerRepository.countByDepartmentId(id);
        if (customerCount > 0) {
            throw new BusinessException("该院系下存在 " + customerCount + " 个客户，无法删除");
        }

        departmentRepository.delete(department);
        log.info("院系删除成功: id={}, name={}", id, department.getName());
    }

    /**
     * 批量删除院系
     *
     * @param ids 院系ID列表
     * @param authentication 认证信息
     * @return 删除数量
     */
    @Transactional
    public int batchDeleteDepartments(List<Long> ids, Authentication authentication) {
        log.info("批量删除院系: ids={}", ids);

        // 检查权限 - 只有管理员和经理可以批量删除院系
        checkAdminOrManagerPermission(authentication);

        List<Department> departments = departmentRepository.findAllById(ids);
        if (departments.isEmpty()) {
            throw new BusinessException("未找到要删除的院系");
        }

        // 检查是否有关联的客户
        for (Department department : departments) {
            long customerCount = customerRepository.countByDepartmentId(department.getId());
            if (customerCount > 0) {
                throw new BusinessException("院系 " + department.getName() + " 下存在 " + customerCount + " 个客户，无法删除");
            }
        }

        departmentRepository.deleteAll(departments);
        log.info("批量删除院系成功，数量: {}", departments.size());

        return departments.size();
    }

    // ==================== 辅助方法 ====================

    /**
     * 检查院系访问权限
     *
     * @param department 院系
     * @param authentication 认证信息
     */
    private void checkDepartmentPermission(Department department, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // 管理员和经理有所有权限
        if (currentUser.getRole() == User.UserRole.ADMIN || currentUser.getRole() == User.UserRole.MANAGER) {
            return;
        }
        
        // 销售人员可以查看所有院系（因为需要在创建客户时选择）
        // 如果需要更严格的权限控制，可以在这里添加限制
    }

    /**
     * 检查管理员或经理权限
     *
     * @param authentication 认证信息
     */
    private void checkAdminOrManagerPermission(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() != User.UserRole.ADMIN && currentUser.getRole() != User.UserRole.MANAGER) {
            throw new BusinessException("只有管理员和经理才能执行此操作");
        }
    }

    /**
     * 获取当前用户
     *
     * @param authentication 认证信息
     * @return 用户对象
     */
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("当前用户不存在"));
    }

    /**
     * 转换为响应对象
     *
     * @param department 院系实体
     * @return 院系响应
     */
    private DepartmentResponse convertToResponse(Department department) {
        // 统计客户数量
        long customerCountLong = customerRepository.countByDepartmentId(department.getId());
        int customerCount = (int) customerCountLong;

        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .schoolId(department.getSchool() != null ? department.getSchool().getId() : null)
                .schoolName(department.getSchool() != null ? department.getSchool().getName() : null)
                .contactPhone(department.getContactPhone())
                .address(department.getAddress())
                .description(department.getDescription())
                .customerCount(customerCount)
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}