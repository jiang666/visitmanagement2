package com.proshine.visitmanagement.repository;

import com.proshine.visitmanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 *
 * @author System
 * @since 2024-01-01
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据角色查询用户 - 用于 UserService
     */
    List<User> findByRole(User.UserRole role);
    
    /**
     * 根据部门查询用户 - 用于 UserService
     */
    List<User> findByDepartment(String department);
    
    /**
     * 根据角色和部门查询活跃用户 - 用于 UserService
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.department = :department AND u.status = 'ACTIVE'")
    List<User> findActiveUsersByRoleAndDepartment(@Param("role") User.UserRole role, @Param("department") String department);

    /**
     * 根据状态查找用户
     *
     * @param status 状态
     * @return 用户列表
     */
    List<User> findByStatus(User.UserStatus status);

    // ==================== 复杂查询方法 ====================

    /**
     * 分页查询用户（支持关键词搜索和筛选）
     *
     * @param keyword 关键词（用户名、真实姓名、邮箱）
     * @param role 角色
     * @param department 部门
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页用户列表
     */
    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "u.username LIKE %:keyword% OR u.realName LIKE %:keyword% OR u.email LIKE %:keyword%) " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:department IS NULL OR :department = '' OR u.department = :department) " +
            "AND (:status IS NULL OR u.status = :status) " +
            "ORDER BY u.createdAt DESC")
    Page<User> findUsersWithFilters(@Param("keyword") String keyword,
                                    @Param("role") User.UserRole role,
                                    @Param("department") String department,
                                    @Param("status") User.UserStatus status,
                                    Pageable pageable);

    // ==================== 统计查询方法 ====================

    /**
     * 统计指定时间之后注册的用户数量
     *
     * @param createdAt 指定时间
     * @return 用户数量
     */
    long countByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * 统计各部门用户数量
     *
     * @return 部门用户统计（部门名称，用户数量）
     */
    @Query("SELECT u.department, COUNT(u) FROM User u WHERE u.status = com.proshine.visitmanagement.entity.User$UserStatus.ACTIVE GROUP BY u.department")
    List<Object[]> countUsersByDepartment();

    /**
     * 统计各角色用户数量
     *
     * @return 角色用户统计（角色，用户数量）
     */
    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.status = com.proshine.visitmanagement.entity.User$UserStatus.ACTIVE GROUP BY u.role")
    List<Object[]> countUsersByRole();

    // ==================== 验证查询方法 ====================

    /**
     * 检查用户名是否存在（排除指定ID）
     *
     * @param username 用户名
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :excludeId")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("excludeId") Long excludeId);

    /**
     * 检查邮箱是否存在（排除指定ID）
     *
     * @param email 邮箱
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :excludeId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("excludeId") Long excludeId);
}