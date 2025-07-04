package com.proshine.visitmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 拜访记录响应DTO
 *
 * @author System
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitRecordResponse {

    private Long id;

    // ==================== 客户信息 ====================
    private Long customerId;
    private String customerName;
    private String customerPosition;
    private String customerPhone;
    private String customerEmail;
    private String customerInfluenceLevel;
    private String customerDecisionPower;

    // ==================== 院系和学校信息 ====================
    private Long departmentId;
    private String departmentName;
    private Long schoolId;
    private String schoolName;
    private String schoolCity;

    // ==================== 销售人员信息 ====================
    private Long salesId;
    private String salesName;
    private String salesDepartment;

    // ==================== 拜访基础信息 ====================
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime visitTime;

    private Integer durationMinutes;
    private String visitType;
    private String visitTypeDescription;
    private String status;
    private String statusDescription;
    private String intentLevel;
    private String intentLevelDescription;

    // ==================== 拜访详细内容 ====================
    private String availableMatters;
    private String demandAnalysis;
    private String competitorAnalysis;
    private String nextSteps;
    private String visitSummary;
    private String notes;

    /**
     * 可办事项
     */
    private String businessItems;

    /**
     * 需求痛点
     */
    private String painPoints;

    /**
     * 竞品信息
     */
    private String competitors;

    /**
     * 预算范围
     */
    private String budgetRange;

    /**
     * 决策时间线
     */
    private String decisionTimeline;

    /**
     * 下一步计划
     */
    private String nextStep;

    /**
     * 跟进日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate followUpDate;

    /**
     * 留下的资料
     */
    private String materialsLeft;

    /**
     * 是否添加微信
     */
    private Boolean wechatAdded;

    /**
     * 拜访评分
     */
    private Integer rating;

    /**
     * 拜访地点
     */
    private String location;

    /**
     * 天气情况
     */
    private String weather;

    // ==================== 时间信息 ====================
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}