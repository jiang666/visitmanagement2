package com.proshine.visitmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学校响应类
 *
 * @author System
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolResponse {

    private Long id;
    private String name;
    private String address;
    private String province;
    private String city;
    private String schoolType; // 向后兼容，返回第一个类型
    private String schoolTypeDescription; // 向后兼容，返回第一个类型的描述
    private List<String> schoolTypes; // 新字段：所有学校类型
    private List<String> schoolTypeDescriptions; // 新字段：所有学校类型的描述
    private String contactPhone;
    private String website;
    private Integer departmentCount;
    private Integer customerCount;
    private Integer visitCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastVisitDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}