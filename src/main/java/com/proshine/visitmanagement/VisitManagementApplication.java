package com.proshine.visitmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 客户拜访管理系统启动类
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class VisitManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisitManagementApplication.class, args);
        System.out.println("🚀 客户拜访管理系统启动成功！");
        System.out.println("💻 健康检查: http://localhost:8080/actuator/health");
    }
}
