package com.proshine.visitmanagement.security;

import com.proshine.visitmanagement.entity.User;
import com.proshine.visitmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义用户详情服务
 *
 * @author System
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 根据用户名加载用户详情
     *
     * @param username 用户名
     * @return UserDetails 用户详情
     * @throws UsernameNotFoundException 用户未找到异常
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("正在加载用户详情: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("用户不存在: {}", username);
                    return new UsernameNotFoundException("用户不存在: " + username);
                });

        log.debug("成功加载用户: {}, 角色: {}, 状态: {}",
                user.getUsername(), user.getRole(), user.getStatus());

        return createUserDetails(user);
    }

    /**
     * 根据用户ID加载用户详情
     *
     * @param userId 用户ID
     * @return UserDetails 用户详情
     * @throws UsernameNotFoundException 用户未找到异常
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("正在根据ID加载用户详情: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("用户ID不存在: {}", userId);
                    return new UsernameNotFoundException("用户ID不存在: " + userId);
                });

        log.debug("成功根据ID加载用户: {}, 角色: {}, 状态: {}",
                user.getUsername(), user.getRole(), user.getStatus());

        return createUserDetails(user);
    }

    /**
     * 创建Spring Security的UserDetails对象
     *
     * @param user 用户实体
     * @return UserDetails
     */
    private UserDetails createUserDetails(User user) {
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
                .authorities(getAuthorities(user))
                .enabled(user.getStatus() == User.UserStatus.ACTIVE)
                .accountNonExpired(true)
                .accountNonLocked(user.getStatus() != User.UserStatus.INACTIVE)
                .credentialsNonExpired(true)
                .build();
    }

    /**
     * 获取用户权限列表
     *
     * @param user 用户实体
     * @return 权限集合
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        if (user.getRole() == null) {
            log.warn("用户 {} 没有角色信息", user.getUsername());
            return Collections.emptyList();
        }

        // 基于角色创建权限
        String roleName = "ROLE_" + user.getRole().name();
        SimpleGrantedAuthority roleAuthority = new SimpleGrantedAuthority(roleName);

        // 根据角色添加具体权限
        List<GrantedAuthority> authorities = getPermissionsByRole(user.getRole());
        authorities.add(roleAuthority);

        log.debug("用户 {} 的权限: {}", user.getUsername(),
                authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

        return authorities;
    }

    /**
     * 根据角色获取具体权限
     *
     * @param role 用户角色
     * @return 权限列表
     */
    private List<GrantedAuthority> getPermissionsByRole(User.UserRole role) {
        List<GrantedAuthority> permissions = new java.util.ArrayList<>();

        switch (role) {
            case ADMIN:
                // 管理员拥有所有权限
                permissions.add(new SimpleGrantedAuthority("user:read"));
                permissions.add(new SimpleGrantedAuthority("user:write"));
                permissions.add(new SimpleGrantedAuthority("user:delete"));
                permissions.add(new SimpleGrantedAuthority("school:read"));
                permissions.add(new SimpleGrantedAuthority("school:write"));
                permissions.add(new SimpleGrantedAuthority("school:delete"));
                permissions.add(new SimpleGrantedAuthority("department:read"));
                permissions.add(new SimpleGrantedAuthority("department:write"));
                permissions.add(new SimpleGrantedAuthority("department:delete"));
                permissions.add(new SimpleGrantedAuthority("customer:read"));
                permissions.add(new SimpleGrantedAuthority("customer:write"));
                permissions.add(new SimpleGrantedAuthority("customer:delete"));
                permissions.add(new SimpleGrantedAuthority("visit:read"));
                permissions.add(new SimpleGrantedAuthority("visit:write"));
                permissions.add(new SimpleGrantedAuthority("visit:delete"));
                permissions.add(new SimpleGrantedAuthority("dashboard:read"));
                permissions.add(new SimpleGrantedAuthority("export:read"));
                permissions.add(new SimpleGrantedAuthority("system:config"));
                break;

            case MANAGER:
                // 经理权限
                permissions.add(new SimpleGrantedAuthority("user:read"));
                permissions.add(new SimpleGrantedAuthority("school:read"));
                permissions.add(new SimpleGrantedAuthority("department:read"));
                permissions.add(new SimpleGrantedAuthority("customer:read"));
                permissions.add(new SimpleGrantedAuthority("customer:write"));
                permissions.add(new SimpleGrantedAuthority("customer:delete"));
                permissions.add(new SimpleGrantedAuthority("visit:read"));
                permissions.add(new SimpleGrantedAuthority("visit:write"));
                permissions.add(new SimpleGrantedAuthority("visit:delete"));
                permissions.add(new SimpleGrantedAuthority("dashboard:read"));
                permissions.add(new SimpleGrantedAuthority("export:read"));
                break;

            case SALES:
                // 销售权限
                permissions.add(new SimpleGrantedAuthority("school:read"));
                permissions.add(new SimpleGrantedAuthority("department:read"));
                permissions.add(new SimpleGrantedAuthority("customer:read"));
                permissions.add(new SimpleGrantedAuthority("customer:write"));
                permissions.add(new SimpleGrantedAuthority("visit:read"));
                permissions.add(new SimpleGrantedAuthority("visit:write"));
                permissions.add(new SimpleGrantedAuthority("visit:delete"));
                permissions.add(new SimpleGrantedAuthority("dashboard:read"));
                break;

            default:
                log.warn("未知角色: {}", role);
                break;
        }

        return permissions;
    }

    /**
     * 检查用户是否具有指定权限
     *
     * @param username 用户名
     * @param permission 权限名称
     * @return 是否具有权限
     */
    public boolean hasPermission(String username, String permission) {
        try {
            UserDetails userDetails = loadUserByUsername(username);
            return userDetails.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(permission));
        } catch (UsernameNotFoundException e) {
            log.warn("检查权限时用户不存在: {}", username);
            return false;
        }
    }

    /**
     * 检查用户是否具有指定角色
     *
     * @param username 用户名
     * @param role 角色名称
     * @return 是否具有角色
     */
    public boolean hasRole(String username, String role) {
        String roleAuthority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return hasPermission(username, roleAuthority);
    }

    /**
     * 获取用户的所有权限
     *
     * @param username 用户名
     * @return 权限列表
     */
    public List<String> getUserPermissions(String username) {
        try {
            UserDetails userDetails = loadUserByUsername(username);
            return userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        } catch (UsernameNotFoundException e) {
            log.warn("获取用户权限时用户不存在: {}", username);
            return Collections.emptyList();
        }
    }

    /**
     * 刷新用户详情（当用户信息发生变化时调用）
     *
     * @param username 用户名
     * @return 更新后的UserDetails
     */
    public UserDetails refreshUserDetails(String username) {
        log.debug("刷新用户详情: {}", username);
        return loadUserByUsername(username);
    }

    /**
     * 验证用户账号状态
     *
     * @param username 用户名
     * @return 账号是否有效
     */
    public boolean isAccountValid(String username) {
        try {
            UserDetails userDetails = loadUserByUsername(username);
            return userDetails.isEnabled() &&
                    userDetails.isAccountNonExpired() &&
                    userDetails.isAccountNonLocked() &&
                    userDetails.isCredentialsNonExpired();
        } catch (UsernameNotFoundException e) {
            log.warn("验证账号状态时用户不存在: {}", username);
            return false;
        }
    }
}