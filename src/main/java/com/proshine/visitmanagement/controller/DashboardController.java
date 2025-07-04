package com.proshine.visitmanagement.controller;

import com.proshine.visitmanagement.dto.response.ApiResponse;
import com.proshine.visitmanagement.service.DashboardService;
import com.proshine.visitmanagement.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘控制器
 * 提供系统概览、统计图表、数据分析等功能
 *
 * @author System
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Validated
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取仪表盘概览数据
     *
     * @param authentication 认证信息
     * @return 仪表盘概览数据
     */
    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> getDashboardOverview(Authentication authentication) {
        log.debug("获取仪表盘概览数据");

        Map<String, Object> overview = dashboardService.getDashboardOverview(authentication);

        return ApiResponse.success(overview, "获取仪表盘概览成功");
    }

    /**
     * 获取拜访统计数据
     *
     * @param period 统计周期（today/week/month/quarter/year）
     * @param startDate 开始日期（可选，优先级高于period）
     * @param endDate 结束日期（可选，优先级高于period）
     * @param salesId 销售人员ID（可选，管理员可查看所有人）
     * @param authentication 认证信息
     * @return 拜访统计数据
     */
    @GetMapping("/visit-statistics")
    public ApiResponse<Map<String, Object>> getVisitStatistics(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Long salesId,
            Authentication authentication) {

        log.debug("获取拜访统计: period={}, startDate={}, endDate={}, salesId={}",
                period, startDate, endDate, salesId);

        // 验证统计周期
        List<String> validPeriods = Arrays.asList("today", "week", "month", "quarter", "year", "custom");
        if (!validPeriods.contains(period)) {
            return ApiResponse.error("统计周期参数错误，支持：" + String.join("、", validPeriods));
        }

        // 验证自定义日期范围
        if ("custom".equals(period)) {
            if (startDate == null || endDate == null) {
                return ApiResponse.error("自定义时间段需要提供开始日期和结束日期");
            }
            if (startDate.isAfter(endDate)) {
                return ApiResponse.error("开始日期不能晚于结束日期");
            }
        }

        Map<String, Object> statistics = dashboardService.getVisitStatistics(
                period, startDate, endDate, salesId, authentication);

        return ApiResponse.success(statistics, "获取拜访统计成功");
    }

    /**
     * 获取销售业绩统计
     *
     * @param period 统计周期
     * @param year 年份（可选）
     * @param month 月份（可选）
     * @param authentication 认证信息
     * @return 销售业绩统计
     */
    @GetMapping("/sales-performance")
    public ApiResponse<Map<String, Object>> getSalesPerformance(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Authentication authentication) {

        log.debug("获取销售业绩统计: period={}, year={}, month={}", period, year, month);

        // 验证统计周期
        List<String> validPeriods = Arrays.asList("month", "quarter", "year");
        if (!validPeriods.contains(period)) {
            return ApiResponse.error("统计周期参数错误，支持：" + String.join("、", validPeriods));
        }

        Map<String, Object> performance = dashboardService.getSalesPerformance(
                period, year, month, authentication);

        return ApiResponse.success(performance, "获取销售业绩统计成功");
    }

    /**
     * 获取客户分析数据
     *
     * @param dimension 分析维度（school/department/region/type）
     * @param authentication 认证信息
     * @return 客户分析数据
     */
    @GetMapping("/customer-analysis")
    public ApiResponse<Map<String, Object>> getCustomerAnalysis(
            @RequestParam(defaultValue = "school") String dimension,
            Authentication authentication) {

        log.debug("获取客户分析数据: dimension={}", dimension);

        // 验证分析维度
        List<String> validDimensions = Arrays.asList("school", "department", "region", "type");
        if (!validDimensions.contains(dimension)) {
            return ApiResponse.error("分析维度参数错误，支持：" + String.join("、", validDimensions));
        }

        Map<String, Object> analysis = dashboardService.getCustomerAnalysis(dimension, authentication);

        return ApiResponse.success(analysis, "获取客户分析数据成功");
    }

    /**
     * 获取趋势分析数据
     *
     * @param metric 指标类型（visits/customers/performance）
     * @param period 时间粒度（day/week/month）
     * @param duration 持续时长（天数）
     * @param authentication 认证信息
     * @return 趋势分析数据
     */
    @GetMapping("/trend-analysis")
    public ApiResponse<Map<String, Object>> getTrendAnalysis(
            @RequestParam(defaultValue = "visits") String metric,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "30") Integer duration,
            Authentication authentication) {

        log.debug("获取趋势分析: metric={}, period={}, duration={}", metric, period, duration);

        // 验证指标类型
        List<String> validMetrics = Arrays.asList("visits", "customers", "performance");
        if (!validMetrics.contains(metric)) {
            return ApiResponse.error("指标类型参数错误，支持：" + String.join("、", validMetrics));
        }

        // 验证时间粒度
        List<String> validPeriods = Arrays.asList("day", "week", "month");
        if (!validPeriods.contains(period)) {
            return ApiResponse.error("时间粒度参数错误，支持：" + String.join("、", validPeriods));
        }

        // 验证持续时长
        ValidationUtils.min(duration, 1, "duration");
        ValidationUtils.max(duration, 365, "duration");

        Map<String, Object> trend = dashboardService.getTrendAnalysis(
                metric, period, duration, authentication);

        return ApiResponse.success(trend, "获取趋势分析数据成功");
    }

    /**
     * 获取今日工作概要
     *
     * @param authentication 认证信息
     * @return 今日工作概要
     */
    @GetMapping("/today-summary")
    public ApiResponse<Map<String, Object>> getTodaySummary(Authentication authentication) {
        log.debug("获取今日工作概要");

        Map<String, Object> todaySummary = dashboardService.getTodaySummary(authentication);

        return ApiResponse.success(todaySummary, "获取今日概要成功");
    }

    /**
     * 获取待办事项
     *
     * @param limit 返回数量限制
     * @param authentication 认证信息
     * @return 待办事项列表
     */
    @GetMapping("/todos")
    public ApiResponse<Map<String, Object>> getTodos(
            @RequestParam(defaultValue = "10") Integer limit,
            Authentication authentication) {

        log.debug("获取待办事项: limit={}", limit);

        ValidationUtils.min(limit, 1, "limit");
        ValidationUtils.max(limit, 100, "limit");

        Map<String, Object> todos = dashboardService.getTodos(limit, authentication);

        return ApiResponse.success(todos, "获取待办事项成功");
    }

    /**
     * 获取最近活动
     *
     * @param days 查询天数
     * @param limit 返回数量限制
     * @param authentication 认证信息
     * @return 最近活动列表
     */
    @GetMapping("/recent-activities")
    public ApiResponse<Map<String, Object>> getRecentActivities(
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "20") Integer limit,
            Authentication authentication) {

        log.debug("获取最近活动: days={}, limit={}", days, limit);

        ValidationUtils.min(days, 1, "days");
        ValidationUtils.max(days, 90, "days");
        ValidationUtils.min(limit, 1, "limit");
        ValidationUtils.max(limit, 100, "limit");

        Map<String, Object> activities = dashboardService.getRecentActivities(days, limit, authentication);

        return ApiResponse.success(activities, "获取最近活动成功");
    }

    /**
     * 获取排行榜数据
     *
     * @param type 排行榜类型（sales/customers/schools）
     * @param period 统计周期
     * @param limit 返回数量限制
     * @param authentication 认证信息
     * @return 排行榜数据
     */
    @GetMapping("/rankings")
    public ApiResponse<Map<String, Object>> getRankings(
            @RequestParam(defaultValue = "sales") String type,
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "10") Integer limit,
            Authentication authentication) {

        log.debug("获取排行榜: type={}, period={}, limit={}", type, period, limit);

        // 验证排行榜类型
        List<String> validTypes = Arrays.asList("sales", "customers", "schools");
        if (!validTypes.contains(type)) {
            return ApiResponse.error("排行榜类型参数错误，支持：" + String.join("、", validTypes));
        }

        // 验证统计周期
        List<String> validPeriods = Arrays.asList("week", "month", "quarter", "year");
        if (!validPeriods.contains(period)) {
            return ApiResponse.error("统计周期参数错误，支持：" + String.join("、", validPeriods));
        }

        ValidationUtils.min(limit, 1, "limit");
        ValidationUtils.max(limit, 50, "limit");

        Map<String, Object> rankings = dashboardService.getRankings(type, period, limit, authentication);

        return ApiResponse.success(rankings, "获取排行榜数据成功");
    }

    /**
     * 获取地理分布数据
     *
     * @param level 地理级别（province/city）
     * @param authentication 认证信息
     * @return 地理分布数据
     */
    @GetMapping("/geographic-distribution")
    public ApiResponse<Map<String, Object>> getGeographicDistribution(
            @RequestParam(defaultValue = "province") String level,
            Authentication authentication) {

        log.debug("获取地理分布数据: level={}", level);

        // 验证地理级别
        List<String> validLevels = Arrays.asList("province", "city");
        if (!validLevels.contains(level)) {
            return ApiResponse.error("地理级别参数错误，支持：" + String.join("、", validLevels));
        }

        Map<String, Object> distribution = dashboardService.getGeographicDistribution(level, authentication);

        return ApiResponse.success(distribution, "获取地理分布数据成功");
    }

    /**
     * 获取实时数据
     *
     * @param authentication 认证信息
     * @return 实时数据
     */
    @GetMapping("/realtime")
    public ApiResponse<Map<String, Object>> getRealtimeData(Authentication authentication) {
        log.debug("获取实时数据");

        Map<String, Object> realtimeData = dashboardService.getRealtimeData(authentication);

        return ApiResponse.success(realtimeData, "获取实时数据成功");
    }

    /**
     * 获取个性化推荐
     *
     * @param authentication 认证信息
     * @return 个性化推荐
     */
    @GetMapping("/recommendations")
    public ApiResponse<Map<String, Object>> getRecommendations(Authentication authentication) {
        log.debug("获取个性化推荐");

        Map<String, Object> recommendations = dashboardService.getRecommendations(authentication);

        return ApiResponse.success(recommendations, "获取个性化推荐成功");
    }

    /**
     * 刷新仪表盘缓存
     *
     * @param authentication 认证信息
     * @return 刷新结果
     */
    @PostMapping("/refresh")
    public ApiResponse<Void> refreshDashboard(Authentication authentication) {
        log.info("刷新仪表盘缓存");

        dashboardService.refreshCache(authentication);

        return ApiResponse.success("仪表盘缓存刷新成功");
    }
}