package com.proshine.visitmanagement.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学校实体类
 *
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "schools", indexes = {
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_city", columnList = "city"),
        @Index(name = "idx_province", columnList = "province")
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE schools SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class School {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 学校名称
     */
    @NotBlank(message = "学校名称不能为空")
    @Size(max = 200, message = "学校名称长度不能超过200个字符")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 地址
     */
    @Size(max = 500, message = "地址长度不能超过500个字符")
    @Column(name = "address", length = 500)
    private String address;

    /**
     * 省份
     */
    @Size(max = 50, message = "省份长度不能超过50个字符")
    @Column(name = "province", length = 50)
    private String province;

    /**
     * 城市
     */
    @Size(max = 50, message = "城市长度不能超过50个字符")
    @Column(name = "city", length = 50)
    private String city;

    /**
     * 学校类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "school_type")
    private SchoolType schoolType = SchoolType.REGULAR;

    /**
     * 联系电话
     */
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /**
     * 官方网站
     */
    @Size(max = 200, message = "网站长度不能超过200个字符")
    @Column(name = "website", length = 200)
    private String website;

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
     * 院系列表
     */
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Department> departments;

    /**
     * 学校类型枚举
     */
    public enum SchoolType {
        /**
         * 985工程
         */
        PROJECT_985("985工程"),

        /**
         * 211工程
         */
        PROJECT_211("211工程"),

        /**
         * 双一流
         */
        DOUBLE_FIRST_CLASS("双一流"),

        /**
         * 普通高校
         */
        REGULAR("普通高校");

        private final String description;

        SchoolType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}

