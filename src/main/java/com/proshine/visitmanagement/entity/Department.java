package com.proshine.visitmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 院系实体类
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "departments", indexes = {
    @Index(name = "idx_school_id", columnList = "school_id"),
    @Index(name = "idx_name", columnList = "name")
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE departments SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Department {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * 所属学校
     */
    @NotNull(message = "学校不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    @JsonIgnore
    private School school;
    
    /**
     * 学校ID
     */
    @Column(name = "school_id", insertable = false, updatable = false)
    private Long schoolId;
    
    /**
     * 院系名称
     */
    @NotBlank(message = "院系名称不能为空")
    @Size(max = 200, message = "院系名称长度不能超过200个字符")
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    /**
     * 联系电话
     */
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    /**
     * 地址
     */
    @Size(max = 300, message = "地址长度不能超过300个字符")
    @Column(name = "address", length = 300)
    private String address;
    
    /**
     * 院系描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
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
     * 客户列表
     */
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Customer> customers;
}