package com.proshine.visitmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 拜访记录实体类
 *
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "visit_records", indexes = {
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_sales_id", columnList = "sales_id"),
        @Index(name = "idx_visit_date", columnList = "visit_date"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_intent_level", columnList = "intent_level"),
        @Index(name = "idx_created_by", columnList = "created_by"),
        @Index(name = "idx_composite", columnList = "sales_id,visit_date,status")
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE visit_records SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class VisitRecord {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 客户
     */
    @NotNull(message = "客户不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    /**
     * 客户ID
     */
    @Column(name = "customer_id", insertable = false, updatable = false)
    private Long customerId;

    /**
     * 销售人员
     */
    @NotNull(message = "销售人员不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_id", nullable = false)
    @JsonIgnore
    private User sales;

    /**
     * 销售人员ID
     */
    @Column(name = "sales_id", insertable = false, updatable = false)
    private Long salesId;

    /**
     * 拜访日期
     */
    @NotNull(message = "拜访日期不能为空")
    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    /**
     * 拜访时间
     */
    @Column(name = "visit_time")
    private LocalTime visitTime;

    /**
     * 拜访时长（分钟）
     */
    @Min(value = 1, message = "拜访时长不能小于1分钟")
    @Max(value = 1440, message = "拜访时长不能超过1440分钟")
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /**
     * 拜访类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type")
    private VisitType visitType = VisitType.FACE_TO_FACE;

    /**
     * 拜访状态
     */
    @NotNull(message = "拜访状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VisitStatus status = VisitStatus.SCHEDULED;

    /**
     * 意向等级
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "intent_level", length = 20)
    private IntentLevel intentLevel;

    /**
     * 拜访地点
     */
    @Size(max = 200, message = "拜访地点长度不能超过200字符")
    @Column(name = "location", length = 200)
    private String location;

    /**
     * 天气情况
     */
    @Size(max = 50, message = "天气情况长度不能超过50字符")
    @Column(name = "weather", length = 50)
    private String weather;

    /**
     * 商务事项
     */
    @Column(name = "business_items", columnDefinition = "TEXT")
    private String businessItems;

    /**
     * 痛点分析
     */
    @Column(name = "pain_points", columnDefinition = "TEXT")
    private String painPoints;

    /**
     * 竞争对手
     */
    @Size(max = 500, message = "竞争对手信息长度不能超过500字符")
    @Column(name = "competitors", length = 500)
    private String competitors;

    /**
     * 预算范围
     */
    @Size(max = 100, message = "预算范围长度不能超过100字符")
    @Column(name = "budget_range", length = 100)
    private String budgetRange;

    /**
     * 决策时间线
     */
    @Size(max = 200, message = "决策时间线长度不能超过200字符")
    @Column(name = "decision_timeline", length = 200)
    private String decisionTimeline;

    /**
     * 下一步行动
     */
    @Column(name = "next_step", columnDefinition = "TEXT")
    private String nextStep;

    /**
     * 跟进日期
     */
    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    /**
     * 备注
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * 是否留下资料
     */
    @Column(name = "materials_left")
    private Boolean materialsLeft = false;

    /**
     * 是否添加微信
     */
    @Column(name = "wechat_added")
    private Boolean wechatAdded = false;

    /**
     * 拜访评分（1-5分）
     */
    @Min(value = 1, message = "拜访评分不能小于1")
    @Max(value = 5, message = "拜访评分不能大于5")
    @Column(name = "rating")
    private Integer rating;

    /**
     * 创建人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;

    /**
     * 修改人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @JsonIgnore
    private User updatedBy;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 删除时间
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ==================== 枚举定义 ====================

    /**
     * 拜访类型枚举
     */
    @Getter
    public enum VisitType {
        FACE_TO_FACE("面对面拜访"),
        PHONE_CALL("电话拜访"),
        VIDEO_CALL("视频拜访"),
        EMAIL("邮件沟通"),
        WECHAT("微信沟通"),
        OTHER("其他");

        private final String description;

        VisitType(String description) {
            this.description = description;
        }
    }

    /**
     * 拜访状态枚举
     */
    @Getter
    public enum VisitStatus {
        SCHEDULED("已安排"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        CANCELLED("已取消"),
        POSTPONED("已延期"),
        NO_SHOW("未出现");

        private final String description;

        VisitStatus(String description) {
            this.description = description;
        }
    }

    /**
     * 意向等级枚举
     */
    @Getter
    public enum IntentLevel {
        VERY_HIGH("非常高"),
        HIGH("高"),
        MEDIUM("中等"),
        LOW("低"),
        VERY_LOW("非常低"),
        NO_INTENT("无意向");

        private final String description;

        IntentLevel(String description) {
            this.description = description;
        }
    }
}