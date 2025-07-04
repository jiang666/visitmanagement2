package com.proshine.visitmanagement.repository;

import com.proshine.visitmanagement.entity.Department;
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
 * 院系数据访问层
 *
 * @author System
 * @since 2024-01-01
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据学校ID查找院系
     *
     * @param schoolId 学校ID
     * @return 院系列表
     */
    List<Department> findBySchoolId(Long schoolId);

    /**
     * 根据院系名称查找（精确匹配）
     *
     * @param name 院系名称
     * @return 院系信息
     */
    Optional<Department> findByName(String name);

    /**
     * 根据学校ID和院系名称查找（精确匹配）
     *
     * @param schoolId 学校ID
     * @param name 院系名称
     * @return 院系信息
     */
    Optional<Department> findBySchoolIdAndName(Long schoolId, String name);

    /**
     * 根据院系名称模糊查找
     *
     * @param name 院系名称关键词
     * @return 院系列表
     */
    List<Department> findByNameContaining(String name);

    /**
     * 根据学校ID和名称模糊查找院系
     *
     * @param schoolId 学校ID
     * @param name 院系名称关键词
     * @return 院系列表
     */
    List<Department> findBySchoolIdAndNameContaining(Long schoolId, String name);

    /**
     * 根据联系电话查找院系
     *
     * @param contactPhone 联系电话
     * @return 院系信息
     */
    Optional<Department> findByContactPhone(String contactPhone);

    // ==================== 统计查询方法 ====================

    /**
     * 根据学校ID统计院系数量
     *
     * @param schoolId 学校ID
     * @return 院系数量
     */
    long countBySchoolId(Long schoolId);

    /**
     * 检查院系名称在指定学校中是否存在
     *
     * @param schoolId 学校ID
     * @param name 院系名称
     * @return 是否存在
     */
    boolean existsBySchoolIdAndName(Long schoolId, String name);

    /**
     * 检查院系名称是否存在（排除指定ID）
     *
     * @param schoolId 学校ID
     * @param name 院系名称
     * @param excludeId 排除的院系ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(d) > 0 FROM Department d WHERE d.school.id = :schoolId AND d.name = :name AND d.id != :excludeId")
    boolean existsBySchoolIdAndNameAndIdNot(@Param("schoolId") Long schoolId,
                                            @Param("name") String name,
                                            @Param("excludeId") Long excludeId);

    // ==================== 关联查询方法 ====================

    /**
     * 查找有客户的院系
     *
     * @return 有客户的院系列表
     */
    @Query("SELECT DISTINCT d FROM Department d WHERE EXISTS (SELECT c FROM Customer c WHERE c.department = d) ORDER BY d.name")
    List<Department> findDepartmentsWithCustomers();

    /**
     * 根据学校ID查找有客户的院系
     *
     * @param schoolId 学校ID
     * @return 有客户的院系列表
     */
    @Query("SELECT DISTINCT d FROM Department d WHERE d.school.id = :schoolId AND EXISTS (SELECT c FROM Customer c WHERE c.department = d) ORDER BY d.name")
    List<Department> findDepartmentsWithCustomersBySchoolId(@Param("schoolId") Long schoolId);

    /**
     * 查找没有客户的院系
     *
     * @return 没有客户的院系列表
     */
    @Query("SELECT d FROM Department d WHERE NOT EXISTS (SELECT c FROM Customer c WHERE c.department = d) ORDER BY d.name")
    List<Department> findDepartmentsWithoutCustomers();

    /**
     * 根据学校ID查找没有客户的院系
     *
     * @param schoolId 学校ID
     * @return 没有客户的院系列表
     */
    @Query("SELECT d FROM Department d WHERE d.school.id = :schoolId AND NOT EXISTS (SELECT c FROM Customer c WHERE c.department = d) ORDER BY d.name")
    List<Department> findDepartmentsWithoutCustomersBySchoolId(@Param("schoolId") Long schoolId);

    // ==================== 分页查询方法 ====================

    /**
     * 分页查询院系（支持多条件筛选）
     *
     * @param keyword 关键词（院系名称、学校名称）
     * @param schoolId 学校ID
     * @param hasCustomers 是否有客户
     * @param pageable 分页参数
     * @return 分页院系列表
     */
    @Query("SELECT d FROM Department d " +
            "LEFT JOIN d.school s " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "d.name LIKE %:keyword% OR s.name LIKE %:keyword%) " +
            "AND (:schoolId IS NULL OR d.school.id = :schoolId) " +
            "AND (:hasCustomers IS NULL OR " +
            "(:hasCustomers = true AND EXISTS (SELECT c FROM Customer c WHERE c.department = d)) OR " +
            "(:hasCustomers = false AND NOT EXISTS (SELECT c FROM Customer c WHERE c.department = d))) " +
            "ORDER BY d.createdAt DESC")
    Page<Department> findDepartmentsWithFilters(@Param("keyword") String keyword,
                                                @Param("schoolId") Long schoolId,
                                                @Param("hasCustomers") Boolean hasCustomers,
                                                Pageable pageable);

    /**
     * 根据多个条件查找院系
     *
     * @param schoolId 学校ID
     * @param name 院系名称关键词
     * @param contactPhone 联系电话
     * @return 院系列表
     */
    @Query("SELECT d FROM Department d WHERE " +
            "(:schoolId IS NULL OR d.school.id = :schoolId) " +
            "AND (:name IS NULL OR :name = '' OR d.name LIKE %:name%) " +
            "AND (:contactPhone IS NULL OR :contactPhone = '' OR d.contactPhone = :contactPhone) " +
            "ORDER BY d.name")
    List<Department> findByMultipleFilters(@Param("schoolId") Long schoolId,
                                           @Param("name") String name,
                                           @Param("contactPhone") String contactPhone);

    // ==================== 统计分析方法 ====================

    /**
     * 统计各学校的院系数量
     *
     * @return 学校ID和院系数量的统计（学校ID，院系数量）
     */
    @Query("SELECT d.school.id, COUNT(d) FROM Department d GROUP BY d.school.id ORDER BY COUNT(d) DESC")
    List<Object[]> countDepartmentsBySchool();

    /**
     * 获取院系客户数量统计
     *
     * @return 院系ID和客户数量的统计（院系ID，客户数量）
     */
    @Query("SELECT d.id, COUNT(c) FROM Department d LEFT JOIN d.customers c GROUP BY d.id ORDER BY COUNT(c) DESC")
    List<Object[]> countCustomersByDepartment();

    /**
     * 获取指定学校的院系客户数量统计
     *
     * @param schoolId 学校ID
     * @return 院系ID和客户数量的统计（院系ID，客户数量）
     */
    @Query("SELECT d.id, COUNT(c) FROM Department d LEFT JOIN d.customers c WHERE d.school.id = :schoolId GROUP BY d.id ORDER BY COUNT(c) DESC")
    List<Object[]> countCustomersByDepartmentAndSchool(@Param("schoolId") Long schoolId);

    /**
     * 统计有客户的院系数量
     *
     * @return 有客户的院系数量
     */
    @Query("SELECT COUNT(DISTINCT d) FROM Department d WHERE EXISTS (SELECT c FROM Customer c WHERE c.department = d)")
    long countDepartmentsWithCustomers();

    /**
     * 统计没有客户的院系数量
     *
     * @return 没有客户的院系数量
     */
    @Query("SELECT COUNT(d) FROM Department d WHERE NOT EXISTS (SELECT c FROM Customer c WHERE c.department = d)")
    long countDepartmentsWithoutCustomers();

    // ==================== 排序查询方法 ====================

    /**
     * 根据学校ID查找院系（按名称排序）
     *
     * @param schoolId 学校ID
     * @return 院系列表（按名称排序）
     */
    List<Department> findBySchoolIdOrderByName(Long schoolId);

    /**
     * 根据学校ID查找院系（按创建时间倒序）
     *
     * @param schoolId 学校ID
     * @return 院系列表（按创建时间倒序）
     */
    List<Department> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);

    /**
     * 查找最近创建的院系
     *
     * @param pageable 分页参数
     * @return 最近创建的院系列表
     */
    @Query("SELECT d FROM Department d ORDER BY d.createdAt DESC")
    List<Department> findRecentDepartments(Pageable pageable);

    /**
     * 查找指定学校中客户数量最多的院系
     *
     * @param schoolId 学校ID
     * @param pageable 分页参数
     * @return 院系列表（按客户数量倒序）
     */
    @Query("SELECT d FROM Department d " +
            "LEFT JOIN d.customers c " +
            "WHERE d.school.id = :schoolId " +
            "GROUP BY d.id, d.name, d.school.id, d.contactPhone, d.address, d.description, d.createdAt, d.updatedAt, d.deletedAt " +
            "ORDER BY COUNT(c) DESC")
    List<Department> findTopDepartmentsByCustomerCount(@Param("schoolId") Long schoolId, Pageable pageable);

    // ==================== 时间查询方法 ====================

    /**
     * 查找指定时间段内创建的院系
     *
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 院系列表
     */
    @Query("SELECT d FROM Department d WHERE d.createdAt BETWEEN :startDate AND :endDate ORDER BY d.createdAt DESC")
    List<Department> findDepartmentsCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 根据创建时间范围查询院系
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 院系列表
     */
    List<Department> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定时间后创建的院系数量
     *
     * @param createdAfter 创建时间
     * @return 院系数量
     */
    long countByCreatedAtAfter(LocalDateTime createdAfter);

    // ==================== 验证查询方法 ====================

    /**
     * 删除前检查：查找有关联数据的院系
     *
     * @param departmentId 院系ID
     * @return 是否有关联数据
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.department.id = :departmentId")
    boolean hasRelatedData(@Param("departmentId") Long departmentId);

    /**
     * 检查院系是否有联系电话
     *
     * @param departmentId 院系ID
     * @return 是否有联系电话
     */
    @Query("SELECT COUNT(d) > 0 FROM Department d WHERE d.id = :departmentId AND d.contactPhone IS NOT NULL AND d.contactPhone != ''")
    boolean hasContactPhone(@Param("departmentId") Long departmentId);

    /**
     * 检查学校是否有院系
     *
     * @param schoolId 学校ID
     * @return 是否有院系
     */
    boolean existsBySchoolId(Long schoolId);

    // ==================== 特殊查询方法 ====================

    /**
     * 查找有联系电话的院系
     *
     * @return 有联系电话的院系列表
     */
    @Query("SELECT d FROM Department d WHERE d.contactPhone IS NOT NULL AND d.contactPhone != '' ORDER BY d.name")
    List<Department> findDepartmentsWithContactPhone();

    /**
     * 查找没有联系电话的院系
     *
     * @return 没有联系电话的院系列表
     */
    @Query("SELECT d FROM Department d WHERE d.contactPhone IS NULL OR d.contactPhone = '' ORDER BY d.name")
    List<Department> findDepartmentsWithoutContactPhone();

    /**
     * 查找有描述的院系
     *
     * @return 有描述的院系列表
     */
    @Query("SELECT d FROM Department d WHERE d.description IS NOT NULL AND d.description != '' ORDER BY d.name")
    List<Department> findDepartmentsWithDescription();

    /**
     * 根据学校类型查找院系
     *
     * @param schoolType 学校类型
     * @return 院系列表
     */
    @Query("SELECT d FROM Department d LEFT JOIN d.school s WHERE s.schoolType = :schoolType ORDER BY s.name, d.name")
    List<Department> findBySchoolType(@Param("schoolType") com.proshine.visitmanagement.entity.School.SchoolType schoolType);

    /**
     * 根据省份查找院系
     *
     * @param province 省份
     * @return 院系列表
     */
    @Query("SELECT d FROM Department d LEFT JOIN d.school s WHERE s.province = :province ORDER BY s.name, d.name")
    List<Department> findByProvince(@Param("province") String province);

    /**
     * 根据城市查找院系
     *
     * @param city 城市
     * @return 院系列表
     */
    @Query("SELECT d FROM Department d LEFT JOIN d.school s WHERE s.city = :city ORDER BY s.name, d.name")
    List<Department> findByCity(@Param("city") String city);
}