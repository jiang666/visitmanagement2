package com.proshine.visitmanagement.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户请求DTO
 */
@Data
public class UserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    @Size(min = 6, max = 20, message = "密码长度在6-20个字符之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 100, message = "真实姓名长度不能超过100个字符")
    private String realName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "ADMIN|MANAGER|SALES", message = "角色必须是ADMIN、MANAGER或SALES")
    private String role;

    @Size(max = 100, message = "部门长度不能超过100个字符")
    private String department;

    private String avatarUrl;
}