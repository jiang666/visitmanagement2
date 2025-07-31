package com.proshine.visitmanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 客户请求DTO
 */
@Data
public class CustomerRequest {

    @NotBlank(message = "客户姓名不能为空")
    @Size(max = 100, message = "客户姓名长度不能超过100个字符")
    private String name;

    @Size(max = 100, message = "职位长度不能超过100个字符")
    private String position;

    @Size(max = 50, message = "职称长度不能超过50个字符")
    private String title;

    private Long schoolId;

    private Long departmentId;

    @Size(max = 20, message = "电话长度不能超过20个字符")
    private String phone;

    @Size(max = 100, message = "微信号长度不能超过100个字符")
    private String wechat;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Size(max = 200, message = "办公地点长度不能超过200个字符")
    private String officeLocation;

    @Size(max = 100, message = "楼层房间长度不能超过100个字符")
    private String floorRoom;

    @Size(max = 1000, message = "研究方向长度不能超过1000个字符")
    private String researchDirection;

    @Pattern(regexp = "HIGH|MEDIUM|LOW", message = "影响力等级无效")
    private String influenceLevel = "MEDIUM";

    @Pattern(regexp = "DECISION_MAKER|INFLUENCER|USER|OTHER", message = "决策权力无效")
    private String decisionPower = "OTHER";

    /**
     * 客户状态
     * 新增字段：支持客户状态管理
     */
    @Pattern(regexp = "ACTIVE|INACTIVE|POTENTIAL|CONVERTED|LOST|SUSPENDED", message = "客户状态无效")
    private String status = "ACTIVE";

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @Size(max = 2000, message = "备注长度不能超过2000个字符")
    private String notes;

    /**
     * 是否添加微信（新增字段）
     * 注意：这个字段主要用于导入时的标识，实际业务逻辑中通常根据wechat字段是否为空来判断
     */
    private Boolean wechatAdded;
}