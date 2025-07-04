package com.proshine.visitmanagement.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置类
 *
 * @author System
 * @since 2024-01-01
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 安全过滤器链配置 (Spring Boot 2.7.6 兼容版本)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（使用JWT时不需要）
                .csrf().disable()

                // 配置CORS
                .cors().configurationSource(corsConfigurationSource())

                .and()

                // 配置会话管理为无状态
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()

                // 配置异常处理
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                .and()

                // 配置请求授权 - 使用Spring Boot 2.x的API
                .authorizeRequests()
                // 公开端点 - 不需要认证
                .antMatchers(
                        "/auth/login",
                        "/auth/register",
                        "/auth/refresh",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error"
                ).permitAll()

                // 静态资源
                .antMatchers(HttpMethod.GET,
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/fonts/**"
                ).permitAll()

                // 健康检查端点
                .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                // 认证相关端点
                .antMatchers(HttpMethod.POST, "/auth/**").permitAll()
                .antMatchers(HttpMethod.GET, "/auth/verify").authenticated()
                .antMatchers(HttpMethod.GET, "/auth/user-info").authenticated()

                // 用户管理 - 需要管理员权限
                .antMatchers(HttpMethod.GET, "/users/**").hasAnyRole("ADMIN", "MANAGER")
                .antMatchers(HttpMethod.POST, "/users/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/users/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")

                // 学校管理 - 需要管理员权限
                .antMatchers(HttpMethod.GET, "/schools/**").hasAnyRole("ADMIN", "MANAGER", "SALES")
                .antMatchers(HttpMethod.POST, "/schools/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/schools/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/schools/**").hasRole("ADMIN")

                // 院系管理 - 需要管理员权限
                .antMatchers(HttpMethod.GET, "/departments/**").hasAnyRole("ADMIN", "MANAGER", "SALES")
                .antMatchers(HttpMethod.POST, "/departments/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/departments/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/departments/**").hasRole("ADMIN")

                // 客户管理 - 所有登录用户都可以访问
                .antMatchers(HttpMethod.GET, "/customers/**").hasAnyRole("ADMIN", "MANAGER", "SALES")
                .antMatchers(HttpMethod.POST, "/customers/**").hasAnyRole("ADMIN", "MANAGER", "SALES")
                .antMatchers(HttpMethod.PUT, "/customers/**").hasAnyRole("ADMIN", "MANAGER", "SALES")
                .antMatchers(HttpMethod.DELETE, "/customers/**").hasAnyRole("ADMIN", "MANAGER")

                // 拜访记录管理 - 所有登录用户都可以访问
                .antMatchers(HttpMethod.GET, "/visits/**").hasAnyRole("ADMIN", "MANAGER", "SALES")
                .antMatchers(HttpMethod.POST, "/visits/**").hasAnyRole("ADMIN", "MANAGER", "SALES")
                .antMatchers(HttpMethod.PUT, "/visits/**").hasAnyRole("ADMIN", "MANAGER", "SALES")
                .antMatchers(HttpMethod.DELETE, "/visits/**").hasAnyRole("ADMIN", "MANAGER", "SALES")

                // 仪表盘 - 所有登录用户都可以访问
                .antMatchers(HttpMethod.GET, "/dashboard/**").hasAnyRole("ADMIN", "MANAGER", "SALES")

                // 文件上传下载
                .antMatchers(HttpMethod.POST, "/files/upload").hasAnyRole("ADMIN", "MANAGER", "SALES")
                .antMatchers(HttpMethod.GET, "/files/download/**").hasAnyRole("ADMIN", "MANAGER", "SALES")

                // 数据导出 - 需要管理员或经理权限
                .antMatchers(HttpMethod.GET, "/export/**").hasAnyRole("ADMIN", "MANAGER")

                // 系统配置 - 需要管理员权限
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/config/**").hasRole("ADMIN")

                // 其他所有请求都需要认证
                .anyRequest().authenticated()

                .and()

                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 禁用默认登录页面
                .formLogin().disable()

                // 禁用HTTP Basic认证
                .httpBasic().disable()

                // 禁用默认登出
                .logout().disable();

        return http.build();
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:8080",
                "https://*.yourdomain.com"
        ));

        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name(),
                HttpMethod.PATCH.name()
        ));

        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-CSRF-TOKEN"
        ));

        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Authorization",
                "Content-Disposition"
        ));

        // 允许发送Cookie
        configuration.setAllowCredentials(true);

        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}