package com.proshine.visitmanagement.config;

import com.proshine.visitmanagement.entity.User;
import com.proshine.visitmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes default data on application startup.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Ensure default admin user exists
        if (!userRepository.existsByUsername("admin")) {
            log.info("No admin user found, creating default admin account");
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRealName("超级管理员");
            admin.setRole(User.UserRole.ADMIN);
            admin.setDepartment("系统");
            admin.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(admin);
            log.info("Initialized default admin user");
        }
    }

}
