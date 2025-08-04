package com.proshine.visitmanagement.repository;

import com.proshine.visitmanagement.entity.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 学校数据访问层
 *
 * @author System
 * @since 2024-01-01
 */
@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 检查学校名称是否存在
     *
     * @param name 学校名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据省份查询学校列表
     *
     * @param province 省份
     * @return 学校列表
     */
    List<School> findByProvince(String province);

    /**
     * 根据城市查询学校列表
     *
     * @param city 城市
     * @return 学校列表
     */
    List<School> findByCity(String city);

    /**
     * 根据学校类型查询学校列表
     *
     * @param schoolType 学校类型
     * @return 学校列表
     */
    @Query("SELECT s FROM School s WHERE s.schoolTypesString LIKE CONCAT('%', :schoolType, '%')")
    List<School> findBySchoolType(@Param("schoolType") School.SchoolType schoolType);

    /**
     * 根据学校名称模糊查询（忽略大小写）
     *
     * @param name 学校名称关键词
     * @return 学校列表
     */
    List<School> findByNameContainingIgnoreCase(String name);

    // ==================== 统计查询方法 ====================

    /**
     * 根据学校类型统计数量
     *
     * @param schoolType 学校类型
     * @return 数量
     */
    @Query("SELECT COUNT(s) FROM School s WHERE s.schoolTypesString LIKE CONCAT('%', :schoolType, '%')")
    long countBySchoolType(@Param("schoolType") School.SchoolType schoolType);

    // ==================== 复杂查询方法 ====================

    /**
     * 多条件分页查询学校
     *
     * @param keyword 关键词（学校名称或地址）
     * @param province 省份
     * @param city 城市
     * @param schoolTypes 学校类型列表
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT s FROM School s WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " s.name LIKE CONCAT('%', :keyword, '%') OR " +
            " s.address LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:province IS NULL OR :province = '' OR s.province = :province) AND " +
            "(:city IS NULL OR :city = '' OR s.city = :city) AND " +
            "(:schoolTypesStr IS NULL OR :schoolTypesStr = '' OR " +
            " s.schoolTypesString LIKE CONCAT('%', :schoolTypesStr, '%'))")
    Page<School> findSchoolsWithFilters(@Param("keyword") String keyword,
                                        @Param("province") String province,
                                        @Param("city") String city,
                                        @Param("schoolTypesStr") String schoolTypesStr,
                                        Pageable pageable);

    /**
     * 多条件查询学校列表（用于导出）
     *
     * @param keyword 关键词
     * @param province 省份
     * @param city 城市
     * @param schoolTypes 学校类型列表
     * @return 学校列表
     */
    @Query("SELECT s FROM School s WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " s.name LIKE CONCAT('%', :keyword, '%') OR " +
            " s.address LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:province IS NULL OR :province = '' OR s.province = :province) AND " +
            "(:city IS NULL OR :city = '' OR s.city = :city) AND " +
            "(:schoolTypesStr IS NULL OR :schoolTypesStr = '' OR " +
            " s.schoolTypesString LIKE CONCAT('%', :schoolTypesStr, '%')) " +
            "ORDER BY s.province ASC, s.city ASC, s.name ASC")
    List<School> findSchoolsForExport(@Param("keyword") String keyword,
                                      @Param("province") String province,
                                      @Param("city") String city,
                                      @Param("schoolTypesStr") String schoolTypesStr);
}