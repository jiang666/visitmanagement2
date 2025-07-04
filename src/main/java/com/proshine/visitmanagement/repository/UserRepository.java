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
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByEmail(String email);

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
     * 根据角色查找用户
     *
     * @param role 角色
     * @return 用户列表
     */
    List<User> findByRole(User.UserRole role);

    /**
     * 根据部门查找用户
     *
     * @param department 部门
     * @return 用户列表
     */
    List<User> findByDepartment(String department);

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

    /**
     * 根据角色和部门查找活跃用户
     *
     * @param role 角色
     * @param department 部门
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.department = :department " +
            "AND u.status = com.proshine.visitmanagement.entity.User$UserStatus.ACTIVE " +
            "ORDER BY u.createdAt DESC")
    List<User> findActiveUsersByRoleAndDepartment(@Param("role") User.UserRole role,
                                                  @Param("department") String department);

    /**
     * 根据角色和状态查找用户
     *
     * @param role 角色
     * @param status 状态
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status ORDER BY u.createdAt DESC")
    List<User> findByRoleAndStatus(@Param("role") User.UserRole role, @Param("status") User.UserStatus status);

    /**
     * 根据部门和状态查找用户
     *
     * @param department 部门
     * @param status 状态
     * @return 用户列表
     */
    List<User> findByDepartmentAndStatus(String department, User.UserStatus status);

    // ==================== 统计查询方法 ====================

    /**
     * 统计指定时间之后注册的用户数量
     *
     * @param createdAt 指定时间
     * @return 用户数量
     */
    long countByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * 根据角色统计用户数量
     *
     * @param role 角色
     * @return 用户数量
     */
    long countByRole(User.UserRole role);

    /**
     * 根据状态统计用户数量
     *
     * @param status 状态
     * @return 用户数量
     */
    long countByStatus(User.UserStatus status);

    /**
     * 根据部门统计用户数量
     *
     * @param department 部门
     * @return 用户数量
     */
    long countByDepartment(String department);

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

    /**
     * 统计各状态用户数量
     *
     * @return 状态用户统计（状态，用户数量）
     */
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> countUsersByStatus();

    /**
     * 根据创建时间范围统计用户数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户数量
     */
    long countByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

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

    // ==================== 查询列表方法 ====================

    /**
     * 查找最近创建的用户
     *
     * @param pageable 分页参数
     * @return 最近创建的用户列表
     */
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(Pageable pageable);

    /**
     * 查找最近登录的用户
     *
     * @param pageable 分页参数
     * @return 最近登录的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NOT NULL ORDER BY u.lastLoginAt DESC")
    List<User> findRecentLoginUsers(Pageable pageable);

    /**
     * 根据创建时间范围查找用户列表
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户列表
     */
    List<User> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据最后登录时间范围查找用户列表
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户列表
     */
    List<User> findByLastLoginAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 特殊查询方法 ====================

    /**
     * 查找活跃用户列表
     *
     * @return 活跃用户列表
     */
    @Query("SELECT u FROM User u WHERE u.status = com.proshine.visitmanagement.entity.User$UserStatus.ACTIVE ORDER BY u.createdAt DESC")
    List<User> findActiveUsers();

    /**
     * 查找管理员用户列表
     *
     * @return 管理员用户列表
     */
    @Query("SELECT u FROM User u WHERE u.role = com.proshine.visitmanagement.entity.User$UserRole.ADMIN AND u.status = com.proshine.visitmanagement.entity.User$UserStatus.ACTIVE ORDER BY u.createdAt DESC")
    List<User> findAdminUsers();

    /**
     * 查找销售人员列表
     *
     * @return 销售人员列表
     */
    @Query("SELECT u FROM User u WHERE u.role = com.proshine.visitmanagement.entity.User$UserRole.SALES AND u.status = com.proshine.visitmanagement.entity.User$UserStatus.ACTIVE ORDER BY u.realName")
    List<User> findSalesUsers();

    /**
     * 查找指定部门的活跃用户
     *
     * @param department 部门
     * @return 活跃用户列表
     */
    @Query("SELECT u FROM User u WHERE u.department = :department AND u.status = com.proshine.visitmanagement.entity.User$UserStatus.ACTIVE ORDER BY u.realName")
    List<User> findActiveUsersByDepartment(@Param("department") String department);

    /**
     * 查找长时间未登录的用户
     *
     * @param beforeDate 指定日期之前
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL OR u.lastLoginAt < :beforeDate ORDER BY u.lastLoginAt ASC")
    List<User> findUsersNotLoginSince(@Param("beforeDate") LocalDateTime beforeDate);
}