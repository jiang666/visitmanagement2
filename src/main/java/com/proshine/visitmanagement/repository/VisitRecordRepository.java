package com.proshine.visitmanagement.repository;

import com.proshine.visitmanagement.entity.VisitRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 拜访记录数据访问层接口
 *
 * @author System
 * @since 2024-01-01
 */
@Repository
public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {

    Long countBySalesId(Long salesId);

    Long countBySalesIdAndVisitDateBetween(Long salesId, LocalDate startDate, LocalDate endDate);

    // ==================== 基础查询方法 ====================

    /**
     * 根据客户ID查找拜访记录
     */
    List<VisitRecord> findByCustomerIdOrderByVisitDateDesc(Long customerId);

    /**
     * 根据销售人员ID查找拜访记录
     */
    List<VisitRecord> findBySalesIdOrderByVisitDateDesc(Long salesId);

    /**
     * 根据客户ID和销售人员ID查找拜访记录
     */
    List<VisitRecord> findByCustomerIdAndSalesIdOrderByVisitDateDesc(Long customerId, Long salesId);

    /**
     * 根据状态查找拜访记录
     */
    List<VisitRecord> findByStatus(VisitRecord.VisitStatus status);

    /**
     * 根据意向等级查找拜访记录
     */
    List<VisitRecord> findByIntentLevel(VisitRecord.IntentLevel intentLevel);

    /**
     * 根据拜访日期查找记录
     */
    @Query("SELECT vr FROM VisitRecord vr WHERE vr.visitDate = :visitDate")
    List<VisitRecord> findByVisitDate(@Param("visitDate") LocalDate visitDate);

    /**
     * 根据销售人员ID和拜访日期查找记录
     */
    @Query("SELECT vr FROM VisitRecord vr WHERE vr.sales.id = :salesId AND vr.visitDate = :visitDate")
    List<VisitRecord> findBySalesIdAndVisitDate(@Param("salesId") Long salesId, @Param("visitDate") LocalDate visitDate);

    /**
     * 根据拜访日期范围查找记录
     */
    @Query("SELECT vr FROM VisitRecord vr WHERE vr.visitDate BETWEEN :startDate AND :endDate ORDER BY vr.visitDate DESC")
    List<VisitRecord> findByVisitDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据销售人员ID和拜访日期范围查找记录
     */
    @Query("SELECT vr FROM VisitRecord vr WHERE vr.sales.id = :salesId AND vr.visitDate BETWEEN :startDate AND :endDate ORDER BY vr.visitDate DESC")
    List<VisitRecord> findBySalesIdAndVisitDateBetweenOrderByVisitDateDesc(@Param("salesId") Long salesId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // ==================== 统计查询方法 ====================

    /**
     * 根据拜访日期统计记录数
     */
    @Query("SELECT COUNT(vr) FROM VisitRecord vr WHERE vr.visitDate = :visitDate")
    long countByVisitDate(@Param("visitDate") LocalDate visitDate);

    /**
     * 根据销售人员ID和拜访日期统计记录数
     */
    @Query("SELECT COUNT(vr) FROM VisitRecord vr WHERE vr.sales.id = :salesId AND vr.visitDate = :visitDate")
    long countBySalesIdAndVisitDate(@Param("salesId") Long salesId, @Param("visitDate") LocalDate visitDate);

    /**
     * 根据拜访日期范围统计记录数
     */
    @Query("SELECT COUNT(vr) FROM VisitRecord vr WHERE vr.visitDate BETWEEN :startDate AND :endDate")
    long countByVisitDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // ==================== 分页查询方法 ====================

    /**
     * 多条件分页查询拜访记录
     */
    @Query("SELECT vr FROM VisitRecord vr " +
            "LEFT JOIN vr.customer c " +
            "LEFT JOIN vr.sales s " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "c.name LIKE %:keyword% OR vr.notes LIKE %:keyword% OR vr.businessItems LIKE %:keyword%) " +
            "AND (:salesId IS NULL OR vr.sales.id = :salesId) " +
            "AND (:customerId IS NULL OR vr.customer.id = :customerId) " +
            "AND (:startDate IS NULL OR vr.visitDate >= :startDate) " +
            "AND (:endDate IS NULL OR vr.visitDate <= :endDate) " +
            "AND (:status IS NULL OR vr.status = :status) " +
            "AND (:intentLevel IS NULL OR vr.intentLevel = :intentLevel) " +
            "ORDER BY vr.visitDate DESC, vr.createdAt DESC")
    Page<VisitRecord> findVisitRecordsWithFilters(@Param("keyword") String keyword,
                                                  @Param("salesId") Long salesId,
                                                  @Param("customerId") Long customerId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate,
                                                  @Param("status") VisitRecord.VisitStatus status,
                                                  @Param("intentLevel") VisitRecord.IntentLevel intentLevel,
                                                  Pageable pageable);

    /**
     * 查找最近的拜访记录
     */
    @Query("SELECT vr FROM VisitRecord vr ORDER BY vr.visitDate DESC, vr.createdAt DESC")
    List<VisitRecord> findRecentVisitRecords(Pageable pageable);

    /**
     * 根据销售人员查找最近的拜访记录
     */
    @Query("SELECT vr FROM VisitRecord vr WHERE vr.sales.id = :salesId ORDER BY vr.visitDate DESC, vr.createdAt DESC")
    List<VisitRecord> findRecentVisitsBySalesId(@Param("salesId") Long salesId, Pageable pageable);


    // ==================== DashBoard 相关统计方法 ====================

    /**
     * 根据创建者和拜访日期范围查找拜访记录
     */
    @Query("SELECT vr FROM VisitRecord vr WHERE vr.createdBy.id = :createdById AND vr.visitDate BETWEEN :startDate AND :endDate")
    List<VisitRecord> findByCreatedByAndVisitDateBetween(@Param("createdById") Long createdById, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据创建时间范围查找拜访记录
     */
    @Query("SELECT vr FROM VisitRecord vr WHERE vr.createdAt BETWEEN :startTime AND :endTime")
    List<VisitRecord> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据创建者和创建时间范围查找拜访记录
     */
    @Query("SELECT vr FROM VisitRecord vr WHERE vr.createdBy.id = :createdById AND vr.createdAt BETWEEN :startTime AND :endTime")
    List<VisitRecord> findByCreatedByAndCreatedAtBetween(@Param("createdById") Long createdById, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据创建者统计拜访记录数
     */
    @Query("SELECT COUNT(vr) FROM VisitRecord vr WHERE vr.createdBy.id = :createdById")
    long countByCreatedById(@Param("createdById") Long createdById);

    /**
     * 根据创建者和拜访日期统计拜访记录数
     */
    @Query("SELECT COUNT(vr) FROM VisitRecord vr WHERE vr.createdBy.id = :createdById AND vr.visitDate = :visitDate")
    long countByCreatedByAndVisitDate(@Param("createdById") Long createdById, @Param("visitDate") LocalDate visitDate);

    /**
     * 根据创建者和拜访日期范围统计拜访记录数
     */
    @Query("SELECT COUNT(vr) FROM VisitRecord vr WHERE vr.createdBy.id = :createdById AND vr.visitDate BETWEEN :startDate AND :endDate")
    long countByCreatedByAndVisitDateBetween(@Param("createdById") Long createdById, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // ==================== 兼容性方法 ====================

    /**
     * 根据创建者和拜访日期查找记录（兼容老版本）
     */
    @Query("SELECT vr FROM VisitRecord vr WHERE vr.createdBy.id = :createdById AND vr.visitDate = :visitDate")
    List<VisitRecord> findByCreateByAndVisitDate(@Param("createdById") Long createdById, @Param("visitDate") LocalDate visitDate);

    /**
     * 根据创建者和拜访日期范围查找记录（兼容老版本）
     */
    @Query("SELECT vr FROM VisitRecord vr WHERE vr.createdBy.id = :createdById AND vr.visitDate BETWEEN :startDate AND :endDate ORDER BY vr.visitDate DESC")
    List<VisitRecord> findByCreateByAndVisitDateBetween(@Param("createdById") Long createdById, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据创建者统计拜访记录数（兼容老版本）
     */
    @Query("SELECT COUNT(vr) FROM VisitRecord vr WHERE vr.createdBy.id = :createdById")
    long countByCreateBy(@Param("createdById") Long createdById);

    /**
     * 根据创建者和拜访日期统计拜访记录数（兼容老版本）
     */
    @Query("SELECT COUNT(vr) FROM VisitRecord vr WHERE vr.createdBy.id = :createdById AND vr.visitDate = :visitDate")
    long countByCreateByAndVisitDate(@Param("createdById") Long createdById, @Param("visitDate") LocalDate visitDate);

    // ==================== 高级查询方法 ====================

    /**
     * 获取拜访记录的统计分析数据
     */
    @Query("SELECT vr.status as status, COUNT(vr) as count " +
            "FROM VisitRecord vr " +
            "WHERE vr.visitDate BETWEEN :startDate AND :endDate " +
            "GROUP BY vr.status")
    List<Object[]> getVisitStatusStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取销售人员的拜访记录统计
     */
    @Query("SELECT s.realName as salesName, COUNT(vr) as visitCount " +
            "FROM VisitRecord vr " +
            "JOIN vr.sales s " +
            "WHERE vr.visitDate BETWEEN :startDate AND :endDate " +
            "GROUP BY s.id, s.realName " +
            "ORDER BY COUNT(vr) DESC")
    List<Object[]> getSalesVisitStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取客户拜访频次统计
     */
    @Query("SELECT c.name as customerName, COUNT(vr) as visitCount " +
            "FROM VisitRecord vr " +
            "JOIN vr.customer c " +
            "WHERE vr.visitDate BETWEEN :startDate AND :endDate " +
            "GROUP BY c.id, c.name " +
            "ORDER BY COUNT(vr) DESC")
    List<Object[]> getCustomerVisitFrequency(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取意向等级分布统计
     */
    @Query("SELECT vr.intentLevel as intentLevel, COUNT(vr) as count " +
            "FROM VisitRecord vr " +
            "WHERE vr.visitDate BETWEEN :startDate AND :endDate " +
            "GROUP BY vr.intentLevel")
    List<Object[]> getIntentLevelDistribution(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取拜访记录按月统计
     */
    @Query("SELECT YEAR(vr.visitDate) as year, MONTH(vr.visitDate) as month, COUNT(vr) as count " +
            "FROM VisitRecord vr " +
            "WHERE vr.visitDate BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(vr.visitDate), MONTH(vr.visitDate) " +
            "ORDER BY YEAR(vr.visitDate), MONTH(vr.visitDate)")
    List<Object[]> getMonthlyVisitStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取拜访记录按周统计
     */
    @Query("SELECT YEAR(vr.visitDate) as year, WEEK(vr.visitDate) as week, COUNT(vr) as count " +
            "FROM VisitRecord vr " +
            "WHERE vr.visitDate BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(vr.visitDate), WEEK(vr.visitDate) " +
            "ORDER BY YEAR(vr.visitDate), WEEK(vr.visitDate)")
    List<Object[]> getWeeklyVisitStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取拜访记录按日统计
     */
    @Query("SELECT vr.visitDate as date, COUNT(vr) as count " +
            "FROM VisitRecord vr " +
            "WHERE vr.visitDate BETWEEN :startDate AND :endDate " +
            "GROUP BY vr.visitDate " +
            "ORDER BY vr.visitDate")
    List<Object[]> getDailyVisitStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取销售人员的拜访记录按日统计
     */
    @Query("SELECT vr.visitDate as date, COUNT(vr) as count " +
            "FROM VisitRecord vr " +
            "WHERE vr.sales.id = :salesId " +
            "AND vr.visitDate BETWEEN :startDate AND :endDate " +
            "GROUP BY vr.visitDate " +
            "ORDER BY vr.visitDate")
    List<Object[]> getDailyVisitStatisticsBySales(@Param("salesId") Long salesId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    long countByCustomerId(Long id);

    long countBySalesIdAndVisitDateAfter(Long salesId, LocalDate weekAgo);

    long countBySalesIdAndStatusAndVisitDateBetween(Long salesId, VisitRecord.VisitStatus status, LocalDate startDate, LocalDate endDate);

    long countByStatusAndVisitDateBetween(VisitRecord.VisitStatus status, LocalDate startDate, LocalDate endDate);

    long countBySalesIdAndIntentLevelAndVisitDateBetween(Long salesId, VisitRecord.IntentLevel intentLevel, LocalDate startDate, LocalDate endDate);

    long countByIntentLevelAndVisitDateBetween(VisitRecord.IntentLevel intentLevel, LocalDate startDate, LocalDate endDate);
}