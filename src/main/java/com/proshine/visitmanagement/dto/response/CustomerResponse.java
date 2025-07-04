package com.proshine.visitmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;

    private String name;

    private String position;

    private String title;

    private Long departmentId;

    private String departmentName;

    private Long schoolId;

    private String schoolName;

    private String schoolCity;

    private String schoolType;

    private String schoolTypeDescription;

    private String phone;

    private String wechat;

    private String email;

    private String officeLocation;

    private String floorRoom;

    private String researchDirection;

    private String influenceLevel;

    private String influenceLevelDescription;

    private String decisionPower;

    private String decisionPowerDescription;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private String notes;

    private Integer visitCount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastVisitDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long createdById;

    private String createdByName;

    private Long updatedById;

    private String updatedByName;

    private String lastIntentLevel;

    private Boolean wechatAdded;
}