package com.proshine.visitmanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证入口点
 * 当用户访问受保护的资源但未提供有效认证时，会通过此类处理
 * 
 * @author System
 * @since 2024-01-01
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 处理认证失败的情况
     * 当用户未认证或认证失败时调用此方法
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param authException 认证异常
     * @throws IOException IO异常
     */
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String remoteAddr = getClientIpAddress(request);
        
        // 记录未授权访问日志
        log.warn("未授权访问 - URI: {} {}, IP: {}, 异常: {}", 
                method, requestURI, remoteAddr, authException.getMessage());
        
        // 设置响应状态和内容类型
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // 创建错误响应体
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", 401);
        errorResponse.put("message", "认证失败，请先登录");
        errorResponse.put("data", null);
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", requestURI);
        
        // 根据异常类型提供更具体的错误信息
        String detailMessage = getDetailedErrorMessage(authException, request);
        errorResponse.put("detail", detailMessage);
        
        // 添加调试信息（仅在开发环境）
        if (isDebugMode()) {
            errorResponse.put("debug", authException.getClass().getSimpleName());
        }
        
        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
    
    /**
     * 获取详细的错误信息
     * 
     * @param authException 认证异常
     * @param request HTTP请求
     * @return 详细错误信息
     */
    private String getDetailedErrorMessage(AuthenticationException authException, HttpServletRequest request) {
        String errorMessage = authException.getMessage();
        String requestURI = request.getRequestURI();
        
        // JWT相关错误
        if (errorMessage != null) {
            if (errorMessage.contains("JWT")) {
                if (errorMessage.contains("expired")) {
                    return "访问令牌已过期，请刷新令牌或重新登录";
                } else if (errorMessage.contains("invalid")) {
                    return "访问令牌无效，请重新登录";
                } else if (errorMessage.contains("malformed")) {
                    return "访问令牌格式错误，请重新登录";
                }
            }
        }
        
        // 根据请求路径提供特定的提示
        if (requestURI.startsWith("/api/admin")) {
            return "访问管理员功能需要管理员权限，请确保已正确登录";
        } else if (requestURI.startsWith("/api/manager")) {
            return "访问管理功能需要管理员或经理权限";
        }
        
        // 检查请求头中是否包含Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            return "请在请求头中提供有效的访问令牌";
        } else if (!authHeader.startsWith("Bearer ")) {
            return "访问令牌格式错误，应以 'Bearer ' 开头";
        }
        
        // 默认错误信息
        return "认证失败，请检查登录状态";
    }
    
    /**
     * 获取客户端真实IP地址
     * 
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
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
        
        // 处理多IP的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip != null ? ip : "unknown";
    }
    
    /**
     * 判断是否为调试模式
     * 
     * @return 是否为调试模式
     */
    private boolean isDebugMode() {
        // 可以通过系统属性或环境变量来控制
        String profile = System.getProperty("spring.profiles.active");
        return "dev".equals(profile) || "development".equals(profile);
    }
}