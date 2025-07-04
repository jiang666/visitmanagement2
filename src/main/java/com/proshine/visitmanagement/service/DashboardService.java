package com.proshine.visitmanagement.service;

import com.proshine.visitmanagement.entity.Customer;
import com.proshine.visitmanagement.entity.School;
import com.proshine.visitmanagement.entity.User;
import com.proshine.visitmanagement.entity.VisitRecord;
import com.proshine.visitmanagement.exception.ResourceNotFoundException;
import com.proshine.visitmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务类
 *
 * @author System
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final VisitRecordRepository visitRecordRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;

    /**
     * 获取仪表盘概览数据
     *
     * @param authentication 认证信息
     * @return 概览数据
     */
    public Map<String, Object> getDashboardOverview(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Map<String, Object> overview = new HashMap<>();

        try {
            // 基础统计数据
            Map<String, Object> basicStats = getBasicStatistics(currentUser);
            overview.put("basicStats", basicStats);

            // 今日数据
            Map<String, Object> todayStats = getTodayStatistics(currentUser);
            overview.put("todayStats", todayStats);

            // 本月数据
            Map<String, Object> monthStats = getMonthStatistics(currentUser);
            overview.put("monthStats", monthStats);

            // 趋势数据
            Map<String, Object> trendData = getRecentTrend(currentUser);
            overview.put("trendData", trendData);

        } catch (Exception e) {
            log.error("获取仪表盘概览数据失败", e);
            overview.put("error", "获取数据失败");
        }

        return overview;
    }

    /**
     * 获取拜访统计数据
     *
     * @param period 统计周期
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param salesId 销售人员ID
     * @param authentication 认证信息
     * @return 拜访统计数据
     */
    public Map<String, Object> getVisitStatistics(String period, LocalDate startDate, LocalDate endDate,
                                                  Long salesId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Map<String, Object> statistics = new HashMap<>();

        try {
            // 确定查询的销售人员
            Long targetSalesId = determineSalesId(currentUser, salesId);

            // 计算日期范围
            LocalDate[] dateRange = calculateDateRange(period, startDate, endDate);
            LocalDate queryStartDate = dateRange[0];
            LocalDate queryEndDate = dateRange[1];

            // 获取拜访统计
            Map<String, Object> visitStats = getVisitStatsByDateRange(targetSalesId, queryStartDate, queryEndDate);
            statistics.put("visitStats", visitStats);

            // 获取拜访趋势
            List<Map<String, Object>> visitTrend = getVisitTrendByDateRange(targetSalesId, queryStartDate, queryEndDate);
            statistics.put("visitTrend", visitTrend);

            // 获取客户统计
            Map<String, Object> customerStats = getCustomerStatsByDateRange(targetSalesId, queryStartDate, queryEndDate);
            statistics.put("customerStats", customerStats);

        } catch (Exception e) {
            log.error("获取拜访统计数据失败", e);
            statistics.put("error", "获取统计数据失败");
        }

        return statistics;
    }

    /**
     * 获取销售业绩统计
     *
     * @param period 统计周期
     * @param year 年份
     * @param month 月份
     * @param authentication 认证信息
     * @return 销售业绩统计
     */
    public Map<String, Object> getSalesPerformance(String period, Integer year, Integer month,
                                                   Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Map<String, Object> performance = new HashMap<>();

        try {
            // 默认使用当前年月
            int targetYear = year != null ? year : LocalDate.now().getYear();
            int targetMonth = month != null ? month : LocalDate.now().getMonthValue();

            switch (period) {
                case "month":
                    performance = getMonthlyPerformance(currentUser, targetYear, targetMonth);
                    break;
                case "quarter":
                    int quarter = (targetMonth - 1) / 3 + 1;
                    performance = getQuarterlyPerformance(currentUser, targetYear, quarter);
                    break;
                case "year":
                    performance = getYearlyPerformance(currentUser, targetYear);
                    break;
                default:
                    performance.put("error", "不支持的统计周期");
            }

        } catch (Exception e) {
            log.error("获取销售业绩统计失败", e);
            performance.put("error", "获取业绩统计失败");
        }

        return performance;
    }

    /**
     * 获取客户分析数据
     *
     * @param dimension 分析维度
     * @param authentication 认证信息
     * @return 客户分析数据
     */
    public Map<String, Object> getCustomerAnalysis(String dimension, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Map<String, Object> analysis = new HashMap<>();

        try {
            switch (dimension) {
                case "school":
                    analysis = getCustomerAnalysisBySchool(currentUser);
                    break;
                case "department":
                    analysis = getCustomerAnalysisByDepartment(currentUser);
                    break;
                case "region":
                    analysis = getCustomerAnalysisByRegion(currentUser);
                    break;
                case "type":
                    analysis = getCustomerAnalysisByType(currentUser);
                    break;
                default:
                    analysis.put("error", "不支持的分析维度");
            }

        } catch (Exception e) {
            log.error("获取客户分析数据失败", e);
            analysis.put("error", "获取分析数据失败");
        }

        return analysis;
    }

    /**
     * 获取趋势分析数据
     *
     * @param metric 指标类型
     * @param period 统计周期
     * @param duration 时长
     * @param authentication 认证信息
     * @return 趋势分析数据
     */
    public Map<String, Object> getTrendAnalysis(String metric, String period, Integer duration,
                                                Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Map<String, Object> trend = new HashMap<>();

        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(duration - 1);

            switch (metric) {
                case "visits":
                    trend = getVisitTrendAnalysis(currentUser, startDate, endDate, period);
                    break;
                case "customers":
                    trend = getCustomerTrendAnalysis(currentUser, startDate, endDate, period);
                    break;
                case "performance":
                    trend = getPerformanceTrendAnalysis(currentUser, startDate, endDate, period);
                    break;
                default:
                    trend.put("error", "不支持的指标类型");
            }

        } catch (Exception e) {
            log.error("获取趋势分析数据失败", e);
            trend.put("error", "获取趋势分析失败");
        }

        return trend;
    }

    /**
     * 获取排行榜数据
     *
     * @param type 排行类型
     * @param period 统计周期
     * @param limit 数量限制
     * @param authentication 认证信息
     * @return 排行榜数据
     */
    public Map<String, Object> getRankings(String type, String period, Integer limit, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Map<String, Object> rankings = new HashMap<>();

        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = calculateStartDate(period, endDate);

            switch (type) {
                case "sales":
                    rankings = getSalesRankings(startDate, endDate, limit);
                    break;
                case "customer":
                    rankings = getCustomerRankings(startDate, endDate, limit);
                    break;
                case "school":
                    rankings = getSchoolRankings(startDate, endDate, limit);
                    break;
                default:
                    rankings.put("error", "不支持的排行类型");
            }

        } catch (Exception e) {
            log.error("获取排行榜数据失败", e);
            rankings.put("error", "获取排行榜失败");
        }

        return rankings;
    }

    /**
     * 获取地理分布数据
     *
     * @param level 地理级别
     * @param authentication 认证信息
     * @return 地理分布数据
     */
    public Map<String, Object> getGeographicDistribution(String level, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Map<String, Object> distribution = new HashMap<>();

        try {
            switch (level) {
                case "province":
                    distribution = getProvinceDistribution(currentUser);
                    break;
                case "city":
                    distribution = getCityDistribution(currentUser);
                    break;
                default:
                    distribution.put("error", "不支持的地理级别");
            }

        } catch (Exception e) {
            log.error("获取地理分布数据失败", e);
            distribution.put("error", "获取地理分布失败");
        }

        return distribution;
    }

    /**
     * 获取实时数据
     *
     * @param authentication 认证信息
     * @return 实时数据
     */
    public Map<String, Object> getRealtimeData(Authentication authentication) {
        Map<String, Object> realtimeData = new HashMap<>();

        try {
            // 当前在线用户数
            realtimeData.put("onlineUsers", getCurrentOnlineUsers());

            // 今日拜访数
            User currentUser = getCurrentUser(authentication);
            realtimeData.put("todayVisits", getTodayVisitCount(currentUser));

            // 今日新增客户数
            realtimeData.put("todayNewCustomers", getTodayNewCustomerCount(currentUser));

            // 系统状态
            realtimeData.put("systemStatus", "正常");

        } catch (Exception e) {
            log.error("获取实时数据失败", e);
            realtimeData.put("error", "获取实时数据失败");
        }

        return realtimeData;
    }

    /**
     * 获取个性化推荐
     *
     * @param authentication 认证信息
     * @return 个性化推荐
     */
    public Map<String, Object> getRecommendations(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Map<String, Object> recommendations = new HashMap<>();

        try {
            // 推荐拜访的客户
            List<Customer> recommendedCustomers = getRecommendedCustomers(currentUser);
            recommendations.put("customers", recommendedCustomers);

            // 推荐关注的学校
            List<School> recommendedSchools = getRecommendedSchools(currentUser);
            recommendations.put("schools", recommendedSchools);

            // 工作建议
            List<String> workSuggestions = getWorkSuggestions(currentUser);
            recommendations.put("suggestions", workSuggestions);

        } catch (Exception e) {
            log.error("获取个性化推荐失败", e);
            recommendations.put("error", "获取推荐失败");
        }

        return recommendations;
    }

    /**
     * 刷新缓存
     *
     * @param authentication 认证信息
     */
    @Transactional
    public void refreshCache(Authentication authentication) {
        try {
            log.info("刷新仪表盘缓存");
            // 这里可以添加具体的缓存刷新逻辑
        } catch (Exception e) {
            log.error("刷新缓存失败", e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 获取当前用户
     */
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }

    /**
     * 获取基础统计数据
     */
    private Map<String, Object> getBasicStatistics(User currentUser) {
        Map<String, Object> stats = new HashMap<>();

        if (currentUser.getRole() == User.UserRole.SALES) {
            // 销售人员的统计数据
            stats.put("totalVisits", visitRecordRepository.countBySalesId(currentUser.getId()));
            stats.put("totalCustomers", customerRepository.countByCreatedById(currentUser.getId()));
        } else {
            // 管理员的统计数据
            stats.put("totalVisits", visitRecordRepository.count());
            stats.put("totalCustomers", customerRepository.count());
            stats.put("totalSchools", schoolRepository.count());
        }

        return stats;
    }

    /**
     * 获取今日统计数据
     */
    private Map<String, Object> getTodayStatistics(User currentUser) {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        if (currentUser.getRole() == User.UserRole.SALES) {
            // 使用正确的Repository方法
            List<VisitRecord> todayVisits = visitRecordRepository.findBySalesIdAndVisitDate(currentUser.getId(), today);
            stats.put("todayVisits", todayVisits.size());
            stats.put("todayNewCustomers", customerRepository.countBySalesIdAndCreatedAtAfter(currentUser.getId(), today.atStartOfDay()));
        } else {
            List<VisitRecord> todayVisits = visitRecordRepository.findByVisitDate(today);
            stats.put("todayVisits", todayVisits.size());
            stats.put("todayNewCustomers", customerRepository.countByCreatedAtAfter(today.atStartOfDay()));
        }

        return stats;
    }

    /**
     * 获取本月统计数据
     */
    private Map<String, Object> getMonthStatistics(User currentUser) {
        Map<String, Object> stats = new HashMap<>();
        LocalDate monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthEnd = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        if (currentUser.getRole() == User.UserRole.SALES) {
            // 使用正确的Repository方法
            stats.put("monthVisits", visitRecordRepository.countBySalesIdAndVisitDateBetween(
                    currentUser.getId(), monthStart, monthEnd));
            stats.put("monthNewCustomers", customerRepository.countBySalesIdAndCreatedAtBetween(
                    currentUser.getId(), monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59)));
        } else {
            stats.put("monthVisits", visitRecordRepository.countByVisitDateBetween(monthStart, monthEnd));
            stats.put("monthNewCustomers", customerRepository.countByCreatedAtBetween(
                    monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59)));
        }

        return stats;
    }

    /**
     * 获取最近趋势数据
     */
    private Map<String, Object> getRecentTrend(User currentUser) {
        Map<String, Object> trend = new HashMap<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        // 获取最近30天的拜访趋势
        List<Map<String, Object>> visitTrend = getVisitTrendByDateRange(
                currentUser.getRole() == User.UserRole.SALES ? currentUser.getId() : null,
                startDate, endDate);

        trend.put("visitTrend", visitTrend);
        trend.put("period", "最近30天");

        return trend;
    }

    /**
     * 确定销售人员ID
     */
    private Long determineSalesId(User currentUser, Long requestedSalesId) {
        if (currentUser.getRole() == User.UserRole.SALES) {
            return currentUser.getId();
        } else {
            return requestedSalesId;
        }
    }

    /**
     * 计算日期范围
     */
    private LocalDate[] calculateDateRange(String period, LocalDate startDate, LocalDate endDate) {
        LocalDate queryStartDate = startDate;
        LocalDate queryEndDate = endDate;

        if (queryStartDate == null || queryEndDate == null) {
            LocalDate now = LocalDate.now();
            switch (period) {
                case "today":
                    queryStartDate = queryEndDate = now;
                    break;
                case "week":
                    queryStartDate = now.minusDays(7);
                    queryEndDate = now;
                    break;
                case "month":
                    queryStartDate = now.with(TemporalAdjusters.firstDayOfMonth());
                    queryEndDate = now.with(TemporalAdjusters.lastDayOfMonth());
                    break;
                case "quarter":
                    int quarter = (now.getMonthValue() - 1) / 3;
                    queryStartDate = LocalDate.of(now.getYear(), quarter * 3 + 1, 1);
                    queryEndDate = queryStartDate.plusMonths(3).minusDays(1);
                    break;
                case "year":
                    queryStartDate = LocalDate.of(now.getYear(), 1, 1);
                    queryEndDate = LocalDate.of(now.getYear(), 12, 31);
                    break;
                default:
                    queryStartDate = now.minusDays(30);
                    queryEndDate = now;
            }
        }

        return new LocalDate[]{queryStartDate, queryEndDate};
    }

    /**
     * 获取指定日期范围的拜访统计
     */
    private Map<String, Object> getVisitStatsByDateRange(Long salesId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        List<VisitRecord> visits = (salesId != null) ?
                visitRecordRepository.findBySalesIdAndVisitDateBetweenOrderByVisitDateDesc(salesId, startDate, endDate) :
                visitRecordRepository.findByVisitDateBetween(startDate, endDate);

        stats.put("totalVisits", visits.size());
        stats.put("completedVisits", visits.stream().filter(v -> v.getStatus() == VisitRecord.VisitStatus.COMPLETED).count());
        stats.put("scheduledVisits", visits.stream().filter(v -> v.getStatus() == VisitRecord.VisitStatus.SCHEDULED).count());

        return stats;
    }

    /**
     * 获取指定日期范围的拜访趋势
     */
    private List<Map<String, Object>> getVisitTrendByDateRange(Long salesId, LocalDate startDate, LocalDate endDate) {
        List<VisitRecord> visits = (salesId != null) ?
                visitRecordRepository.findBySalesIdAndVisitDateBetweenOrderByVisitDateDesc(salesId, startDate, endDate) :
                visitRecordRepository.findByVisitDateBetween(startDate, endDate);

        // 按日期分组统计
        Map<LocalDate, Long> visitsByDate = visits.stream()
                .collect(Collectors.groupingBy(
                        VisitRecord::getVisitDate,
                        Collectors.counting()
                ));

        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", currentDate.toString());
            item.put("count", visitsByDate.getOrDefault(currentDate, 0L));
            trendData.add(item);
            currentDate = currentDate.plusDays(1);
        }

        return trendData;
    }

    /**
     * 获取指定日期范围的客户统计
     */
    private Map<String, Object> getCustomerStatsByDateRange(Long salesId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Customer> customers = (salesId != null) ?
                customerRepository.findByCreatedByAndCreatedAtBetween(salesId, startDateTime, endDateTime) :
                customerRepository.findByCreatedAtBetween(startDateTime, endDateTime);

        stats.put("totalCustomers", customers.size());
        stats.put("activeCustomers", customers.stream().filter(c -> c.getStatus() == Customer.CustomerStatus.ACTIVE).count());
        stats.put("inactiveCustomers", customers.stream().filter(c -> c.getStatus() == Customer.CustomerStatus.INACTIVE).count());

        return stats;
    }

    // ===================== 其他业务方法（简化实现） =====================

    private Map<String, Object> getMonthlyPerformance(User currentUser, int year, int month) {
        Map<String, Object> performance = new HashMap<>();
        performance.put("visitCount", 0);
        performance.put("customerCount", 0);
        performance.put("completionRate", 0.0);
        return performance;
    }

    private Map<String, Object> getQuarterlyPerformance(User currentUser, int year, int quarter) {
        Map<String, Object> performance = new HashMap<>();
        performance.put("visitCount", 0);
        performance.put("customerCount", 0);
        performance.put("completionRate", 0.0);
        return performance;
    }

    private Map<String, Object> getYearlyPerformance(User currentUser, int year) {
        Map<String, Object> performance = new HashMap<>();
        performance.put("visitCount", 0);
        performance.put("customerCount", 0);
        performance.put("completionRate", 0.0);
        return performance;
    }

    private Map<String, Object> getCustomerAnalysisBySchool(User currentUser) {
        Map<String, Object> analysis = new HashMap<>();

        List<Customer> customers = currentUser.getRole() == User.UserRole.SALES ?
                customerRepository.findByCreatedById(currentUser.getId()) :
                customerRepository.findAll();

        Map<String, Long> customersBySchool = customers.stream()
                .collect(Collectors.groupingBy(
                        customer -> customer.getDepartment() != null && customer.getDepartment().getSchool() != null ?
                                customer.getDepartment().getSchool().getName() : "未分类",
                        Collectors.counting()
                ));

        analysis.put("distribution", customersBySchool);
        analysis.put("total", customers.size());

        return analysis;
    }

    private Map<String, Object> getCustomerAnalysisByDepartment(User currentUser) {
        Map<String, Object> analysis = new HashMap<>();

        List<Customer> customers = currentUser.getRole() == User.UserRole.SALES ?
                customerRepository.findByCreatedById(currentUser.getId()) :
                customerRepository.findAll();

        Map<String, Long> customersByDepartment = customers.stream()
                .collect(Collectors.groupingBy(
                        customer -> customer.getDepartment() != null ? customer.getDepartment().getName() : "未分类",
                        Collectors.counting()
                ));

        analysis.put("distribution", customersByDepartment);
        analysis.put("total", customers.size());

        return analysis;
    }

    private Map<String, Object> getCustomerAnalysisByRegion(User currentUser) {
        Map<String, Object> analysis = new HashMap<>();

        List<Customer> customers = currentUser.getRole() == User.UserRole.SALES ?
                customerRepository.findByCreatedById(currentUser.getId()) :
                customerRepository.findAll();

        Map<String, Long> customersByRegion = customers.stream()
                .collect(Collectors.groupingBy(
                        customer -> customer.getDepartment() != null && customer.getDepartment().getSchool() != null ?
                                customer.getDepartment().getSchool().getCity() : "未分类",
                        Collectors.counting()
                ));

        analysis.put("distribution", customersByRegion);
        analysis.put("total", customers.size());

        return analysis;
    }

    private Map<String, Object> getCustomerAnalysisByType(User currentUser) {
        Map<String, Object> analysis = new HashMap<>();

        List<Customer> customers = currentUser.getRole() == User.UserRole.SALES ?
                customerRepository.findByCreatedById(currentUser.getId()) :
                customerRepository.findAll();

        Map<String, Long> customersByStatus = customers.stream()
                .collect(Collectors.groupingBy(
                        customer -> customer.getStatus() != null ? customer.getStatus().toString() : "未分类",
                        Collectors.counting()
                ));

        analysis.put("distribution", customersByStatus);
        analysis.put("total", customers.size());

        return analysis;
    }

    private Map<String, Object> getVisitTrendAnalysis(User currentUser, LocalDate startDate, LocalDate endDate, String period) {
        Map<String, Object> trend = new HashMap<>();

        List<VisitRecord> visits = currentUser.getRole() == User.UserRole.SALES ?
                visitRecordRepository.findBySalesIdAndVisitDateBetweenOrderByVisitDateDesc(currentUser.getId(), startDate, endDate) :
                visitRecordRepository.findByVisitDateBetween(startDate, endDate);

        // 按日期分组统计
        Map<LocalDate, Long> visitsByDate = visits.stream()
                .collect(Collectors.groupingBy(
                        VisitRecord::getVisitDate,
                        Collectors.counting()
                ));

        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", currentDate);
            item.put("count", visitsByDate.getOrDefault(currentDate, 0L));
            trendData.add(item);
            currentDate = currentDate.plusDays(1);
        }

        trend.put("data", trendData);
        trend.put("total", visits.size());

        return trend;
    }

    private Map<String, Object> getCustomerTrendAnalysis(User currentUser, LocalDate startDate, LocalDate endDate, String period) {
        Map<String, Object> trend = new HashMap<>();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Customer> customers = currentUser.getRole() == User.UserRole.SALES ?
                customerRepository.findByCreatedByAndCreatedAtBetween(currentUser.getId(), startDateTime, endDateTime) :
                customerRepository.findByCreatedAtBetween(startDateTime, endDateTime);

        // 按日期分组统计
        Map<LocalDate, Long> customersByDate = customers.stream()
                .collect(Collectors.groupingBy(
                        customer -> customer.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", currentDate);
            item.put("count", customersByDate.getOrDefault(currentDate, 0L));
            trendData.add(item);
            currentDate = currentDate.plusDays(1);
        }

        trend.put("data", trendData);
        trend.put("total", customers.size());

        return trend;
    }

    private Map<String, Object> getPerformanceTrendAnalysis(User currentUser, LocalDate startDate, LocalDate endDate, String period) {
        Map<String, Object> trend = new HashMap<>();
        // 简化实现
        trend.put("data", new ArrayList<>());
        trend.put("total", 0);
        return trend;
    }

    private LocalDate calculateStartDate(String period, LocalDate endDate) {
        switch (period) {
            case "week":
                return endDate.minusDays(7);
            case "month":
                return endDate.minusDays(30);
            case "quarter":
                return endDate.minusDays(90);
            case "year":
                return endDate.minusDays(365);
            default:
                return endDate.minusDays(30);
        }
    }

    private Map<String, Object> getSalesRankings(LocalDate startDate, LocalDate endDate, Integer limit) {
        Map<String, Object> rankings = new HashMap<>();

        // 获取销售人员拜访数量排名
        List<VisitRecord> visits = visitRecordRepository.findByVisitDateBetween(startDate, endDate);
        Map<String, Long> salesVisitCount = visits.stream()
                .collect(Collectors.groupingBy(
                        visit -> visit.getSales().getRealName(),
                        Collectors.counting()
                ));

        List<Map<String, Object>> rankingList = salesVisitCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        rankings.put("data", rankingList);
        rankings.put("total", salesVisitCount.size());

        return rankings;
    }

    private Map<String, Object> getCustomerRankings(LocalDate startDate, LocalDate endDate, Integer limit) {
        Map<String, Object> rankings = new HashMap<>();

        // 获取拜访频次最高的客户
        List<VisitRecord> visits = visitRecordRepository.findByVisitDateBetween(startDate, endDate);
        Map<String, Long> customerVisitCount = visits.stream()
                .collect(Collectors.groupingBy(
                        visit -> visit.getCustomer().getName(),
                        Collectors.counting()
                ));

        List<Map<String, Object>> rankingList = customerVisitCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        rankings.put("data", rankingList);
        rankings.put("total", customerVisitCount.size());

        return rankings;
    }

    private Map<String, Object> getSchoolRankings(LocalDate startDate, LocalDate endDate, Integer limit) {
        Map<String, Object> rankings = new HashMap<>();

        // 获取学校客户数量排名
        List<Customer> customers = customerRepository.findAll();
        Map<String, Long> schoolCustomerCount = customers.stream()
                .collect(Collectors.groupingBy(
                        customer -> customer.getDepartment() != null && customer.getDepartment().getSchool() != null ?
                                customer.getDepartment().getSchool().getName() : "未分类",
                        Collectors.counting()
                ));

        List<Map<String, Object>> rankingList = schoolCustomerCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        rankings.put("data", rankingList);
        rankings.put("total", schoolCustomerCount.size());

        return rankings;
    }

    private Map<String, Object> getProvinceDistribution(User currentUser) {
        Map<String, Object> distribution = new HashMap<>();

        List<Customer> customers = currentUser.getRole() == User.UserRole.SALES ?
                customerRepository.findByCreatedById(currentUser.getId()) :
                customerRepository.findAll();

        Map<String, Long> provinceDistribution = customers.stream()
                .collect(Collectors.groupingBy(
                        customer -> customer.getDepartment() != null &&
                                customer.getDepartment().getSchool() != null &&
                                customer.getDepartment().getSchool().getProvince() != null ?
                                customer.getDepartment().getSchool().getProvince() : "未分类",
                        Collectors.counting()
                ));

        distribution.put("data", provinceDistribution);
        distribution.put("total", customers.size());

        return distribution;
    }

    private Map<String, Object> getCityDistribution(User currentUser) {
        Map<String, Object> distribution = new HashMap<>();

        List<Customer> customers = currentUser.getRole() == User.UserRole.SALES ?
                customerRepository.findByCreatedById(currentUser.getId()) :
                customerRepository.findAll();

        Map<String, Long> cityDistribution = customers.stream()
                .collect(Collectors.groupingBy(
                        customer -> customer.getDepartment() != null &&
                                customer.getDepartment().getSchool() != null &&
                                customer.getDepartment().getSchool().getCity() != null ?
                                customer.getDepartment().getSchool().getCity() : "未分类",
                        Collectors.counting()
                ));

        distribution.put("data", cityDistribution);
        distribution.put("total", customers.size());

        return distribution;
    }

    private Long getCurrentOnlineUsers() {
        // 简化实现，实际应从Session或缓存中获取
        return 10L;
    }

    private Long getTodayVisitCount(User currentUser) {
        LocalDate today = LocalDate.now();
        return currentUser.getRole() == User.UserRole.SALES ?
                (long) visitRecordRepository.findBySalesIdAndVisitDate(currentUser.getId(), today).size() :
                (long) visitRecordRepository.findByVisitDate(today).size();
    }

    private Long getTodayNewCustomerCount(User currentUser) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();

        return currentUser.getRole() == User.UserRole.SALES ?
                customerRepository.countBySalesIdAndCreatedAtAfter(currentUser.getId(), startOfDay) :
                customerRepository.countByCreatedAtAfter(startOfDay);
    }

    private List<Customer> getRecommendedCustomers(User currentUser) {
        // 简化实现，实际应根据算法推荐
        return currentUser.getRole() == User.UserRole.SALES ?
                customerRepository.findByCreatedById(currentUser.getId()).stream().limit(5).collect(Collectors.toList()) :
                customerRepository.findAll().stream().limit(5).collect(Collectors.toList());
    }

    private List<School> getRecommendedSchools(User currentUser) {
        // 简化实现，实际应根据算法推荐
        return schoolRepository.findAll().stream().limit(5).collect(Collectors.toList());
    }

    private List<String> getWorkSuggestions(User currentUser) {
        return null;
    }

    public Map<String, Object> getTodaySummary(Authentication authentication) {
        return null;
    }

    public Map<String, Object> getTodos(Integer limit, Authentication authentication) {
        return null;
    }

    public Map<String, Object> getRecentActivities(Integer days, Integer limit, Authentication authentication) {
        return null;
    }
}