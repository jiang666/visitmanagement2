package com.proshine.visitmanagement.controller;

import com.proshine.visitmanagement.entity.School;
import com.proshine.visitmanagement.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 简单测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/simple-test")
@RequiredArgsConstructor
public class SimpleTestController {

    private final SchoolRepository schoolRepository;

    /**
     * 测试基础查询方法
     */
    @GetMapping("/basic/{keyword}")
    public String testBasic(@PathVariable String keyword) {
        log.info("测试关键词: [{}]", keyword);
        
        // 1. 测试标准JPA方法
        List<School> result1 = schoolRepository.findByNameContaining(keyword);
        log.info("findByNameContaining 结果: {}", result1.size());
        
        // 2. 测试不区分大小写的方法
        List<School> result2 = schoolRepository.findByNameContainingIgnoreCase(keyword);
        log.info("findByNameContainingIgnoreCase 结果: {}", result2.size());
        
        // 3. 直接检查特定记录
        School school13 = schoolRepository.findById(13L).orElse(null);
        School school62 = schoolRepository.findById(62L).orElse(null);
        
        StringBuilder result = new StringBuilder();
        result.append("关键词: ").append(keyword).append("\n");
        result.append("findByNameContaining: ").append(result1.size()).append("条\n");
        result.append("findByNameContainingIgnoreCase: ").append(result2.size()).append("条\n");
        result.append("学校13: ").append(school13 != null ? school13.getName() : "未找到").append("\n");
        result.append("学校62: ").append(school62 != null ? school62.getName() : "未找到").append("\n");
        
        if (school13 != null) {
            result.append("学校13包含'").append(keyword).append("': ").append(school13.getName().contains(keyword)).append("\n");
        }
        if (school62 != null) {
            result.append("学校62包含'").append(keyword).append("': ").append(school62.getName().contains(keyword)).append("\n");
        }
        
        // 列出找到的学校
        result.append("\n找到的学校:\n");
        result1.forEach(school -> result.append("- ").append(school.getName()).append("\n"));
        
        return result.toString();
    }
    
    /**
     * 获取所有包含指定字符的学校
     */
    @GetMapping("/all-containing/{keyword}")
    public String getAllContaining(@PathVariable String keyword) {
        List<School> allSchools = schoolRepository.findAll();
        log.info("数据库总学校数: {}", allSchools.size());
        
        List<School> containing = allSchools.stream()
            .filter(school -> school.getName() != null && school.getName().contains(keyword))
            .collect(Collectors.toList());
            
        StringBuilder result = new StringBuilder();
        result.append("数据库总学校数: ").append(allSchools.size()).append("\n");
        result.append("包含'").append(keyword).append("'的学校: ").append(containing.size()).append("条\n\n");
        
        containing.forEach(school -> 
            result.append("ID: ").append(school.getId())
                  .append(", Name: ").append(school.getName())
                  .append(", DeletedAt: ").append(school.getDeletedAt())
                  .append("\n"));
        
        return result.toString();
    }
}