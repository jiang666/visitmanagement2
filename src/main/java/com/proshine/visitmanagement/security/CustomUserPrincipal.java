package com.proshine.visitmanagement.security;

import com.proshine.visitmanagement.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * 自定义用户主体信息
 * 实现Spring Security的UserDetails接口，扩展用户信息
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserPrincipal implements UserDetails {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 角色
     */
    private User.UserRole role;
    
    /**
     * 部门
     */
    private String department;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 用户状态
     */
    private User.UserStatus status;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
    
    /**
     * 权限集合
     */
    private Collection<? extends GrantedAuthority> authorities;
    
    /**
     * 账号是否启用
     */
    private boolean enabled;
    
    /**
     * 账号是否未过期
     */
    private boolean accountNonExpired;
    
    /**
     * 账号是否未锁定
     */
    private boolean accountNonLocked;
    
    /**
     * 凭据是否未过期
     */
    private boolean credentialsNonExpired;
    
    /**
     * 获取权限集合
     * 
     * @return 权限集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    /**
     * 获取密码
     * 
     * @return 密码
     */
    @Override
    public String getPassword() {
        return password;
    }
    
    /**
     * 获取用户名
     * 
     * @return 用户名
     */
    @Override
    public String getUsername() {
        return username;
    }
    
    /**
     * 账号是否未过期
     * 
     * @return 是否未过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    /**
     * 账号是否未锁定
     * 
     * @return 是否未锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    /**
     * 凭据是否未过期
     * 
     * @return 是否未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    /**
     * 账号是否启用
     * 
     * @return 是否启用
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 获取角色名称
     * 
     * @return 角色名称
     */
    public String getRoleName() {
        return role != null ? role.name() : null;
    }
    
    /**
     * 获取角色描述
     * 
     * @return 角色描述
     */
    public String getRoleDescription() {
        return role != null ? role.getDescription() : null;
    }
    
    /**
     * 获取状态名称
     * 
     * @return 状态名称
     */
    public String getStatusName() {
        return status != null ? status.name() : null;
    }
    
    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getStatusDescription() {
        return status != null ? status.getDescription() : null;
    }
    
    /**
     * 检查是否为管理员
     * 
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        return role == User.UserRole.ADMIN;
    }
    
    /**
     * 检查是否为经理
     * 
     * @return 是否为经理
     */
    public boolean isManager() {
        return role == User.UserRole.MANAGER;
    }
    
    /**
     * 检查是否为销售
     * 
     * @return 是否为销售
     */
    public boolean isSales() {
        return role == User.UserRole.SALES;
    }
    
    /**
     * 检查账号是否激活
     * 
     * @return 是否激活
     */
    public boolean isActive() {
        return status == User.UserStatus.ACTIVE;
    }
    
    /**
     * 检查账号是否禁用
     * 
     * @return 是否禁用
     */
    public boolean isInactive() {
        return status == User.UserStatus.INACTIVE;
    }
    
    /**
     * 从User实体创建CustomUserPrincipal
     * 
     * @param user 用户实体
     * @param authorities 权限集合
     * @return CustomUserPrincipal实例
     */
    public static CustomUserPrincipal from(User user, Collection<? extends GrantedAuthority> authorities) {
        return CustomUserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .department(user.getDepartment())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .authorities(authorities)
                .enabled(user.getStatus() == User.UserStatus.ACTIVE)
                .accountNonExpired(true)
                .accountNonLocked(user.getStatus() != User.UserStatus.INACTIVE)
                .credentialsNonExpired(true)
                .build();
    }
    
    @Override
    public String toString() {
        return "CustomUserPrincipal{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", role=" + role +
                ", department='" + department + '\'' +
                ", status=" + status +
                ", enabled=" + enabled +
                '}';
    }
}