package com.proshine.visitmanagement.repository;

import com.proshine.visitmanagement.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
     * 根据联系电话查找院系
     *
     * @param contactPhone 联系电话
     * @return 院系信息
     */
    Optional<Department> findByContactPhone(String contactPhone);

    // ==================== 统计查询方法 ====================

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

    // ==================== 排序查询方法 ====================

    /**
     * 根据学校ID查找院系（按名称排序）
     *
     * @param schoolId 学校ID
     * @return 院系列表（按名称排序）
     */
    List<Department> findBySchoolIdOrderByName(Long schoolId);
}