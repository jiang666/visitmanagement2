package com.proshine.visitmanagement.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 院系请求DTO
 */
@Data
public class DepartmentRequest {

    @NotNull(message = "学校ID不能为空")
    private Long schoolId;

    @NotBlank(message = "院系名称不能为空")
    @Size(max = 200, message = "院系名称长度不能超过200个字符")
    private String name;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;

    @Size(max = 300, message = "地址长度不能超过300个字符")
    private String address;

    @Size(max = 1000, message = "描述长度不能超过1000个字符")
    private String description;
}