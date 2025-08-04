package com.proshine.visitmanagement.controller;

import com.proshine.visitmanagement.entity.School;
import com.proshine.visitmanagement.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 调试控制器 - 专门用于排查搜索问题
 */
@Slf4j
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final SchoolRepository schoolRepository;
    private final EntityManager entityManager;

    /**
     * 调试搜索问题
     */
    @GetMapping("/search")
    public Object debugSearch(@RequestParam(defaultValue = "邮电") String keyword) {
        log.info("=== 开始调试搜索问题 ===");
        log.info("关键词: [{}]", keyword);
        log.info("关键词长度: {}", keyword.length());
        log.info("关键词字节: {}", java.util.Arrays.toString(keyword.getBytes(StandardCharsets.UTF_8)));
        
        // 1. 使用JPA Repository的不同方法
        log.info("1. 测试 findByNameContaining:");
        List<School> result1 = schoolRepository.findByNameContaining(keyword);
        log.info("结果数量: {}", result1.size());
        result1.forEach(s -> log.info("  - ID: {}, Name: [{}]", s.getId(), s.getName()));
        
        log.info("2. 测试 findByNameContainingIgnoreCase:");
        List<School> result2 = schoolRepository.findByNameContainingIgnoreCase(keyword);
        log.info("结果数量: {}", result2.size());
        result2.forEach(s -> log.info("  - ID: {}, Name: [{}]", s.getId(), s.getName()));
        
        // 2. 使用原生SQL查询
        log.info("3. 测试原生SQL查询:");
        Query nativeQuery = entityManager.createNativeQuery(
            "SELECT * FROM schools WHERE name LIKE ? AND deleted_at IS NULL", School.class);
        nativeQuery.setParameter(1, "%" + keyword + "%");
        @SuppressWarnings("unchecked")
        List<School> result3 = nativeQuery.getResultList();
        log.info("原生SQL结果数量: {}", result3.size());
        result3.forEach(s -> log.info("  - ID: {}, Name: [{}]", s.getId(), s.getName()));
        
        // 3. 使用JPQL查询
        log.info("4. 测试JPQL查询:");
        Query jpqlQuery = entityManager.createQuery(
            "SELECT s FROM School s WHERE s.name LIKE :keyword", School.class);
        jpqlQuery.setParameter("keyword", "%" + keyword + "%");
        @SuppressWarnings("unchecked")
        List<School> result4 = jpqlQuery.getResultList();
        log.info("JPQL结果数量: {}", result4.size());
        result4.forEach(s -> log.info("  - ID: {}, Name: [{}]", s.getId(), s.getName()));
        
        // 4. 查询所有学校并手动过滤
        log.info("5. 查询所有学校并手动过滤:");
        List<School> allSchools = schoolRepository.findAll();
        log.info("数据库总学校数: {}", allSchools.size());
        
        List<School> manualFilter = allSchools.stream()
            .filter(s -> s.getName() != null && s.getName().contains(keyword))
            .collect(java.util.stream.Collectors.toList());
        log.info("手动过滤结果数量: {}", manualFilter.size());
        manualFilter.forEach(s -> log.info("  - ID: {}, Name: [{}]", s.getId(), s.getName()));
        
        // 5. 检查具体的两条记录
        log.info("6. 检查具体的邮电相关学校:");
        School school13 = schoolRepository.findById(13L).orElse(null);
        School school62 = schoolRepository.findById(62L).orElse(null);
        
        if (school13 != null) {
            log.info("学校ID=13: [{}], 包含关键词: {}", school13.getName(), school13.getName().contains(keyword));
            log.info("学校名称字节: {}", java.util.Arrays.toString(school13.getName().getBytes(StandardCharsets.UTF_8)));
        } else {
            log.info("未找到ID=13的学校");
        }
        
        if (school62 != null) {
            log.info("学校ID=62: [{}], 包含关键词: {}", school62.getName(), school62.getName().contains(keyword));
            log.info("学校名称字节: {}", java.util.Arrays.toString(school62.getName().getBytes(StandardCharsets.UTF_8)));
        } else {
            log.info("未找到ID=62的学校");
        }
        
        // 返回调试结果
        java.util.Map<String, Object> debugResult = new java.util.HashMap<>();
        debugResult.put("keyword", keyword);
        debugResult.put("keywordBytes", java.util.Arrays.toString(keyword.getBytes(StandardCharsets.UTF_8)));
        debugResult.put("findByNameContaining", result1.size());
        debugResult.put("findByNameContainingIgnoreCase", result2.size());
        debugResult.put("nativeSQL", result3.size());
        debugResult.put("jpql", result4.size());
        debugResult.put("manualFilter", manualFilter.size());
        debugResult.put("school13", school13 != null ? school13.getName() : "not found");
        debugResult.put("school62", school62 != null ? school62.getName() : "not found");
        return debugResult;
    }
    
    /**
     * 测试不同编码的搜索
     */
    @GetMapping("/encoding-test")
    public Object testEncoding() {
        log.info("=== 编码测试 ===");
        
        String[] testKeywords = {
            "邮电",
            new String("邮电".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8),
            new String("邮电".getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1)
        };
        
        java.util.Map<String, Object> results = new java.util.HashMap<>();
        
        for (int i = 0; i < testKeywords.length; i++) {
            String keyword = testKeywords[i];
            log.info("测试关键词 {}: [{}], 字节: {}", i, keyword, java.util.Arrays.toString(keyword.getBytes(StandardCharsets.UTF_8)));
            
            List<School> result = schoolRepository.findByNameContaining(keyword);
            log.info("结果数量: {}", result.size());
            
            java.util.Map<String, Object> testResult = new java.util.HashMap<>();
            testResult.put("keyword", keyword);
            testResult.put("bytes", java.util.Arrays.toString(keyword.getBytes(StandardCharsets.UTF_8)));
            testResult.put("count", result.size());
            results.put("test" + i, testResult);
        }
        
        return results;
    }
}