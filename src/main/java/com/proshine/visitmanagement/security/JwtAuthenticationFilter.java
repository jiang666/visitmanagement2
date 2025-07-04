package com.proshine.visitmanagement.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * JWT认证过滤器
 * 
 * @author System
 * @since 2024-01-01
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    
    /**
     * 执行过滤逻辑
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 从请求中获取JWT令牌
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                // 验证令牌有效性
                if (jwtTokenProvider.validateToken(jwt)) {
                    
                    // 检查是否为访问令牌（不是刷新令牌）
                    if (!jwtTokenProvider.isAccessToken(jwt)) {
                        log.warn("尝试使用刷新令牌进行API访问: {}", request.getRequestURI());
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    // 从令牌中获取用户名
                    String username = jwtTokenProvider.getUsernameFromToken(jwt);
                    
                    if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                        
                        // 加载用户详情
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        // 验证用户状态和令牌信息
                        if (isValidUserAndToken(userDetails, jwt, request)) {
                            
                            // 从令牌中获取权限信息
                            Collection<? extends GrantedAuthority> authorities = jwtTokenProvider.getAuthorities(jwt);
                            
                            // 创建认证对象
                            UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                            
                            // 设置认证详情
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            
                            // 设置到安全上下文
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            
                            // 记录成功认证日志
                            log.debug("用户 '{}' 认证成功，访问路径: {}", username, request.getRequestURI());
                            
                            // 检查令牌是否即将过期（可选：添加响应头提醒前端刷新）
                            checkTokenExpiration(jwt, response);
                        }
                    }
                } else {
                    log.debug("无效的JWT令牌，路径: {}", request.getRequestURI());
                }
            }
        } catch (Exception e) {
            log.error("无法设置用户认证信息: {}", e.getMessage());
            // 清除可能存在的认证信息
            SecurityContextHolder.clearContext();
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求中获取JWT令牌
     * 
     * @param request HTTP请求
     * @return JWT令牌
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. 从Authorization头获取
        String bearerToken = request.getHeader(jwtTokenProvider.getJwtHeader());
        if (StringUtils.hasText(bearerToken)) {
            String token = jwtTokenProvider.resolveToken(bearerToken);
            if (token != null) {
                return token;
            }
        }
        
        // 2. 从查询参数获取（用于WebSocket等场景）
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        
        // 3. 从Cookie中获取（可选）
        if (request.getCookies() != null) {
            for (javax.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
    
    /**
     * 验证用户和令牌的有效性
     * 
     * @param userDetails 用户详情
     * @param jwt JWT令牌
     * @param request HTTP请求
     * @return 是否有效
     */
    private boolean isValidUserAndToken(UserDetails userDetails, String jwt, HttpServletRequest request) {
        try {
            // 1. 检查用户是否启用
            if (!userDetails.isEnabled()) {
                log.warn("用户账号已禁用: {}", userDetails.getUsername());
                return false;
            }
            
            // 2. 检查账号是否未过期
            if (!userDetails.isAccountNonExpired()) {
                log.warn("用户账号已过期: {}", userDetails.getUsername());
                return false;
            }
            
            // 3. 检查账号是否未锁定
            if (!userDetails.isAccountNonLocked()) {
                log.warn("用户账号已锁定: {}", userDetails.getUsername());
                return false;
            }
            
            // 4. 检查凭证是否未过期
            if (!userDetails.isCredentialsNonExpired()) {
                log.warn("用户凭证已过期: {}", userDetails.getUsername());
                return false;
            }
            
            // 5. 验证令牌中的用户名与用户详情一致
            String tokenUsername = jwtTokenProvider.getUsernameFromToken(jwt);
            if (!userDetails.getUsername().equals(tokenUsername)) {
                log.warn("令牌中的用户名与用户详情不匹配: {} vs {}", tokenUsername, userDetails.getUsername());
                return false;
            }
            
            // 6. 检查令牌是否在黑名单中（可选，需要注入AuthService）
            // if (authService.isTokenBlacklisted(jwt)) {
            //     log.warn("令牌已被列入黑名单: {}", userDetails.getUsername());
            //     return false;
            // }
            
            return true;
            
        } catch (Exception e) {
            log.error("验证用户和令牌时发生错误: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查令牌过期情况并添加响应头
     * 
     * @param jwt JWT令牌
     * @param response HTTP响应
     */
    private void checkTokenExpiration(String jwt, HttpServletResponse response) {
        try {
            // 检查令牌是否即将过期（30分钟内）
            long threshold = 30 * 60 * 1000; // 30分钟
            if (jwtTokenProvider.isTokenExpiringSoon(jwt, threshold)) {
                // 添加响应头提示前端刷新令牌
                response.setHeader("X-Token-Expiring", "true");
                
                // 获取剩余有效时间
                long remainingTime = jwtTokenProvider.getRemainingValidityTime(jwt);
                response.setHeader("X-Token-Remaining", String.valueOf(remainingTime));
                
                log.debug("令牌即将过期，剩余时间: {} ms", remainingTime);
            }
        } catch (Exception e) {
            log.debug("检查令牌过期时间失败: {}", e.getMessage());
        }
    }
    
    /**
     * 判断是否应该跳过过滤
     * 
     * @param request HTTP请求
     * @return 是否跳过
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 跳过认证的路径
        return path.startsWith("/auth/login") ||
               path.startsWith("/auth/register") ||
               path.startsWith("/auth/refresh") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/favicon.ico") ||
               path.startsWith("/error") ||
               path.equals("/");
    }
    
    /**
     * 清除安全上下文（用于登出或令牌失效）
     * 
     * @param request HTTP请求
     */
    public static void clearSecurityContext(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        log.debug("已清除安全上下文，路径: {}", request.getRequestURI());
    }
    
    /**
     * 获取当前认证的用户名
     * 
     * @return 用户名
     */
    public static String getCurrentUsername() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                return (String) principal;
            }
        } catch (Exception e) {
            log.debug("获取当前用户名失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取当前认证的用户详情
     * 
     * @return 用户详情
     */
    public static UserDetails getCurrentUserDetails() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                return (UserDetails) principal;
            }
        } catch (Exception e) {
            log.debug("获取当前用户详情失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 检查当前用户是否具有指定角色
     * 
     * @param role 角色名称
     * @return 是否具有角色
     */
    public static boolean hasRole(String role) {
        try {
            Collection<? extends GrantedAuthority> authorities = 
                    SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            
            return authorities.stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role) || 
                                         authority.getAuthority().equals(role));
        } catch (Exception e) {
            log.debug("检查用户角色失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查当前用户是否具有任一指定角色
     * 
     * @param roles 角色名称数组
     * @return 是否具有任一角色
     */
    public static boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 手动设置认证信息（用于测试或特殊场景）
     * 
     * @param userDetails 用户详情
     * @param authorities 权限集合
     */
    public static void setAuthentication(UserDetails userDetails, Collection<? extends GrantedAuthority> authorities) {
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("手动设置认证信息: {}", userDetails.getUsername());
    }
}