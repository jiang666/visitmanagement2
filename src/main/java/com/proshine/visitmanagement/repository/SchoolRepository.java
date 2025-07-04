package com.proshine.visitmanagement.repository;

import com.proshine.visitmanagement.entity.School;
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
 * 学校数据访问层
 *
 * @author System
 * @since 2024-01-01
 */
@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据学校名称查询（精确匹配）
     *
     * @param name 学校名称
     * @return 学校信息
     */
    Optional<School> findByName(String name);

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
     * 根据省份和城市查询学校列表
     *
     * @param province 省份
     * @param city 城市
     * @return 学校列表
     */
    List<School> findByProvinceAndCity(String province, String city);

    /**
     * 根据学校类型查询学校列表
     *
     * @param schoolType 学校类型
     * @return 学校列表
     */
    List<School> findBySchoolType(School.SchoolType schoolType);

    /**
     * 根据学校名称模糊查询（忽略大小写）
     *
     * @param name 学校名称关键词
     * @return 学校列表
     */
    List<School> findByNameContainingIgnoreCase(String name);

    /**
     * 根据学校名称模糊查询
     *
     * @param name 学校名称关键词
     * @return 学校列表
     */
    List<School> findByNameContaining(String name);

    /**
     * 根据地址模糊查询
     *
     * @param address 地址关键词
     * @return 学校列表
     */
    List<School> findByAddressContaining(String address);

    // ==================== 统计查询方法 ====================

    /**
     * 根据学校类型统计数量
     *
     * @param schoolType 学校类型
     * @return 数量
     */
    long countBySchoolType(School.SchoolType schoolType);

    /**
     * 根据省份统计学校数量
     *
     * @param province 省份
     * @return 数量
     */
    long countByProvince(String province);

    /**
     * 根据城市统计学校数量
     *
     * @param city 城市
     * @return 数量
     */
    long countByCity(String city);

    /**
     * 统计指定时间后创建的学校数量
     *
     * @param createdAfter 创建时间
     * @return 数量
     */
    long countByCreatedAtAfter(LocalDateTime createdAfter);

    // ==================== 复杂查询方法 ====================

    /**
     * 多条件分页查询学校
     *
     * @param keyword 关键词（学校名称或地址）
     * @param province 省份
     * @param city 城市
     * @param schoolType 学校类型
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT s FROM School s WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:province IS NULL OR :province = '' OR s.province = :province) AND " +
            "(:city IS NULL OR :city = '' OR s.city = :city) AND " +
            "(:schoolType IS NULL OR s.schoolType = :schoolType) " +
            "ORDER BY s.name ASC")
    Page<School> findSchoolsWithFilters(@Param("keyword") String keyword,
                                        @Param("province") String province,
                                        @Param("city") String city,
                                        @Param("schoolType") School.SchoolType schoolType,
                                        Pageable pageable);

    /**
     * 多条件查询学校列表（用于导出）
     *
     * @param keyword 关键词
     * @param province 省份
     * @param city 城市
     * @param schoolType 学校类型
     * @return 学校列表
     */
    @Query("SELECT s FROM School s WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:province IS NULL OR :province = '' OR s.province = :province) AND " +
            "(:city IS NULL OR :city = '' OR s.city = :city) AND " +
            "(:schoolType IS NULL OR s.schoolType = :schoolType) " +
            "ORDER BY s.province ASC, s.city ASC, s.name ASC")
    List<School> findSchoolsForExport(@Param("keyword") String keyword,
                                      @Param("province") String province,
                                      @Param("city") String city,
                                      @Param("schoolType") School.SchoolType schoolType);

    /**
     * 根据关键词搜索学校（支持多字段搜索）
     *
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT s FROM School s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.province) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.city) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY s.name ASC")
    Page<School> searchSchools(@Param("keyword") String keyword, Pageable pageable);

    // ==================== 地理信息查询方法 ====================

    /**
     * 查询指定省份的所有城市
     *
     * @param province 省份
     * @return 城市列表
     */
    @Query("SELECT DISTINCT s.city FROM School s WHERE s.province = :province AND s.city IS NOT NULL ORDER BY s.city")
    List<String> findCitiesByProvince(@Param("province") String province);

    /**
     * 查询所有省份
     *
     * @return 省份列表
     */
    @Query("SELECT DISTINCT s.province FROM School s WHERE s.province IS NOT NULL ORDER BY s.province")
    List<String> findAllProvinces();

    /**
     * 查询所有城市
     *
     * @return 城市列表
     */
    @Query("SELECT DISTINCT s.city FROM School s WHERE s.city IS NOT NULL ORDER BY s.city")
    List<String> findAllCities();

    // ==================== 统计分析查询方法 ====================

    /**
     * 查询指定类型的学校，按省份分组统计
     *
     * @param schoolType 学校类型
     * @return 省份统计结果（省份，数量）
     */
    @Query("SELECT s.province, COUNT(s) FROM School s WHERE s.schoolType = :schoolType " +
            "GROUP BY s.province ORDER BY COUNT(s) DESC")
    List<Object[]> countBySchoolTypeGroupByProvince(@Param("schoolType") School.SchoolType schoolType);

    /**
     * 查询每个省份的学校类型分布
     *
     * @return 省份-类型统计结果（省份，学校类型，数量）
     */
    @Query("SELECT s.province, s.schoolType, COUNT(s) FROM School s " +
            "GROUP BY s.province, s.schoolType ORDER BY s.province, s.schoolType")
    List<Object[]> countByProvinceAndSchoolType();

    /**
     * 查询每个城市的学校数量（TOP N）
     *
     * @param limit 限制数量
     * @return 城市统计结果（城市，数量）
     */
    @Query(value = "SELECT city, COUNT(*) as count FROM schools " +
            "WHERE city IS NOT NULL AND city != '' " +
            "GROUP BY city ORDER BY count DESC LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopCitiesBySchoolCount(@Param("limit") int limit);

    /**
     * 查询每个省份的学校数量（TOP N）
     *
     * @param limit 限制数量
     * @return 省份统计结果（省份，数量）
     */
    @Query(value = "SELECT province, COUNT(*) as count FROM schools " +
            "WHERE province IS NOT NULL AND province != '' " +
            "GROUP BY province ORDER BY count DESC LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopProvincesBySchoolCount(@Param("limit") int limit);

    /**
     * 统计各学校类型的数量
     *
     * @return 学校类型统计结果（学校类型，数量）
     */
    @Query("SELECT s.schoolType, COUNT(s) FROM School s GROUP BY s.schoolType")
    List<Object[]> countBySchoolType();

    // ==================== 特殊查询方法 ====================

    /**
     * 查询有网站的学校列表
     *
     * @return 学校列表
     */
    @Query("SELECT s FROM School s WHERE s.website IS NOT NULL AND s.website != '' ORDER BY s.name")
    List<School> findSchoolsWithWebsite();

    /**
     * 查询有联系电话的学校列表
     *
     * @return 学校列表
     */
    @Query("SELECT s FROM School s WHERE s.contactPhone IS NOT NULL AND s.contactPhone != '' ORDER BY s.name")
    List<School> findSchoolsWithContactPhone();

    /**
     * 查询最近创建的学校
     *
     * @param pageable 分页参数
     * @return 学校列表
     */
    @Query("SELECT s FROM School s ORDER BY s.createdAt DESC")
    List<School> findRecentSchools(Pageable pageable);

    /**
     * 根据创建时间范围查询学校
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 学校列表
     */
    List<School> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询985/211类型的学校
     *
     * @return 985/211学校列表
     */
    @Query("SELECT s FROM School s WHERE s.schoolType IN " +
            "(com.proshine.visitmanagement.entity.School$SchoolType.PROJECT_985, " +
            "com.proshine.visitmanagement.entity.School$SchoolType.PROJECT_211) " +
            "ORDER BY s.schoolType, s.name")
    List<School> findEliteSchools();

    /**
     * 查询双一流学校
     *
     * @return 双一流学校列表
     */
    @Query("SELECT s FROM School s WHERE s.schoolType = com.proshine.visitmanagement.entity.School$SchoolType.DOUBLE_FIRST_CLASS ORDER BY s.name")
    List<School> findDoubleFirstClassSchools();

    /**
     * 查询普通高校
     *
     * @return 普通高校列表
     */
    @Query("SELECT s FROM School s WHERE s.schoolType = com.proshine.visitmanagement.entity.School$SchoolType.REGULAR ORDER BY s.name")
    List<School> findRegularSchools();

    // ==================== 验证查询方法 ====================

    /**
     * 检查学校名称是否存在（排除指定ID）
     *
     * @param name 学校名称
     * @param excludeId 排除的学校ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(s) > 0 FROM School s WHERE s.name = :name AND s.id != :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 查询指定省份城市是否有学校
     *
     * @param province 省份
     * @param city 城市
     * @return 是否存在
     */
    boolean existsByProvinceAndCity(String province, String city);

    /**
     * 查询指定类型在指定省份是否有学校
     *
     * @param schoolType 学校类型
     * @param province 省份
     * @return 是否存在
     */
    boolean existsBySchoolTypeAndProvince(School.SchoolType schoolType, String province);
}