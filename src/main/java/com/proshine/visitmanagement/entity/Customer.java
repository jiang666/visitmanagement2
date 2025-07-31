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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户实体类
 *
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_department_id", columnList = "department_id"),
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_influence_level", columnList = "influence_level"),
        @Index(name = "idx_created_by", columnList = "created_by"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_phone", columnList = "phone"),
        @Index(name = "idx_email", columnList = "email")
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE customers SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Customer {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 客户姓名
     */
    @NotBlank(message = "客户姓名不能为空")
    @Size(max = 100, message = "客户姓名长度不能超过100个字符")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 职位
     */
    @Size(max = 100, message = "职位长度不能超过100个字符")
    @Column(name = "position", length = 100)
    private String position;

    /**
     * 职称
     */
    @Size(max = 50, message = "职称长度不能超过50个字符")
    @Column(name = "title", length = 50)
    private String title;

    /**
     * 所属学校
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    @JsonIgnore
    private School school;

    /**
     * 所属院系
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnore
    private Department department;

    /**
     * 院系ID
     */
    @Column(name = "department_id", insertable = false, updatable = false)
    private Long departmentId;

    /**
     * 电话
     */
    @Size(max = 20, message = "电话长度不能超过20个字符")
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 微信号
     */
    @Size(max = 100, message = "微信号长度不能超过100个字符")
    @Column(name = "wechat", length = 100)
    private String wechat;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 办公地点
     */
    @Size(max = 200, message = "办公地点长度不能超过200个字符")
    @Column(name = "office_location", length = 200)
    private String officeLocation;

    /**
     * 楼层房间
     */
    @Size(max = 100, message = "楼层房间长度不能超过100个字符")
    @Column(name = "floor_room", length = 100)
    private String floorRoom;

    /**
     * 研究方向
     */
    @Column(name = "research_direction", columnDefinition = "TEXT")
    private String researchDirection;

    /**
     * 影响力等级
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "influence_level")
    private InfluenceLevel influenceLevel = InfluenceLevel.MEDIUM;

    /**
     * 决策权力
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "decision_power")
    private DecisionPower decisionPower = DecisionPower.OTHER;

    /**
     * 客户状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    /**
     * 生日
     */
    @Column(name = "birthday")
    private LocalDate birthday;

    /**
     * 备注
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 删除时间（软删除）
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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
     * 拜访记录列表
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<VisitRecord> visitRecords;

    // ==================== 枚举定义 ====================

    /**
     * 影响力等级枚举
     */
    @Getter
    public enum InfluenceLevel {
        HIGH("高影响力"),
        MEDIUM("中影响力"),
        LOW("低影响力");

        private final String description;

        InfluenceLevel(String description) {
            this.description = description;
        }
    }

    /**
     * 决策权力枚举
     */
    @Getter
    public enum DecisionPower {
        DECISION_MAKER("决策者"),
        INFLUENCER("影响者"),
        USER("使用者"),
        OTHER("其他");

        private final String description;

        DecisionPower(String description) {
            this.description = description;
        }
    }

    /**
     * 客户状态枚举
     */
    @Getter
    public enum CustomerStatus {
        ACTIVE("活跃"),
        INACTIVE("非活跃"),
        POTENTIAL("潜在客户"),
        CONVERTED("已转化"),
        LOST("已流失"),
        SUSPENDED("暂停合作");

        private final String description;

        CustomerStatus(String description) {
            this.description = description;
        }
    }
}