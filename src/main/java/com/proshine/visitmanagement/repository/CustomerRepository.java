package com.proshine.visitmanagement.repository;

import com.proshine.visitmanagement.entity.Customer;
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
 * 客户数据访问层接口 - 清理后的版本，仅保留项目中实际使用的方法
 *
 * @author System
 * @since 2024-01-01
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据邮箱查询客户 - 用于客户验证
     */
    Optional<Customer> findByEmail(String email);

    /**
     * 根据姓名模糊查询客户 - 用于客户搜索
     */
    List<Customer> findByNameContaining(String name);

    /**
     * 根据院系ID查询客户 - 用于院系管理和客户服务
     */
    List<Customer> findByDepartmentId(Long departmentId);

    /**
     * 根据创建人 ID查询客户 - 用于仪表板服务
     */
    @Query("SELECT c FROM Customer c WHERE c.createdBy.id = :createdById")
    List<Customer> findByCreatedById(@Param("createdById") Long createdById);
    
    /**
     * 根据影响力等级查询客户 - 用于 CustomerService
     */
    List<Customer> findByInfluenceLevel(Customer.InfluenceLevel level);
    
    /**
     * 统计指定时间后创建的客户数量 - 用于 DashboardService
     */
    long countByCreatedAtAfter(LocalDateTime createdAfter);

    // ==================== 时间范围查询 ====================

    /**
     * 根据创建时间范围查询客户 - 用于仪表板服务
     */
    List<Customer> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据创建人和创建时间范围查询客户 - 用于仪表板服务
     */
    @Query("SELECT c FROM Customer c WHERE c.createdBy.id = :createdById AND c.createdAt BETWEEN :startTime AND :endTime")
    List<Customer> findByCreatedByAndCreatedAtBetween(@Param("createdById") Long createdById, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // ==================== 统计查询方法 ====================

    /**
     * 根据影响力等级统计客户数量 - 用于客户服务统计
     */
    long countByInfluenceLevel(Customer.InfluenceLevel influenceLevel);

    /**
     * 根据决策权力统计客户数量 - 用于客户服务统计
     */
    long countByDecisionPower(Customer.DecisionPower decisionPower);

    /**
     * 根据创建时间范围统计客户数量 - 用于客户服务统计
     */
    long countByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据创建人ID统计客户数量 - 用于客户服务和仪表板服务
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdBy.id = :createdById")
    long countByCreatedById(@Param("createdById") Long createdById);

    /**
     * 根据销售人员ID和创建时间之后统计客户数量 - 用于仪表板服务
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdBy.id = :salesId AND c.createdAt >= :startTime")
    long countBySalesIdAndCreatedAtAfter(@Param("salesId") Long salesId, @Param("startTime") LocalDateTime startTime);

    /**
     * 根据销售人员ID和创建时间范围统计客户数量 - 用于仪表板服务
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdBy.id = :salesId AND c.createdAt BETWEEN :startTime AND :endTime")
    long countBySalesIdAndCreatedAtBetween(@Param("salesId") Long salesId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据创建人ID和创建时间范围统计客户数量 - 用于客户服务
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdBy.id = :createdById AND c.createdAt BETWEEN :startTime AND :endTime")
    long countByCreatedByIdAndCreatedAtBetween(@Param("createdById") Long createdById, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据院系ID统计客户数量 - 用于客户服务和院系服务
     */
    long countByDepartmentId(Long id);

    /**
     * 根据创建人ID和影响力等级统计客户数量 - 用于客户服务
     */
    Object countByCreatedByIdAndInfluenceLevel(Long salesId, Customer.InfluenceLevel influenceLevel);

    // ==================== 分页查询方法 ====================

    /**
     * 多条件分页查询客户（管理员使用）- 用于客户服务
     */
    @Query("SELECT c FROM Customer c " +
            "LEFT JOIN c.department d " +
            "LEFT JOIN d.school s " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "       LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "       LOWER(c.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "       LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "       LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:departmentId IS NULL OR c.department.id = :departmentId) " +
            "AND (:schoolId IS NULL OR d.school.id = :schoolId) " +
            "AND (:schoolCity IS NULL OR :schoolCity = '' OR s.city = :schoolCity) " +
            "AND (:influenceLevel IS NULL OR c.influenceLevel = :influenceLevel) " +
            "AND (:decisionPower IS NULL OR c.decisionPower = :decisionPower) " +
            "AND (:hasWechat IS NULL OR (:hasWechat = TRUE AND c.wechat IS NOT NULL AND c.wechat <> '') OR " +
            "     (:hasWechat = FALSE AND (c.wechat IS NULL OR c.wechat = ''))) " +
            "ORDER BY c.createdAt DESC")
    Page<Customer> findCustomersWithAllFilters(@Param("keyword") String keyword,
                                               @Param("departmentId") Long departmentId,
                                               @Param("schoolId") Long schoolId,
                                               @Param("schoolCity") String schoolCity,
                                               @Param("influenceLevel") Customer.InfluenceLevel influenceLevelEnum,
                                               @Param("decisionPower") Customer.DecisionPower decisionPowerEnum,
                                               @Param("hasWechat") Boolean hasWechat,
                                               Pageable pageable);

    /**
     * 多条件分页查询客户（销售人员使用）- 用于客户服务
     */
    @Query("SELECT c FROM Customer c " +
            "LEFT JOIN c.department d " +
            "LEFT JOIN d.school s " +
            "WHERE c.createdBy.id = :createdById " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(c.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:departmentId IS NULL OR c.department.id = :departmentId) " +
            "AND (:schoolId IS NULL OR d.school.id = :schoolId) " +
            "AND (:schoolCity IS NULL OR :schoolCity = '' OR s.city = :schoolCity) " +
            "AND (:influenceLevel IS NULL OR c.influenceLevel = :influenceLevel) " +
            "AND (:decisionPower IS NULL OR c.decisionPower = :decisionPower) " +
            "AND (:hasWechat IS NULL OR (:hasWechat = TRUE AND c.wechat IS NOT NULL AND c.wechat <> '') OR " +
            "     (:hasWechat = FALSE AND (c.wechat IS NULL OR c.wechat = ''))) " +
            "ORDER BY c.createdAt DESC")
    Page<Customer> findCustomersWithAllFiltersByCreatedBy(@Param("keyword") String keyword,
                                                         @Param("departmentId") Long departmentId,
                                                         @Param("schoolId") Long schoolId,
                                                         @Param("schoolCity") String schoolCity,
                                                         @Param("influenceLevel") Customer.InfluenceLevel influenceLevelEnum,
                                                         @Param("decisionPower") Customer.DecisionPower decisionPowerEnum,
                                                         @Param("hasWechat") Boolean hasWechat,
                                                         @Param("createdById") Long id,
                                                         Pageable pageable);

    // ==================== 导出查询方法 ====================

    /**
     * 多条件查询客户用于导出（管理员使用）- 用于客户服务
     */
    @Query("SELECT c FROM Customer c " +
            "LEFT JOIN c.department d " +
            "LEFT JOIN d.school s " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "       LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "       LOWER(c.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "       LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "       LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:departmentId IS NULL OR c.department.id = :departmentId) " +
            "AND (:schoolId IS NULL OR d.school.id = :schoolId) " +
            "AND (:schoolCity IS NULL OR :schoolCity = '' OR s.city = :schoolCity) " +
            "AND (:influenceLevel IS NULL OR c.influenceLevel = :influenceLevel) " +
            "AND (:decisionPower IS NULL OR c.decisionPower = :decisionPower) " +
            "AND (:hasWechat IS NULL OR (:hasWechat = TRUE AND c.wechat IS NOT NULL AND c.wechat <> '') OR " +
            "     (:hasWechat = FALSE AND (c.wechat IS NULL OR c.wechat = ''))) " +
            "ORDER BY c.createdAt DESC")
    List<Customer> findCustomersForExport(@Param("keyword") String keyword,
                                          @Param("departmentId") Long departmentId,
                                          @Param("schoolId") Long schoolId,
                                          @Param("schoolCity") String schoolCity,
                                          @Param("influenceLevel") Customer.InfluenceLevel influenceLevelEnum,
                                          @Param("decisionPower") Customer.DecisionPower decisionPowerEnum,
                                          @Param("hasWechat") Boolean hasWechat);

    /**
     * 多条件查询客户用于导出（销售人员使用）- 用于客户服务
     */
    @Query("SELECT c FROM Customer c " +
            "LEFT JOIN c.department d " +
            "LEFT JOIN d.school s " +
            "WHERE c.createdBy.id = :createdById " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(c.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:departmentId IS NULL OR c.department.id = :departmentId) " +
            "AND (:schoolId IS NULL OR d.school.id = :schoolId) " +
            "AND (:schoolCity IS NULL OR :schoolCity = '' OR s.city = :schoolCity) " +
            "AND (:influenceLevel IS NULL OR c.influenceLevel = :influenceLevel) " +
            "AND (:decisionPower IS NULL OR c.decisionPower = :decisionPower) " +
            "AND (:hasWechat IS NULL OR (:hasWechat = TRUE AND c.wechat IS NOT NULL AND c.wechat <> '') OR " +
            "     (:hasWechat = FALSE AND (c.wechat IS NULL OR c.wechat = ''))) " +
            "ORDER BY c.createdAt DESC")
    List<Customer> findCustomersForExportByCreatedBy(@Param("keyword") String keyword,
                                                     @Param("departmentId") Long departmentId,
                                                     @Param("schoolId") Long schoolId,
                                                     @Param("schoolCity") String schoolCity,
                                                     @Param("influenceLevel") Customer.InfluenceLevel influenceLevelEnum,
                                                     @Param("decisionPower") Customer.DecisionPower decisionPowerEnum,
                                                     @Param("hasWechat") Boolean hasWechat,
                                                     @Param("createdById") Long id);

    // ==================== 其他查询方法 ====================

    /**
     * 查询最近创建的10个客户 - 用于客户服务
     */
    List<Customer> findTop10ByOrderByCreatedAtDesc();

    /**
     * 根据生日月份查询客户 - 用于客户服务
     */
    @Query("SELECT c FROM Customer c WHERE MONTH(c.birthday) = :month")
    List<Customer> findCustomersByBirthdayMonth(@Param("month") int month);

    // ==================== 统计分析方法 ====================

    /**
     * 获取学校客户分布统计 - 用于客户服务
     */
    @Query("SELECT s.name as schoolName, COUNT(c) as customerCount " +
            "FROM Customer c " +
            "JOIN c.department d " +
            "JOIN d.school s " +
            "GROUP BY s.id, s.name " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> findSchoolCustomerDistribution();

    /**
     * 获取院系客户分布统计 - 用于客户服务
     */
    @Query("SELECT d.name as departmentName, COUNT(c) as customerCount " +
            "FROM Customer c " +
            "JOIN c.department d " +
            "GROUP BY d.id, d.name " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> findDepartmentCustomerDistribution();
}
