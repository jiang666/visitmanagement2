package com.proshine.visitmanagement.controller;

import com.proshine.visitmanagement.dto.request.LoginRequest;
import com.proshine.visitmanagement.dto.request.RegisterRequest;
import com.proshine.visitmanagement.dto.response.ApiResponse;
import com.proshine.visitmanagement.dto.response.LoginResponse;
import com.proshine.visitmanagement.dto.response.UserInfoResponse;
import com.proshine.visitmanagement.service.AuthService;
import com.proshine.visitmanagement.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 认证控制器
 * 处理用户登录、注册、登出等认证相关操作
 *
 * @author System
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @return 登录响应，包含Token和用户信息
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录: username={}", request.getUsername());

        LoginResponse loginResponse = authService.login(request);

        log.info("用户登录成功: username={}, userId={}", request.getUsername(), loginResponse.getUserId());

        return ApiResponse.success(loginResponse, "登录成功");
    }

    /**
     * 用户注册
     *
     * @param request 注册请求参数
     * @return 注册响应
     */
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册: username={}, realName={}", request.getUsername(), request.getRealName());

        // 验证密码确认
        ValidationUtils.passwordConfirm(request.getPassword(), request.getConfirmPassword());

        authService.register(request);

        log.info("用户注册成功: username={}", request.getUsername());

        return ApiResponse.success("注册成功");
    }

    /**
     * 获取当前用户信息
     *
     * @param authentication 认证信息
     * @return 用户信息
     */
    @GetMapping("/user-info")
    public ApiResponse<UserInfoResponse> getUserInfo(Authentication authentication) {
        log.debug("获取用户信息: username={}", authentication.getName());

        UserInfoResponse userInfo = authService.getUserInfo(authentication);

        return ApiResponse.success(userInfo, "获取用户信息成功");
    }

    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @param authentication 认证信息
     * @return 登出响应
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, Authentication authentication) {
        log.info("用户登出: username={}", authentication.getName());

        authService.logout(request, authentication);

        log.info("用户登出成功: username={}", authentication.getName());

        return ApiResponse.success("登出成功");
    }

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新Token
     * @return 新的Token信息
     */
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(
            @RequestParam @NotBlank(message = "刷新Token不能为空") String refreshToken) {

        log.info("刷新Token");

        LoginResponse loginResponse = authService.refresh(refreshToken);

        log.info("Token刷新成功: userId={}", loginResponse.getUserId());

        return ApiResponse.success(loginResponse, "Token刷新成功");
    }

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param confirmPassword 确认新密码
     * @param authentication 认证信息
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            @RequestParam @NotBlank(message = "旧密码不能为空") String oldPassword,
            @RequestParam @NotBlank(message = "新密码不能为空")
            @Size(min = 6, max = 20, message = "新密码长度在6-20个字符之间") String newPassword,
            @RequestParam @NotBlank(message = "确认密码不能为空") String confirmPassword,
            Authentication authentication) {

        log.info("修改密码: username={}", authentication.getName());

        // 验证密码确认
        ValidationUtils.passwordConfirm(newPassword, confirmPassword);

        authService.changePassword(oldPassword, newPassword, authentication);

        log.info("密码修改成功: username={}", authentication.getName());

        return ApiResponse.success("密码修改成功");
    }
}