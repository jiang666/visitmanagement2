package com.proshine.visitmanagement.service;

import com.proshine.visitmanagement.dto.request.UserRequest;
import com.proshine.visitmanagement.dto.response.PageResponse;
import com.proshine.visitmanagement.dto.response.UserResponse;
import com.proshine.visitmanagement.entity.User;
import com.proshine.visitmanagement.exception.BusinessException;
import com.proshine.visitmanagement.exception.ResourceNotFoundException;
import com.proshine.visitmanagement.repository.UserRepository;
import com.proshine.visitmanagement.util.ExcelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务类
 * 
 * @author System
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 分页查询用户
     * 
     * @param keyword 关键词
     * @param role 角色
     * @param department 部门
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页用户列表
     */
    public PageResponse<UserResponse> getUsers(String keyword, User.UserRole role,
                                               String department, User.UserStatus status,
                                               Pageable pageable) {
        Page<User> userPage = userRepository.findUsersWithFilters(keyword, role, department, status, pageable);
        
        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<UserResponse>builder()
                .content(userResponses)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .empty(userPage.isEmpty())
                .build();
    }
    
    /**
     * 根据ID获取用户
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return convertToResponse(user);
    }
    
    /**
     * 根据用户名获取用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return convertToResponse(user);
    }
    
    /**
     * 创建用户
     * 
     * @param request 用户请求
     * @return 创建的用户
     */
    @Transactional
    public UserResponse createUser(UserRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(User.UserRole.valueOf(request.getRole()));
        user.setDepartment(request.getDepartment());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setStatus(User.UserStatus.ACTIVE);
        
        User savedUser = userRepository.save(user);
        log.info("创建用户成功: {}", savedUser.getUsername());
        
        return convertToResponse(savedUser);
    }
    
    /**
     * 更新用户
     * 
     * @param id 用户ID
     * @param request 用户请求
     * @return 更新的用户
     */
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 检查用户名是否已被其他用户使用
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已被其他用户使用
        if (StringUtils.hasText(request.getEmail()) && 
            !request.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }
        
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(User.UserRole.valueOf(request.getRole()));
        user.setDepartment(request.getDepartment());
        user.setAvatarUrl(request.getAvatarUrl());
        
        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        User savedUser = userRepository.save(user);
        log.info("更新用户成功: {}", savedUser.getUsername());
        
        return convertToResponse(savedUser);
    }
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        userRepository.delete(user);
        log.info("删除用户成功: {}", user.getUsername());
    }
    
    /**
     * 批量删除用户
     * 
     * @param ids 用户ID列表
     */
    @Transactional
    public int batchDeleteUsers(List<Long> ids) {
        List<User> users = userRepository.findAllById(ids);
        if (users.isEmpty()) {
            throw new BusinessException("未找到要删除的用户");
        }
        
        userRepository.deleteAll(users);
        log.info("批量删除用户成功，数量: {}", users.size());
        return users.size();
    }

    /**
     * 获取用户列表用于导出
     *
     * @param keyword 关键词
     * @param role 角色
     * @param department 部门
     * @param status 状态
     * @return 用户列表
     */
    public List<UserResponse> getUsersForExport(String keyword, String role,
                                                String department, String status) {
        User.UserRole userRole = null;
        if (StringUtils.hasText(role)) {
            userRole = User.UserRole.valueOf(role);
        }

        User.UserStatus userStatus = null;
        if (StringUtils.hasText(status)) {
            userStatus = User.UserStatus.valueOf(status);
        }

        // 使用现有的查询方法，传入null作为分页参数获取所有数据
        Page<User> userPage = userRepository.findUsersWithFilters(keyword, userRole, department, userStatus, Pageable.unpaged());

        return userPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 总用户数
        long totalUsers = userRepository.count();
        statistics.put("totalUsers", totalUsers);

        // 活跃用户数
        long activeUsers = userRepository.findByStatus(User.UserStatus.ACTIVE).size();
        statistics.put("activeUsers", activeUsers);

        // 各角色用户统计
        List<Object[]> roleStats = userRepository.countUsersByRole();
        Map<String, Long> roleStatMap = new HashMap<>();
        for (Object[] stat : roleStats) {
            User.UserRole role = (User.UserRole) stat[0];
            Long count = (Long) stat[1];
            roleStatMap.put(role.name(), count);
        }
        statistics.put("roleStatistics", roleStatMap);

        // 各部门用户统计
        List<Object[]> departmentStats = userRepository.countUsersByDepartment();
        Map<String, Long> departmentStatMap = new HashMap<>();
        for (Object[] stat : departmentStats) {
            String department = (String) stat[0];
            Long count = (Long) stat[1];
            departmentStatMap.put(department, count);
        }
        statistics.put("departmentStatistics", departmentStatMap);

        // 最近7天注册用户数
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long recentRegistrations = userRepository.countByCreatedAtAfter(sevenDaysAgo);
        statistics.put("recentRegistrations", recentRegistrations);

        return statistics;
    }

    /**
     * 从Excel文件批量导入用户
     *
     * @param file Excel文件
     * @return 导入结果
     */
    @Transactional
    public Map<String, Object> importUsersFromExcel(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        try {
            // 这里需要使用Excel解析工具类来读取文件
            // 假设ExcelUtils.parseExcel返回List<Map<String, Object>>
            List<Map<String, Object>> excelData = ExcelUtils.parseExcel(file);

            for (int i = 0; i < excelData.size(); i++) {
                Map<String, Object> row = excelData.get(i);
                try {
                    // 验证必填字段
                    String username = (String) row.get("username");
                    String password = (String) row.get("password");
                    String realName = (String) row.get("realName");
                    String email = (String) row.get("email");
                    String phone = (String) row.get("phone");
                    String role = (String) row.get("role");
                    String department = (String) row.get("department");

                    if (!StringUtils.hasText(username) || !StringUtils.hasText(password)
                            || !StringUtils.hasText(realName)) {
                        errors.add(String.format("第%d行：用户名、密码、真实姓名不能为空", i + 2));
                        failCount++;
                        continue;
                    }

                    // 检查用户名是否已存在
                    if (userRepository.existsByUsername(username)) {
                        errors.add(String.format("第%d行：用户名'%s'已存在", i + 2, username));
                        failCount++;
                        continue;
                    }

                    // 检查邮箱是否已存在
                    if (StringUtils.hasText(email) && userRepository.existsByEmail(email)) {
                        errors.add(String.format("第%d行：邮箱'%s'已存在", i + 2, email));
                        failCount++;
                        continue;
                    }

                    // 创建用户
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode(password));
                    user.setRealName(realName);
                    user.setEmail(email);
                    user.setPhone(phone);

                    // 验证并设置角色
                    try {
                        user.setRole(User.UserRole.valueOf(role.toUpperCase()));
                    } catch (Exception e) {
                        user.setRole(User.UserRole.SALES); // 默认角色
                    }

                    user.setDepartment(department);
                    user.setStatus(User.UserStatus.ACTIVE);

                    userRepository.save(user);
                    successCount++;

                } catch (Exception e) {
                    errors.add(String.format("第%d行：导入失败 - %s", i + 2, e.getMessage()));
                    failCount++;
                    log.error("导入用户失败: 第{}行", i + 2, e);
                }
            }

            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("errors", errors);
            result.put("totalCount", excelData.size());

            log.info("用户批量导入完成: 成功{}个, 失败{}个", successCount, failCount);

        } catch (Exception e) {
            log.error("解析Excel文件失败", e);
            result.put("successCount", 0);
            result.put("failCount", 0);
            result.put("errors", Arrays.asList("Excel文件解析失败: " + e.getMessage()));
            result.put("totalCount", 0);
        }

        return result;
    }

    /**
     * 更新用户状态
     * 
     * @param id 用户ID
     * @param status 新状态
     */
    @Transactional
    public void updateUserStatus(Long id, User.UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        user.setStatus(status);
        userRepository.save(user);
        log.info("更新用户状态成功: {} -> {}", user.getUsername(), status);
    }
    
    /**
     * 重置用户密码
     * 
     * @param id 用户ID
     * @param newPassword 新密码
     */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("重置用户密码成功: {}", user.getUsername());
    }
    
    /**
     * 更新最后登录时间
     * 
     * @param id 用户ID
     */
    @Transactional
    public void updateLastLoginTime(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * 根据角色获取用户列表
     * 
     * @param role 角色
     * @return 用户列表
     */
    public List<UserResponse> getUsersByRole(User.UserRole role) {
        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据部门获取用户列表
     * 
     * @param department 部门
     * @return 用户列表
     */
    public List<UserResponse> getUsersByDepartment(String department) {
        List<User> users = userRepository.findByDepartment(department);
        return users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取活跃用户列表
     * 
     * @return 活跃用户列表
     */
    public List<UserResponse> getActiveUsers() {
        List<User> users = userRepository.findByStatus(User.UserStatus.ACTIVE);
        return users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据角色和部门获取活跃用户
     * 
     * @param role 角色
     * @param department 部门
     * @return 用户列表
     */
    public List<UserResponse> getActiveUsersByRoleAndDepartment(User.UserRole role, String department) {
        List<User> users = userRepository.findActiveUsersByRoleAndDepartment(role, department);
        return users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取销售人员列表
     * 
     * @return 销售人员列表
     */
    public List<UserResponse> getSalesUsers() {
        return getUsersByRole(User.UserRole.SALES);
    }
    
    /**
     * 获取管理人员列表
     * 
     * @return 管理人员列表
     */
    public List<UserResponse> getManagerUsers() {
        return getUsersByRole(User.UserRole.MANAGER);
    }
    
    /**
     * 检查当前用户权限
     * 
     * @param targetUserId 目标用户ID
     * @param authentication 认证信息
     * @return 是否有权限
     */
    public boolean hasPermission(Long targetUserId, Authentication authentication) {
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("当前用户不存在"));
        
        // 管理员有所有权限
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            return true;
        }
        
        // 经理只能管理同部门的销售人员
        if (currentUser.getRole() == User.UserRole.MANAGER) {
            User targetUser = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("目标用户不存在"));
            return StringUtils.hasText(currentUser.getDepartment()) &&
                   currentUser.getDepartment().equals(targetUser.getDepartment()) &&
                   targetUser.getRole() == User.UserRole.SALES;
        }
        
        // 销售人员只能查看自己的信息
        return currentUser.getId().equals(targetUserId);
    }
    
    /**
     * 检查用户是否存在
     * 
     * @param id 用户ID
     * @return 是否存在
     */
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    public boolean existsByEmail(String email) {
        return StringUtils.hasText(email) && userRepository.existsByEmail(email);
    }
    
    /**
     * 统计各部门用户数量
     * 
     * @return 部门用户统计
     */
    public List<Object[]> countUsersByDepartment() {
        return userRepository.countUsersByDepartment();
    }
    
    /**
     * 统计各角色用户数量
     * 
     * @return 角色用户统计
     */
    public List<Object[]> countUsersByRole() {
        return userRepository.countUsersByRole();
    }
    
    /**
     * 获取用户总数
     * 
     * @return 用户总数
     */
    public long getTotalUserCount() {
        return userRepository.count();
    }
    
    /**
     * 获取活跃用户总数
     * 
     * @return 活跃用户总数
     */
    public long getActiveUserCount() {
        return userRepository.findByStatus(User.UserStatus.ACTIVE).size();
    }
    
    /**
     * 更新用户头像
     * 
     * @param id 用户ID
     * @param avatarUrl 头像URL
     */
    @Transactional
    public void updateAvatar(Long id, String avatarUrl) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        log.info("更新用户头像成功: {}", user.getUsername());
    }
    
    /**
     * 更新用户个人信息
     * 
     * @param id 用户ID
     * @param realName 真实姓名
     * @param email 邮箱
     * @param phone 手机号
     */
    @Transactional
    public void updateProfile(Long id, String realName, String email, String phone) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 检查邮箱是否已被其他用户使用
        if (StringUtils.hasText(email) && !email.equals(user.getEmail()) && 
            userRepository.existsByEmail(email)) {
            throw new BusinessException("邮箱已存在");
        }
        
        if (StringUtils.hasText(realName)) {
            user.setRealName(realName);
        }
        if (StringUtils.hasText(email)) {
            user.setEmail(email);
        }
        if (StringUtils.hasText(phone)) {
            user.setPhone(phone);
        }
        
        userRepository.save(user);
        log.info("更新用户个人信息成功: {}", user.getUsername());
    }

    /**
     * 修改用户状态（启用/禁用）
     *
     * @param id 用户ID
     * @param enabled 是否启用
     */
    @Transactional
    public void changeUserStatus(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        User.UserStatus newStatus = enabled ? User.UserStatus.ACTIVE : User.UserStatus.INACTIVE;
        user.setStatus(newStatus);
        userRepository.save(user);

        log.info("修改用户状态成功: {} -> {}", user.getUsername(), newStatus);
    }

    /**
     * 检查是否为当前用户
     *
     * @param userId 用户ID
     * @param authentication 认证信息
     * @return 是否为当前用户
     */
    public boolean isCurrentUser(Long userId, Authentication authentication) {
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("当前用户不存在"));

        return currentUser.getId().equals(userId);
    }

    /**
     * 转换为响应对象
     * 
     * @param user 用户实体
     * @return 用户响应
     */
    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .roleDescription(user.getRole().getDescription())
                .department(user.getDepartment())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus().name())
                .statusDescription(user.getStatus().getDescription())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}