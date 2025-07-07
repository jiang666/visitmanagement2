package com.proshine.visitmanagement.repository;

import com.proshine.visitmanagement.entity.Customer;
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
 * 客户数据访问层接口
 *
 * @author System
 * @since 2024-01-01
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据姓名模糊查询客户
     */
    List<Customer> findByNameContaining(String name);

    /**
     * 根据院系ID查询客户
     */
    List<Customer> findByDepartmentId(Long departmentId);

    /**
     * 根据创建人查询客户
     */
    List<Customer> findByCreatedBy(User createdBy);

    /**
     * 根据创建人ID查询客户
     */
    @Query("SELECT c FROM Customer c WHERE c.createdBy.id = :createdById")
    List<Customer> findByCreatedById(@Param("createdById") Long createdById);

    /**
     * 根据影响力等级查询客户
     */
    List<Customer> findByInfluenceLevel(Customer.InfluenceLevel influenceLevel);

    /**
     * 根据决策权力查询客户
     */
    List<Customer> findByDecisionPower(Customer.DecisionPower decisionPower);

    /**
     * 根据客户状态查询客户
     */
    List<Customer> findByStatus(Customer.CustomerStatus status);

    /**
     * 根据地区查询客户
     */
    List<Customer> findByRegion(String region);

    /**
     * 根据邮箱查询客户
     */
    Optional<Customer> findByEmail(String email);

    /**
     * 根据手机号查询客户
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * 根据微信号查询客户
     */
    Optional<Customer> findByWechat(String wechat);

    /**
     * 根据学校ID查询客户
     */
    @Query("SELECT c FROM Customer c WHERE c.department.school.id = :schoolId")
    List<Customer> findBySchoolId(@Param("schoolId") Long schoolId);

    // ==================== 生日相关查询 ====================

    /**
     * 根据生日月份查询客户
     */
    @Query("SELECT c FROM Customer c WHERE MONTH(c.birthday) = :month")
    List<Customer> findCustomersByBirthdayMonth(@Param("month") int month);

    /**
     * 根据生日日期查询客户
     */
    @Query("SELECT c FROM Customer c WHERE DAY(c.birthday) = :day AND MONTH(c.birthday) = :month")
    List<Customer> findCustomersByBirthdayDayAndMonth(@Param("day") int day, @Param("month") int month);

    // ==================== 统计查询方法 ====================

    /**
     * 根据影响力等级统计客户数量
     */
    long countByInfluenceLevel(Customer.InfluenceLevel influenceLevel);

    /**
     * 根据决策权力统计客户数量
     */
    long countByDecisionPower(Customer.DecisionPower decisionPower);

    /**
     * 根据客户状态统计客户数量
     */
    long countByStatus(Customer.CustomerStatus status);

    /**
     * 根据创建时间之后统计客户数量
     */
    long countByCreatedAtAfter(LocalDateTime startTime);

    /**
     * 根据创建时间范围统计客户数量
     */
    long countByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据创建人ID统计客户数量
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdBy.id = :createdById")
    long countByCreatedById(@Param("createdById") Long createdById);

    /**
     * 根据销售人员ID和创建时间之后统计客户数量
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdBy.id = :salesId AND c.createdAt >= :startTime")
    long countBySalesIdAndCreatedAtAfter(@Param("salesId") Long salesId, @Param("startTime") LocalDateTime startTime);

    /**
     * 根据销售人员ID和创建时间范围统计客户数量
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdBy.id = :salesId AND c.createdAt BETWEEN :startTime AND :endTime")
    long countBySalesIdAndCreatedAtBetween(@Param("salesId") Long salesId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据创建人ID和创建时间范围统计客户数量
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdBy.id = :createdById AND c.createdAt BETWEEN :startTime AND :endTime")
    long countByCreatedByIdAndCreatedAtBetween(@Param("createdById") Long createdById, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // ==================== 时间范围查询 ====================

    /**
     * 根据创建时间范围查询客户
     */
    List<Customer> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据创建时间范围查询客户并排序
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt BETWEEN :startTime AND :endTime ORDER BY c.createdAt DESC")
    List<Customer> findByCreatedAtBetweenOrderByCreatedAtDesc(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据创建人和创建时间范围查询客户
     */
    @Query("SELECT c FROM Customer c WHERE c.createdBy.id = :createdById AND c.createdAt BETWEEN :startTime AND :endTime")
    List<Customer> findByCreatedByAndCreatedAtBetween(@Param("createdById") Long createdById, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // ==================== 分页查询方法 ====================

    /**
     * 多条件分页查询客户（管理员使用）
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
            "AND (:status IS NULL OR c.status = :status) " +
            "ORDER BY c.createdAt DESC")
    Page<Customer> findCustomersWithFilters(@Param("keyword") String keyword,
                                            @Param("departmentId") Long departmentId,
                                            @Param("schoolId") Long schoolId,
                                            @Param("schoolCity") String schoolCity,
                                            @Param("influenceLevel") Customer.InfluenceLevel influenceLevel,
                                            @Param("status") Customer.CustomerStatus status,
                                            Pageable pageable);

    /**
     * 多条件分页查询客户（销售人员使用）
     */
    @Query("SELECT c FROM Customer c " +
            "LEFT JOIN c.department d " +
            "LEFT JOIN d.school s " +
            "WHERE c.createdBy.id = :salesId " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(c.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:departmentId IS NULL OR c.department.id = :departmentId) " +
            "AND (:schoolId IS NULL OR d.school.id = :schoolId) " +
            "AND (:schoolCity IS NULL OR :schoolCity = '' OR s.city = :schoolCity) " +
            "AND (:influenceLevel IS NULL OR c.influenceLevel = :influenceLevel) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "ORDER BY c.createdAt DESC")
    Page<Customer> findCustomersWithFiltersBySales(@Param("salesId") Long salesId,
                                                   @Param("keyword") String keyword,
                                                   @Param("departmentId") Long departmentId,
                                                   @Param("schoolId") Long schoolId,
                                                   @Param("schoolCity") String schoolCity,
                                                   @Param("influenceLevel") Customer.InfluenceLevel influenceLevel,
                                                   @Param("status") Customer.CustomerStatus status,
                                                   Pageable pageable);

    // ==================== 统计分析方法 ====================

    /**
     * 获取学校客户分布统计
     */
    @Query("SELECT s.name as schoolName, COUNT(c) as customerCount " +
            "FROM Customer c " +
            "JOIN c.department d " +
            "JOIN d.school s " +
            "GROUP BY s.id, s.name " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> findSchoolCustomerDistribution();

    /**
     * 获取院系客户分布统计
     */
    @Query("SELECT d.name as departmentName, COUNT(c) as customerCount " +
            "FROM Customer c " +
            "JOIN c.department d " +
            "GROUP BY d.id, d.name " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> findDepartmentCustomerDistribution();

    /**
     * 获取城市客户分布统计
     */
    @Query("SELECT s.city as cityName, COUNT(c) as customerCount " +
            "FROM Customer c " +
            "JOIN c.department d " +
            "JOIN d.school s " +
            "GROUP BY s.city " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> findCityCustomerDistribution();

    /**
     * 获取省份客户分布统计
     */
    @Query("SELECT s.province as provinceName, COUNT(c) as customerCount " +
            "FROM Customer c " +
            "JOIN c.department d " +
            "JOIN d.school s " +
            "GROUP BY s.province " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> findProvinceCustomerDistribution();

    /**
     * 获取影响力等级分布统计
     */
    @Query("SELECT c.influenceLevel as influenceLevel, COUNT(c) as customerCount " +
            "FROM Customer c " +
            "GROUP BY c.influenceLevel " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> findInfluenceLevelDistribution();

    /**
     * 获取决策权力分布统计
     */
    @Query("SELECT c.decisionPower as decisionPower, COUNT(c) as customerCount " +
            "FROM Customer c " +
            "GROUP BY c.decisionPower " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> findDecisionPowerDistribution();

    /**
     * 获取客户状态分布统计
     */
    @Query("SELECT c.status as status, COUNT(c) as customerCount " +
            "FROM Customer c " +
            "GROUP BY c.status " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> findCustomerStatusDistribution();

    /**
     * 获取销售人员客户数量排名
     */
    @Query("SELECT u.realName as salesName, COUNT(c) as customerCount " +
            "FROM Customer c " +
            "JOIN c.createdBy u " +
            "GROUP BY u.id, u.realName " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> findSalesCustomerCountRanking();

    /**
     * 根据创建时间范围获取客户数量按日期统计
     */
    @Query("SELECT DATE(c.createdAt) as date, COUNT(c) as customerCount " +
            "FROM Customer c " +
            "WHERE c.createdAt BETWEEN :startTime AND :endTime " +
            "GROUP BY DATE(c.createdAt) " +
            "ORDER BY DATE(c.createdAt)")
    List<Object[]> findCustomerCountByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据销售人员和创建时间范围获取客户数量按日期统计
     */
    @Query("SELECT DATE(c.createdAt) as date, COUNT(c) as customerCount " +
            "FROM Customer c " +
            "WHERE c.createdBy.id = :salesId " +
            "AND c.createdAt BETWEEN :startTime AND :endTime " +
            "GROUP BY DATE(c.createdAt) " +
            "ORDER BY DATE(c.createdAt)")
    List<Object[]> findCustomerCountByDateRangeAndSales(@Param("salesId") Long salesId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // ==================== 数据验证方法 ====================

    /**
     * 检查客户姓名是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查微信号是否存在
     */
    boolean existsByWechat(String wechat);

    /**
     * 检查除指定ID外的客户是否存在相同手机号
     */
    boolean existsByPhoneAndIdNot(String phone, Long id);

    /**
     * 检查除指定ID外的客户是否存在相同邮箱
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * 检查除指定ID外的客户是否存在相同微信号
     */
    boolean existsByWechatAndIdNot(String wechat, Long id);

    // ==================== 兼容性方法 ====================

    /**
     * 根据创建人查询客户（兼容老版本）
     */
    @Query("SELECT c FROM Customer c WHERE c.createdBy.id = :createdById")
    List<Customer> findByCreateBy(@Param("createdById") Long createdById);

    /**
     * 根据创建人和创建时间范围查询客户（兼容老版本）
     */
    @Query("SELECT c FROM Customer c WHERE c.createdBy.id = :createdById AND c.createdAt BETWEEN :startTime AND :endTime")
    List<Customer> findByCreateByAndCreateTimeBetween(@Param("createdById") Long createdById, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据创建时间范围查询客户（兼容老版本）
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt BETWEEN :startTime AND :endTime")
    List<Customer> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

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

    List<Customer> findTop10ByOrderByCreatedAtDesc();

    Object countByCreatedByIdAndInfluenceLevel(Long salesId, Customer.InfluenceLevel influenceLevel);

    long countByDepartmentId(Long id);
}
