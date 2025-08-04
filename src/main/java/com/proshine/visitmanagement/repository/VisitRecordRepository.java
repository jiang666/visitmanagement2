package com.proshine.visitmanagement.repository;

import com.proshine.visitmanagement.entity.VisitRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 拜访记录数据访问层接口
 *
 * @author System
 * @since 2024-01-01
 */
@Repository
public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据客户ID查找拜访记录
     */
    List<VisitRecord> findByCustomerIdOrderByVisitDateDesc(Long customerId);

    /**
     * 根据客户ID和销售人员ID查找拜访记录
     */
    List<VisitRecord> findByCustomerIdAndSalesIdOrderByVisitDateDesc(Long customerId, Long salesId);

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

    // ==================== 统计查询方法 ====================

    /**
     * 根据销售人员ID统计拜访记录数
     */
    Long countBySalesId(Long salesId);

    /**
     * 根据销售人员ID和拜访日期范围统计记录数
     */
    Long countBySalesIdAndVisitDateBetween(Long salesId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据拜访日期范围统计记录数
     */
    @Query("SELECT COUNT(vr) FROM VisitRecord vr WHERE vr.visitDate BETWEEN :startDate AND :endDate")
    long countByVisitDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据销售人员ID、状态和拜访日期范围统计记录数
     */
    long countBySalesIdAndStatusAndVisitDateBetween(Long salesId, VisitRecord.VisitStatus status, LocalDate startDate, LocalDate endDate);

    /**
     * 根据状态和拜访日期范围统计记录数
     */
    long countByStatusAndVisitDateBetween(VisitRecord.VisitStatus status, LocalDate startDate, LocalDate endDate);

    /**
     * 根据销售人员ID、意向等级和拜访日期范围统计记录数
     */
    long countBySalesIdAndIntentLevelAndVisitDateBetween(Long salesId, VisitRecord.IntentLevel intentLevel, LocalDate startDate, LocalDate endDate);

    /**
     * 根据意向等级和拜访日期范围统计记录数
     */
    long countByIntentLevelAndVisitDateBetween(VisitRecord.IntentLevel intentLevel, LocalDate startDate, LocalDate endDate);

    /**
     * 根据客户ID统计拜访记录数
     */
    long countByCustomerId(Long id);

    /**
     * 根据销售人员ID和拜访日期之后统计记录数
     */
    long countBySalesIdAndVisitDateAfter(Long salesId, LocalDate weekAgo);
}