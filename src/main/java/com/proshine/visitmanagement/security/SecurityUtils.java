package com.proshine.visitmanagement.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 安全工具类
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
public final class SecurityUtils {
    
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    // 密码复杂度正则表达式
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    // IP地址正则表达式
    private static final Pattern IP_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );
    
    // 私有构造函数，防止实例化
    private SecurityUtils() {
        throw new IllegalStateException("工具类不能被实例化");
    }
    
    /**
     * 获取当前认证信息
     * 
     * @return Authentication 认证信息
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return Optional.ofNullable(authentication);
        } catch (Exception e) {
            log.debug("获取当前认证信息失败: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * 获取当前用户名
     * 
     * @return 用户名
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentAuthentication()
                .map(Authentication::getName)
                .filter(StringUtils::hasText);
    }
    
    /**
     * 获取当前用户详情
     * 
     * @return UserDetails
     */
    public static Optional<UserDetails> getCurrentUserDetails() {
        return getCurrentAuthentication()
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof UserDetails)
                .map(principal -> (UserDetails) principal);
    }
    
    /**
     * 获取当前用户的自定义用户详情
     * 
     * @return CustomUserPrincipal
     */
    public static Optional<CustomUserPrincipal> getCurrentUserPrincipal() {
        return getCurrentAuthentication()
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof CustomUserPrincipal)
                .map(principal -> (CustomUserPrincipal) principal);
    }
    
    /**
     * 获取当前用户ID
     * 
     * @return 用户ID
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::getId);
    }
    
    /**
     * 获取当前用户的真实姓名
     * 
     * @return 真实姓名
     */
    public static Optional<String> getCurrentUserRealName() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::getRealName);
    }
    
    /**
     * 获取当前用户的部门
     * 
     * @return 部门
     */
    public static Optional<String> getCurrentUserDepartment() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::getDepartment);
    }
    
    /**
     * 获取当前用户的角色
     * 
     * @return 角色
     */
    public static Optional<String> getCurrentUserRole() {
        return getCurrentUserPrincipal()
                .map(principal -> principal.getRole().name());
    }
    
    /**
     * 获取当前用户的权限列表
     * 
     * @return 权限列表
     */
    public static Collection<String> getCurrentUserAuthorities() {
        return getCurrentAuthentication()
                .map(Authentication::getAuthorities)
                .map(authorities -> authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .orElse(java.util.Collections.emptyList());
    }
    
    /**
     * 检查当前用户是否已认证
     * 
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }
    
    /**
     * 检查当前用户是否为匿名用户
     * 
     * @return 是否为匿名用户
     */
    public static boolean isAnonymous() {
        return getCurrentUsername()
                .map(username -> "anonymousUser".equals(username))
                .orElse(true);
    }
    
    /**
     * 检查当前用户是否具有指定角色
     * 
     * @param role 角色名称（不需要ROLE_前缀）
     * @return 是否具有角色
     */
    public static boolean hasRole(String role) {
        if (!StringUtils.hasText(role)) {
            return false;
        }
        
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getCurrentUserAuthorities().contains(roleWithPrefix);
    }
    
    /**
     * 检查当前用户是否具有任一指定角色
     * 
     * @param roles 角色名称数组
     * @return 是否具有任一角色
     */
    public static boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查当前用户是否具有所有指定角色
     * 
     * @param roles 角色名称数组
     * @return 是否具有所有角色
     */
    public static boolean hasAllRoles(String... roles) {
        if (roles == null || roles.length == 0) {
            return true;
        }
        
        for (String role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 检查当前用户是否具有指定权限
     * 
     * @param permission 权限名称
     * @return 是否具有权限
     */
    public static boolean hasPermission(String permission) {
        if (!StringUtils.hasText(permission)) {
            return false;
        }
        
        return getCurrentUserAuthorities().contains(permission);
    }
    
    /**
     * 检查当前用户是否具有任一指定权限
     * 
     * @param permissions 权限名称数组
     * @return 是否具有任一权限
     */
    public static boolean hasAnyPermission(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        
        Collection<String> userAuthorities = getCurrentUserAuthorities();
        for (String permission : permissions) {
            if (userAuthorities.contains(permission)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查当前用户是否为管理员
     * 
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * 检查当前用户是否为经理
     * 
     * @return 是否为经理
     */
    public static boolean isManager() {
        return hasRole("MANAGER");
    }
    
    /**
     * 检查当前用户是否为销售
     * 
     * @return 是否为销售
     */
    public static boolean isSales() {
        return hasRole("SALES");
    }
    
    /**
     * 检查当前用户是否为管理员或经理
     * 
     * @return 是否为管理员或经理
     */
    public static boolean isAdminOrManager() {
        return hasAnyRole("ADMIN", "MANAGER");
    }
    
    /**
     * 加密密码
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encodePassword(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return PASSWORD_ENCODER.encode(rawPassword);
    }
    
    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密密码
     * @return 是否匹配
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(encodedPassword)) {
            return false;
        }
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }
    
    /**
     * 验证密码复杂度
     * 
     * @param password 密码
     * @return 是否符合复杂度要求
     */
    public static boolean isPasswordComplex(String password) {
        if (!StringUtils.hasText(password)) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * 生成随机密码
     * 
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("密码长度不能小于8位");
        }
        
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "@$!%*?&";
        String allChars = upperCase + lowerCase + digits + specialChars;
        
        StringBuilder password = new StringBuilder();
        
        // 确保密码包含各种字符类型
        password.append(upperCase.charAt(SECURE_RANDOM.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(SECURE_RANDOM.nextInt(lowerCase.length())));
        password.append(digits.charAt(SECURE_RANDOM.nextInt(digits.length())));
        password.append(specialChars.charAt(SECURE_RANDOM.nextInt(specialChars.length())));
        
        // 填充剩余长度
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(SECURE_RANDOM.nextInt(allChars.length())));
        }
        
        // 打乱密码顺序
        return shuffleString(password.toString());
    }
    
    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip != null ? ip : "unknown";
    }
    
    /**
     * 获取用户代理信息
     * 
     * @param request HTTP请求
     * @return 用户代理字符串
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "unknown";
    }
    
    /**
     * 验证IP地址格式
     * 
     * @param ip IP地址
     * @return 是否为有效IP
     */
    public static boolean isValidIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }
        return IP_PATTERN.matcher(ip).matches();
    }
    
    /**
     * 检查IP是否为内网地址
     * 
     * @param ip IP地址
     * @return 是否为内网地址
     */
    public static boolean isInternalIp(String ip) {
        if (!isValidIp(ip)) {
            return false;
        }
        
        String[] parts = ip.split("\\.");
        int firstOctet = Integer.parseInt(parts[0]);
        int secondOctet = Integer.parseInt(parts[1]);
        
        // 10.0.0.0/8
        if (firstOctet == 10) {
            return true;
        }
        
        // 172.16.0.0/12
        if (firstOctet == 172 && secondOctet >= 16 && secondOctet <= 31) {
            return true;
        }
        
        // 192.168.0.0/16
        if (firstOctet == 192 && secondOctet == 168) {
            return true;
        }
        
        // 127.0.0.0/8 (localhost)
        if (firstOctet == 127) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 清除安全上下文
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        log.debug("已清除安全上下文");
    }
    
    /**
     * 生成安全令牌
     * 
     * @param length 令牌长度
     * @return 安全令牌
     */
    public static String generateSecureToken(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("令牌长度必须大于0");
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            token.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        
        return token.toString();
    }
    
    /**
     * 检查当前用户是否可以访问指定用户的资源
     * 
     * @param targetUserId 目标用户ID
     * @return 是否可以访问
     */
    public static boolean canAccessUserResource(Long targetUserId) {
        if (targetUserId == null) {
            return false;
        }
        
        // 管理员可以访问所有用户资源
        if (isAdmin()) {
            return true;
        }
        
        // 用户只能访问自己的资源
        Optional<Long> currentUserId = getCurrentUserId();
        return currentUserId.isPresent() && currentUserId.get().equals(targetUserId);
    }
    
    /**
     * 检查当前用户是否可以管理指定部门
     * 
     * @param department 部门名称
     * @return 是否可以管理
     */
    public static boolean canManageDepartment(String department) {
        if (!StringUtils.hasText(department)) {
            return false;
        }
        
        // 管理员可以管理所有部门
        if (isAdmin()) {
            return true;
        }
        
        // 经理只能管理自己的部门
        if (isManager()) {
            Optional<String> currentDepartment = getCurrentUserDepartment();
            return currentDepartment.isPresent() && currentDepartment.get().equals(department);
        }
        
        return false;
    }
    
    /**
     * 打乱字符串
     * 
     * @param input 输入字符串
     * @return 打乱后的字符串
     */
    private static String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}