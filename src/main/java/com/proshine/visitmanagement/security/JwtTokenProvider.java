package com.proshine.visitmanagement.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT工具类
 *
 * @author System
 * @since 2024-01-01
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.header:Authorization}")
    private String jwtHeader;

    @Value("${app.jwt.prefix:Bearer }")
    private String jwtPrefix;

    private SecretKey secretKey;

    /**
     * 初始化密钥
     */
    @PostConstruct
    public void init() {
        // 确保密钥长度足够 - Java 8兼容版本
        String keyString = jwtSecret;
        if (keyString.length() < 64) {
            // Java 8兼容的字符串重复方法
            String paddingString = "0123456789abcdef";
            StringBuilder sb = new StringBuilder(keyString);

            while (sb.length() < 64) {
                sb.append(paddingString);
            }

            keyString = sb.substring(0, 64);
        }

        this.secretKey = Keys.hmacShaKeyFor(keyString.getBytes(StandardCharsets.UTF_8));
        log.info("JWT TokenProvider initialized with expiration: {} ms", jwtExpiration);
    }

    /**
     * 生成访问令牌
     *
     * @param authentication 认证信息
     * @return JWT令牌
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration);

        // 获取用户角色
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 生成刷新令牌
     *
     * @param authentication 认证信息
     * @return 刷新令牌
     */
    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration * 7); // 刷新令牌有效期7倍

        return Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (JwtException e) {
            log.error("无法从令牌中获取用户名: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从令牌中获取权限信息
     *
     * @param token JWT令牌
     * @return 权限集合
     */
    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            if (roles == null || roles.isEmpty()) {
                // Java 8兼容：使用Arrays.asList代替List.of()
                return Arrays.asList();
            }

            return roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

        } catch (JwtException e) {
            log.error("无法从令牌中获取权限信息: {}", e.getMessage());
            // Java 8兼容：使用Arrays.asList代替List.of()
            return Arrays.asList();
        }
    }

    /**
     * 验证令牌
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            log.error("JWT签名无效: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT令牌格式错误: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT令牌已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT令牌: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT令牌为空: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 从令牌中获取过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims != null ? claims.getExpiration() : null;
        } catch (Exception e) {
            log.error("获取令牌过期时间失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取令牌中的所有声明
     *
     * @param token JWT令牌
     * @return 声明
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("无法从令牌中获取声明: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查令牌是否过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        Date expirationDate = getExpirationDateFromToken(token);
        if (expirationDate == null) {
            return true;
        }
        return expirationDate.before(new Date());
    }

    /**
     * 检查是否为刷新令牌
     *
     * @param token JWT令牌
     * @return 是否为刷新令牌
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            if (claims == null) {
                return false;
            }

            String type = claims.get("type", String.class);
            return "refresh".equals(type);
        } catch (Exception e) {
            log.error("无法检查令牌类型: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否为访问令牌
     *
     * @param token JWT令牌
     * @return 是否为访问令牌
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            if (claims == null) {
                return false;
            }

            String type = claims.get("type", String.class);
            return "access".equals(type) || type == null; // 默认为访问令牌
        } catch (Exception e) {
            log.error("无法检查令牌类型: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从HTTP头中解析令牌
     *
     * @param bearerToken Bearer令牌字符串
     * @return JWT令牌
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(jwtPrefix)) {
            return bearerToken.substring(jwtPrefix.length());
        }
        return null;
    }

    /**
     * 检查令牌是否可以刷新
     *
     * @param token JWT令牌
     * @return 是否可以刷新
     */
    public boolean canTokenBeRefreshed(String token) {
        try {
            if (!validateToken(token)) {
                return false;
            }

            // 检查是否为刷新令牌
            if (!isRefreshToken(token)) {
                return false;
            }

            // 检查是否已过期
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("检查令牌是否可以刷新时出错: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 生成基于现有令牌的新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    public String generateTokenFromRefreshToken(String refreshToken) {
        try {
            if (!canTokenBeRefreshed(refreshToken)) {
                throw new IllegalArgumentException("刷新令牌无效或已过期");
            }

            String username = getUsernameFromToken(refreshToken);
            if (username == null) {
                throw new IllegalArgumentException("无法从刷新令牌中获取用户名");
            }

            Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration);

            return Jwts.builder()
                    .setSubject(username)
                    .claim("type", "access")
                    .setIssuedAt(new Date())
                    .setExpiration(expiryDate)
                    .signWith(secretKey, SignatureAlgorithm.HS512)
                    .compact();

        } catch (Exception e) {
            log.error("基于刷新令牌生成访问令牌失败: {}", e.getMessage());
            throw new RuntimeException("令牌刷新失败", e);
        }
    }

    /**
     * 获取令牌的JTI（JWT ID）
     *
     * @param token JWT令牌
     * @return JTI
     */
    public String getJwtId(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims != null ? claims.getId() : null;
        } catch (Exception e) {
            log.error("获取JWT ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查令牌是否即将过期
     *
     * @param token JWT令牌
     * @param threshold 阈值（毫秒）
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token, long threshold) {
        try {
            Date expirationDate = getExpirationDateFromToken(token);
            if (expirationDate == null) {
                return true;
            }

            long currentTime = System.currentTimeMillis();
            long expirationTime = expirationDate.getTime();

            return (expirationTime - currentTime) <= threshold;
        } catch (Exception e) {
            log.error("检查令牌是否即将过期失败: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 获取令牌剩余有效时间
     *
     * @param token JWT令牌
     * @return 剩余时间（毫秒）
     */
    public long getRemainingValidityTime(String token) {
        try {
            Date expirationDate = getExpirationDateFromToken(token);
            if (expirationDate == null) {
                return 0;
            }

            long currentTime = System.currentTimeMillis();
            long expirationTime = expirationDate.getTime();

            return Math.max(0, expirationTime - currentTime);
        } catch (Exception e) {
            log.error("获取令牌剩余有效时间失败: {}", e.getMessage());
            return 0;
        }
    }

    // Getter方法
    public String getJwtHeader() {
        return jwtHeader;
    }

    public String getJwtPrefix() {
        return jwtPrefix;
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }
}