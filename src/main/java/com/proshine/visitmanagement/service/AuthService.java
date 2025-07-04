package com.proshine.visitmanagement.service;

import com.proshine.visitmanagement.dto.request.LoginRequest;
import com.proshine.visitmanagement.dto.request.RegisterRequest;
import com.proshine.visitmanagement.dto.response.LoginResponse;
import com.proshine.visitmanagement.dto.response.UserInfoResponse;
import com.proshine.visitmanagement.entity.User;
import com.proshine.visitmanagement.exception.BusinessException;
import com.proshine.visitmanagement.exception.ResourceNotFoundException;
import com.proshine.visitmanagement.repository.UserRepository;
import com.proshine.visitmanagement.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 认证服务类
 *
 * @author System
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // 获取用户信息
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

            // 检查用户状态
            if (user.getStatus() == User.UserStatus.INACTIVE) {
                throw new BusinessException("用户已被禁用");
            }

            // 生成Token
            String token = jwtTokenProvider.generateToken(authentication);

            // 更新最后登录时间
            userService.updateLastLoginTime(user.getId());

            log.info("用户登录成功: {}", user.getUsername());

            // 计算过期时间
            LocalDateTime expireTime = LocalDateTime.now().plusSeconds(jwtTokenProvider.getJwtExpiration() / 1000);

            return LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .role(user.getRole().name())
                    .roleDescription(user.getRole().getDescription())
                    .department(user.getDepartment())
                    .avatarUrl(user.getAvatarUrl())
                    .loginTime(LocalDateTime.now())
                    .expireTime(expireTime)
                    .build();

        } catch (AuthenticationException e) {
            log.warn("用户登录失败: {}", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        }
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     */
    @Transactional
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(User.UserRole.SALES); // 默认角色为销售
        user.setDepartment(request.getDepartment());
        user.setStatus(User.UserStatus.ACTIVE);

        userRepository.save(user);
        log.info("用户注册成功: {}", user.getUsername());
    }

    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @param authentication 认证信息
     */
    public void logout(HttpServletRequest request, Authentication authentication) {
        String username = authentication.getName();
        log.info("用户登出: {}", username);

        // 由于移除了Redis，这里只记录日志
        // 实际的Token失效由JWT的过期时间控制
        // 如果需要立即失效，可以考虑使用内存黑名单或数据库记录
    }

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新Token
     * @return 新的登录响应
     */
    @Transactional
    public LoginResponse refresh(String refreshToken) {
        try {
            // 验证刷新Token
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new BusinessException("刷新Token无效或已过期");
            }

            // 获取用户信息
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

            // 检查用户状态
            if (user.getStatus() == User.UserStatus.INACTIVE) {
                throw new BusinessException("用户已被禁用");
            }

            // 生成新Token
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, null, jwtTokenProvider.getAuthorities(refreshToken));
            String newToken = jwtTokenProvider.generateToken(authentication);

            log.info("Token刷新成功: {}", username);

            // 计算过期时间
            LocalDateTime expireTime = LocalDateTime.now().plusSeconds(jwtTokenProvider.getJwtExpiration() / 1000);

            return LoginResponse.builder()
                    .token(newToken)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .role(user.getRole().name())
                    .roleDescription(user.getRole().getDescription())
                    .department(user.getDepartment())
                    .avatarUrl(user.getAvatarUrl())
                    .loginTime(LocalDateTime.now())
                    .expireTime(expireTime)
                    .build();

        } catch (Exception e) {
            log.warn("Token刷新失败: {}", e.getMessage());
            throw new BusinessException("Token刷新失败");
        }
    }

    /**
     * 获取当前用户信息
     *
     * @param authentication 认证信息
     * @return 用户信息
     */
    public UserInfoResponse getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        return convertToUserInfo(user);
    }

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param authentication 认证信息
     */
    @Transactional
    public void changePassword(String oldPassword, String newPassword, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("用户修改密码成功: {}", username);
    }

    /**
     * 重置密码
     *
     * @param username 用户名
     * @param newPassword 新密码
     */
    @Transactional
    public void resetPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("重置用户密码成功: {}", username);
    }

    /**
     * 验证用户密码
     *
     * @param username 用户名
     * @param password 密码
     * @return 是否验证成功
     */
    public boolean validatePassword(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 验证Token有效性
     *
     * @param token Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return jwtTokenProvider.validateToken(token);
    }

    /**
     * 从Token获取用户信息
     *
     * @param token Token
     * @return 用户信息
     */
    public UserInfoResponse getUserInfoFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!validateToken(token)) {
            throw new BusinessException("Token无效或已过期");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        return convertToUserInfo(user);
    }

    /**
     * 检查用户是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByEmail(email);
    }

    /**
     * 转换为用户信息响应
     *
     * @param user 用户实体
     * @return 用户信息响应
     */
    private UserInfoResponse convertToUserInfo(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .roleDescription(user.getRole().getDescription())
                .department(user.getDepartment())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus().name())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}