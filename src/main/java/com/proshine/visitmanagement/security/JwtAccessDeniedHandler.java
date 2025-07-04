package com.proshine.visitmanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT访问拒绝处理器
 * 当已认证的用户访问没有权限的资源时，会通过此类处理
 * 
 * @author System
 * @since 2024-01-01
 */
@Component
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 处理访问拒绝的情况
     * 当用户已认证但没有足够权限访问资源时调用此方法
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param accessDeniedException 访问拒绝异常
     * @throws IOException IO异常
     */
    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String remoteAddr = getClientIpAddress(request);
        
        // 获取当前认证用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "unknown";
        
        // 记录访问拒绝日志
        log.warn("访问被拒绝 - 用户: {}, URI: {} {}, IP: {}, 异常: {}", 
                username, method, requestURI, remoteAddr, accessDeniedException.getMessage());
        
        // 设置响应状态和内容类型
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // 创建错误响应体
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", 403);
        errorResponse.put("message", "访问被拒绝，权限不足");
        errorResponse.put("data", null);
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", requestURI);
        
        // 根据请求路径和用户角色提供更具体的错误信息
        String detailMessage = getDetailedErrorMessage(request, authentication);
        errorResponse.put("detail", detailMessage);
        
        // 添加建议的解决方案
        String suggestion = getSuggestion(request, authentication);
        if (suggestion != null) {
            errorResponse.put("suggestion", suggestion);
        }
        
        // 添加调试信息（仅在开发环境）
        if (isDebugMode()) {
            errorResponse.put("debug", createDebugInfo(authentication, accessDeniedException));
        }
        
        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
    
    /**
     * 获取详细的错误信息
     * 
     * @param request HTTP请求
     * @param authentication 认证信息
     * @return 详细错误信息
     */
    private String getDetailedErrorMessage(HttpServletRequest request, Authentication authentication) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // 管理员功能
        if (requestURI.startsWith("/api/admin") || requestURI.startsWith("/api/users")) {
            return "此功能需要管理员权限，当前用户权限不足";
        }
        
        // 经理功能
        if (requestURI.startsWith("/api/manager") || requestURI.contains("/export")) {
            return "此功能需要管理员或经理权限，当前用户权限不足";
        }
        
        // 基于HTTP方法的权限检查
        if ("DELETE".equals(method)) {
            return "删除操作需要更高级权限，当前用户权限不足";
        } else if ("PUT".equals(method) || "PATCH".equals(method)) {
            return "修改操作需要相应权限，当前用户权限不足";
        } else if ("POST".equals(method)) {
            return "创建操作需要相应权限，当前用户权限不足";
        }
        
        // 特定资源的权限检查
        if (requestURI.contains("/users/")) {
            return "用户管理功能需要管理员权限";
        } else if (requestURI.contains("/schools/") && !"GET".equals(method)) {
            return "学校信息管理需要管理员权限";
        } else if (requestURI.contains("/departments/") && !"GET".equals(method)) {
            return "院系信息管理需要管理员权限";
        }
        
        // 获取用户当前角色
        String currentRole = getCurrentUserRole(authentication);
        if (currentRole != null) {
            return String.format("当前用户角色为 %s，无权限访问此资源", currentRole);
        }
        
        return "当前用户权限不足，无法访问此资源";
    }
    
    /**
     * 获取解决建议
     * 
     * @param request HTTP请求
     * @param authentication 认证信息
     * @return 解决建议
     */
    private String getSuggestion(HttpServletRequest request, Authentication authentication) {
        String requestURI = request.getRequestURI();
        String currentRole = getCurrentUserRole(authentication);
        
        if (requestURI.startsWith("/api/admin") || requestURI.startsWith("/api/users")) {
            return "请联系系统管理员获取管理员权限";
        }
        
        if (requestURI.contains("/export") || requestURI.startsWith("/api/manager")) {
            return "请联系管理员或经理获取相应权限";
        }
        
        if ("SALES".equals(currentRole)) {
            return "销售人员如需更高权限，请联系经理或管理员";
        }
        
        return "如需访问此功能，请联系系统管理员申请相应权限";
    }
    
    /**
     * 获取当前用户角色
     * 
     * @param authentication 认证信息
     * @return 用户角色
     */
    private String getCurrentUserRole(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return null;
        }
        
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // 移除 "ROLE_" 前缀
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 创建调试信息
     * 
     * @param authentication 认证信息
     * @param accessDeniedException 访问拒绝异常
     * @return 调试信息
     */
    private Map<String, Object> createDebugInfo(Authentication authentication, AccessDeniedException accessDeniedException) {
        Map<String, Object> debugInfo = new HashMap<>();
        
        if (authentication != null) {
            debugInfo.put("username", authentication.getName());
            debugInfo.put("authenticated", authentication.isAuthenticated());
            debugInfo.put("authorities", authentication.getAuthorities().toString());
            debugInfo.put("principal", authentication.getPrincipal().getClass().getSimpleName());
        } else {
            debugInfo.put("authentication", "null");
        }
        
        debugInfo.put("exception", accessDeniedException.getClass().getSimpleName());
        debugInfo.put("exceptionMessage", accessDeniedException.getMessage());
        
        return debugInfo;
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