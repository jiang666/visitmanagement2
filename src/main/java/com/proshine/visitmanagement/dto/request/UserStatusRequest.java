package com.proshine.visitmanagement.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户状态更新请求
 */
@Data
public class UserStatusRequest {
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "状态必须是ACTIVE或INACTIVE")
    private String status;
}
