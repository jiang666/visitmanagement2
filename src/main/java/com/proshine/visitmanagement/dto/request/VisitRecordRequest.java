package com.proshine.visitmanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 拜访记录请求DTO
 */
@Data
public class VisitRecordRequest {
    
    @NotNull(message = "客户ID不能为空")
    private Long customerId;
    
    @NotNull(message = "拜访日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime visitTime;
    
    @Min(value = 1, message = "拜访时长不能小于1分钟")
    @Max(value = 1440, message = "拜访时长不能超过1440分钟")
    private Integer durationMinutes;
    
    @Pattern(regexp = "FIRST_VISIT|FOLLOW_UP|TECHNICAL|BUSINESS|AFTER_SALES", 
             message = "拜访类型无效")
    private String visitType = "FIRST_VISIT";
    
    @Pattern(regexp = "SCHEDULED|COMPLETED|CANCELLED|POSTPONED", 
             message = "拜访状态无效")
    private String status = "SCHEDULED";
    
    @Pattern(regexp = "A|B|C|D", message = "意向等级无效")
    private String intentLevel = "C";
    
    @Size(max = 1000, message = "可办事项长度不能超过1000个字符")
    private String businessItems;
    
    @Size(max = 1000, message = "需求痛点长度不能超过1000个字符")
    private String painPoints;
    
    @Size(max = 1000, message = "竞品信息长度不能超过1000个字符")
    private String competitors;
    
    @Size(max = 100, message = "预算范围长度不能超过100个字符")
    private String budgetRange;
    
    @Size(max = 100, message = "决策时间线长度不能超过100个字符")
    private String decisionTimeline;
    
    @Size(max = 1000, message = "下一步计划长度不能超过1000个字符")
    private String nextStep;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate followUpDate;
    
    @Size(max = 2000, message = "拜访备注长度不能超过2000个字符")
    private String notes;
    
    /**
     * 是否留下资料
     */
    private Boolean materialsLeft;
    
    private Boolean wechatAdded = false;
    
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer rating;
    
    @Size(max = 200, message = "拜访地点长度不能超过200个字符")
    private String location;
    
    @Size(max = 50, message = "天气情况长度不能超过50个字符")
    private String weather;
}
