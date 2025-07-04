package com.proshine.visitmanagement.util;

import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 日期工具类
 * 提供常用的日期时间操作方法
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
public class DateUtils {
    
    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    
    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
    
    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 年月格式
     */
    public static final String YEAR_MONTH_PATTERN = "yyyy-MM";
    
    /**
     * 中文日期格式
     */
    public static final String CHINESE_DATE_PATTERN = "yyyy年MM月dd日";
    
    /**
     * 中文日期时间格式
     */
    public static final String CHINESE_DATETIME_PATTERN = "yyyy年MM月dd日 HH时mm分ss秒";
    
    /**
     * ISO日期时间格式
     */
    public static final String ISO_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    
    /**
     * 紧凑日期格式
     */
    public static final String COMPACT_DATE_PATTERN = "yyyyMMdd";
    
    /**
     * 紧凑日期时间格式
     */
    public static final String COMPACT_DATETIME_PATTERN = "yyyyMMddHHmmss";
    
    // 常用格式化器
    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);
    public static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN);
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);
    public static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern(YEAR_MONTH_PATTERN);
    public static final DateTimeFormatter CHINESE_DATE_FORMATTER = DateTimeFormatter.ofPattern(CHINESE_DATE_PATTERN);
    public static final DateTimeFormatter CHINESE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(CHINESE_DATETIME_PATTERN);
    public static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATETIME_PATTERN);
    public static final DateTimeFormatter COMPACT_DATE_FORMATTER = DateTimeFormatter.ofPattern(COMPACT_DATE_PATTERN);
    public static final DateTimeFormatter COMPACT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(COMPACT_DATETIME_PATTERN);
    
    /**
     * 默认时区
     */
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");
    
    private DateUtils() {
        // 工具类不允许实例化
    }
    
    // ==================== 当前时间获取 ====================
    
    /**
     * 获取当前日期
     */
    public static LocalDate now() {
        return LocalDate.now();
    }
    
    /**
     * 获取当前时间
     */
    public static LocalTime nowTime() {
        return LocalTime.now();
    }
    
    /**
     * 获取当前日期时间
     */
    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now();
    }
    
    /**
     * 获取当前时间戳（毫秒）
     */
    public static long nowTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * 获取当前时间戳（秒）
     */
    public static long nowTimestampSeconds() {
        return System.currentTimeMillis() / 1000;
    }
    
    // ==================== 格式化相关 ====================
    
    /**
     * 格式化日期为字符串
     */
    public static String format(LocalDate date) {
        return date != null ? date.format(DEFAULT_DATE_FORMATTER) : null;
    }
    
    /**
     * 格式化日期为字符串
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null || pattern == null) {
            return null;
        }
        try {
            return date.format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            log.warn("日期格式化失败: date={}, pattern={}", date, pattern, e);
            return null;
        }
    }
    
    /**
     * 格式化时间为字符串
     */
    public static String format(LocalTime time) {
        return time != null ? time.format(DEFAULT_TIME_FORMATTER) : null;
    }
    
    /**
     * 格式化时间为字符串
     */
    public static String format(LocalTime time, String pattern) {
        if (time == null || pattern == null) {
            return null;
        }
        try {
            return time.format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            log.warn("时间格式化失败: time={}, pattern={}", time, pattern, e);
            return null;
        }
    }
    
    /**
     * 格式化日期时间为字符串
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_DATETIME_FORMATTER) : null;
    }
    
    /**
     * 格式化日期时间为字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null) {
            return null;
        }
        try {
            return dateTime.format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            log.warn("日期时间格式化失败: dateTime={}, pattern={}", dateTime, pattern, e);
            return null;
        }
    }
    
    // ==================== 解析相关 ====================
    
    /**
     * 解析日期字符串
     */
    public static LocalDate parseDate(String dateStr) {
        return parseDate(dateStr, DEFAULT_DATE_PATTERN);
    }
    
    /**
     * 解析日期字符串
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        if (dateStr == null || pattern == null) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            log.warn("日期解析失败: dateStr={}, pattern={}", dateStr, pattern, e);
            return null;
        }
    }
    
    /**
     * 解析时间字符串
     */
    public static LocalTime parseTime(String timeStr) {
        return parseTime(timeStr, DEFAULT_TIME_PATTERN);
    }
    
    /**
     * 解析时间字符串
     */
    public static LocalTime parseTime(String timeStr, String pattern) {
        if (timeStr == null || pattern == null) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            log.warn("时间解析失败: timeStr={}, pattern={}", timeStr, pattern, e);
            return null;
        }
    }
    
    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return parseDateTime(dateTimeStr, DEFAULT_DATETIME_PATTERN);
    }
    
    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || pattern == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            log.warn("日期时间解析失败: dateTimeStr={}, pattern={}", dateTimeStr, pattern, e);
            return null;
        }
    }
    
    // ==================== 日期计算 ====================
    
    /**
     * 日期加减天数
     */
    public static LocalDate plusDays(LocalDate date, long days) {
        return date != null ? date.plusDays(days) : null;
    }
    
    /**
     * 日期加减周数
     */
    public static LocalDate plusWeeks(LocalDate date, long weeks) {
        return date != null ? date.plusWeeks(weeks) : null;
    }
    
    /**
     * 日期加减月数
     */
    public static LocalDate plusMonths(LocalDate date, long months) {
        return date != null ? date.plusMonths(months) : null;
    }
    
    /**
     * 日期加减年数
     */
    public static LocalDate plusYears(LocalDate date, long years) {
        return date != null ? date.plusYears(years) : null;
    }
    
    /**
     * 日期时间加减小时
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        return dateTime != null ? dateTime.plusHours(hours) : null;
    }
    
    /**
     * 日期时间加减分钟
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime != null ? dateTime.plusMinutes(minutes) : null;
    }
    
    /**
     * 日期时间加减秒数
     */
    public static LocalDateTime plusSeconds(LocalDateTime dateTime, long seconds) {
        return dateTime != null ? dateTime.plusSeconds(seconds) : null;
    }
    
    // ==================== 日期比较 ====================
    
    /**
     * 比较两个日期是否相等
     */
    public static boolean isSameDate(LocalDate date1, LocalDate date2) {
        if (date1 == null && date2 == null) {
            return true;
        }
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.equals(date2);
    }
    
    /**
     * 判断日期是否在指定范围内
     */
    public static boolean isBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    /**
     * 判断日期时间是否在指定范围内
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (dateTime == null || startDateTime == null || endDateTime == null) {
            return false;
        }
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
    }
    
    /**
     * 判断是否是今天
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }
    
    /**
     * 判断是否是过去的日期
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }
    
    /**
     * 判断是否是未来的日期
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }
    
    /**
     * 判断是否是周末
     */
    public static boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    /**
     * 判断是否是工作日
     */
    public static boolean isWorkday(LocalDate date) {
        return !isWeekend(date);
    }
    
    // ==================== 日期差值计算 ====================
    
    /**
     * 计算两个日期相差的天数
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    /**
     * 计算两个日期相差的周数
     */
    public static long weeksBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.WEEKS.between(startDate, endDate);
    }
    
    /**
     * 计算两个日期相差的月数
     */
    public static long monthsBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.MONTHS.between(startDate, endDate);
    }
    
    /**
     * 计算两个日期相差的年数
     */
    public static long yearsBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.YEARS.between(startDate, endDate);
    }
    
    /**
     * 计算两个时间相差的小时数
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }
    
    /**
     * 计算两个时间相差的分钟数
     */
    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }
    
    /**
     * 计算年龄
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return (int) yearsBetween(birthDate, LocalDate.now());
    }
    
    // ==================== 特殊日期获取 ====================
    
    /**
     * 获取月初日期
     */
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.firstDayOfMonth()) : null;
    }
    
    /**
     * 获取月末日期
     */
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.lastDayOfMonth()) : null;
    }
    
    /**
     * 获取年初日期
     */
    public static LocalDate getFirstDayOfYear(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.firstDayOfYear()) : null;
    }
    
    /**
     * 获取年末日期
     */
    public static LocalDate getLastDayOfYear(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.lastDayOfYear()) : null;
    }
    
    /**
     * 获取本周第一天（周一）
     */
    public static LocalDate getFirstDayOfWeek(LocalDate date) {
        if (date == null) {
            return null;
        }
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        return date.with(weekFields.dayOfWeek(), 1);
    }
    
    /**
     * 获取本周最后一天（周日）
     */
    public static LocalDate getLastDayOfWeek(LocalDate date) {
        if (date == null) {
            return null;
        }
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        return date.with(weekFields.dayOfWeek(), 7);
    }
    
    /**
     * 获取下一个工作日
     */
    public static LocalDate getNextWorkday(LocalDate date) {
        if (date == null) {
            return null;
        }
        LocalDate nextDay = date.plusDays(1);
        while (isWeekend(nextDay)) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }
    
    /**
     * 获取上一个工作日
     */
    public static LocalDate getPreviousWorkday(LocalDate date) {
        if (date == null) {
            return null;
        }
        LocalDate previousDay = date.minusDays(1);
        while (isWeekend(previousDay)) {
            previousDay = previousDay.minusDays(1);
        }
        return previousDay;
    }
    
    // ==================== 日期范围生成 ====================
    
    /**
     * 生成日期范围列表
     */
    public static List<LocalDate> getDateRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dateList = new ArrayList<>();
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return dateList;
        }
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dateList.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        return dateList;
    }
    
    /**
     * 生成工作日范围列表
     */
    public static List<LocalDate> getWorkdayRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> workdayList = new ArrayList<>();
        List<LocalDate> dateRange = getDateRange(startDate, endDate);
        
        for (LocalDate date : dateRange) {
            if (isWorkday(date)) {
                workdayList.add(date);
            }
        }
        return workdayList;
    }
    
    /**
     * 生成月份列表
     */
    public static List<LocalDate> getMonthRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> monthList = new ArrayList<>();
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return monthList;
        }
        
        LocalDate currentMonth = getFirstDayOfMonth(startDate);
        LocalDate endMonth = getFirstDayOfMonth(endDate);
        
        while (!currentMonth.isAfter(endMonth)) {
            monthList.add(currentMonth);
            currentMonth = currentMonth.plusMonths(1);
        }
        return monthList;
    }
    
    // ==================== 时间戳转换 ====================
    
    /**
     * 时间戳转LocalDateTime
     */
    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), DEFAULT_ZONE_ID);
    }
    
    /**
     * LocalDateTime转时间戳
     */
    public static long localDateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli() : 0;
    }
    
    /**
     * LocalDate转时间戳（当天00:00:00）
     */
    public static long localDateToTimestamp(LocalDate date) {
        return date != null ? localDateTimeToTimestamp(date.atStartOfDay()) : 0;
    }
    
    // ==================== 业务相关方法 ====================
    
    /**
     * 判断是否是有效的拜访日期（不能是过去的日期）
     */
    public static boolean isValidVisitDate(LocalDate visitDate) {
        return visitDate != null && !visitDate.isBefore(LocalDate.now());
    }
    
    /**
     * 判断跟进日期是否有效（不能早于拜访日期）
     */
    public static boolean isValidFollowUpDate(LocalDate visitDate, LocalDate followUpDate) {
        if (visitDate == null || followUpDate == null) {
            return false;
        }
        return !followUpDate.isBefore(visitDate);
    }
    
    /**
     * 获取季度第一天
     */
    public static LocalDate getFirstDayOfQuarter(LocalDate date) {
        if (date == null) {
            return null;
        }
        int month = date.getMonthValue();
        int quarterStartMonth = ((month - 1) / 3) * 3 + 1;
        return LocalDate.of(date.getYear(), quarterStartMonth, 1);
    }
    
    /**
     * 获取季度最后一天
     */
    public static LocalDate getLastDayOfQuarter(LocalDate date) {
        if (date == null) {
            return null;
        }
        LocalDate firstDayOfQuarter = getFirstDayOfQuarter(date);
        return firstDayOfQuarter.plusMonths(3).minusDays(1);
    }
    
    /**
     * 获取当前季度
     */
    public static int getCurrentQuarter() {
        return getCurrentQuarter(LocalDate.now());
    }
    
    /**
     * 获取指定日期的季度
     */
    public static int getCurrentQuarter(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return (date.getMonthValue() - 1) / 3 + 1;
    }
    
    /**
     * 友好的时间显示（多久前）
     */
    public static String timeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = minutesBetween(dateTime, now);
        
        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (minutes < 60 * 24) {
            return (minutes / 60) + "小时前";
        } else if (minutes < 60 * 24 * 30) {
            return (minutes / (60 * 24)) + "天前";
        } else if (minutes < 60 * 24 * 365) {
            return (minutes / (60 * 24 * 30)) + "个月前";
        } else {
            return (minutes / (60 * 24 * 365)) + "年前";
        }
    }
    
    // ==================== 拜访管理系统专用方法 ====================
    
    /**
     * 获取本周拜访日期范围
     */
    public static List<LocalDate> getThisWeekVisitDates() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = getFirstDayOfWeek(today);
        LocalDate weekEnd = getLastDayOfWeek(today);
        return getWorkdayRange(weekStart, weekEnd);
    }
    
    /**
     * 获取本月拜访日期范围
     */
    public static List<LocalDate> getThisMonthVisitDates() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = getFirstDayOfMonth(today);
        LocalDate monthEnd = getLastDayOfMonth(today);
        return getWorkdayRange(monthStart, monthEnd);
    }
    
    /**
     * 获取本季度拜访日期范围
     */
    public static List<LocalDate> getThisQuarterVisitDates() {
        LocalDate today = LocalDate.now();
        LocalDate quarterStart = getFirstDayOfQuarter(today);
        LocalDate quarterEnd = getLastDayOfQuarter(today);
        return getWorkdayRange(quarterStart, quarterEnd);
    }
    
    /**
     * 计算拜访频率（每月平均拜访次数）
     */
    public static double calculateVisitFrequency(LocalDate firstVisitDate, LocalDate lastVisitDate, int totalVisits) {
        if (firstVisitDate == null || lastVisitDate == null || totalVisits <= 0) {
            return 0.0;
        }
        
        long totalMonths = monthsBetween(firstVisitDate, lastVisitDate);
        if (totalMonths == 0) {
            return totalVisits;
        }
        
        return (double) totalVisits / totalMonths;
    }
    
    /**
     * 判断是否需要跟进（距离上次拜访超过指定天数）
     */
    public static boolean needFollowUp(LocalDate lastVisitDate, int followUpDays) {
        if (lastVisitDate == null || followUpDays <= 0) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        return daysBetween(lastVisitDate, today) >= followUpDays;
    }
    
    /**
     * 获取下一个建议拜访日期
     */
    public static LocalDate getNextSuggestedVisitDate(LocalDate lastVisitDate, int intervalDays) {
        if (lastVisitDate == null || intervalDays <= 0) {
            return getNextWorkday(LocalDate.now());
        }
        
        LocalDate suggestedDate = plusDays(lastVisitDate, intervalDays);
        
        // 如果建议日期是过去的日期，返回下一个工作日
        if (isPast(suggestedDate)) {
            return getNextWorkday(LocalDate.now());
        }
        
        // 如果建议日期是周末，返回下一个工作日
        if (isWeekend(suggestedDate)) {
            return getNextWorkday(suggestedDate);
        }
        
        return suggestedDate;
    }
    
    /**
     * 生成拜访计划日期列表（排除周末和已有拜访的日期）
     */
    public static List<LocalDate> generateVisitPlan(LocalDate startDate, LocalDate endDate, 
                                                   List<LocalDate> existingVisitDates, 
                                                   int maxVisitsPerWeek) {
        List<LocalDate> plan = new ArrayList<>();
        List<LocalDate> workdays = getWorkdayRange(startDate, endDate);
        
        int visitsThisWeek = 0;
        LocalDate currentWeekStart = null;
        
        for (LocalDate date : workdays) {
            // 跳过已有拜访的日期
            if (existingVisitDates.contains(date)) {
                continue;
            }
            
            // 检查是否是新的一周
            LocalDate weekStart = getFirstDayOfWeek(date);
            if (!weekStart.equals(currentWeekStart)) {
                currentWeekStart = weekStart;
                visitsThisWeek = 0;
            }
            
            // 检查本周拜访次数是否已达上限
            if (visitsThisWeek < maxVisitsPerWeek) {
                plan.add(date);
                visitsThisWeek++;
            }
        }
        
        return plan;
    }
    
    /**
     * 计算客户生日提醒日期（生日前N天）
     */
    public static LocalDate getBirthdayReminderDate(LocalDate birthday, int reminderDays) {
        if (birthday == null || reminderDays < 0) {
            return null;
        }
        
        // 获取今年的生日
        LocalDate thisYearBirthday = birthday.withYear(LocalDate.now().getYear());
        
        // 如果今年生日已过，计算明年的生日
        if (thisYearBirthday.isBefore(LocalDate.now()) || thisYearBirthday.equals(LocalDate.now())) {
            thisYearBirthday = thisYearBirthday.plusYears(1);
        }
        
        return thisYearBirthday.minusDays(reminderDays);
    }
    
    /**
     * 获取即将到来的生日列表（指定天数内）
     */
    public static List<LocalDate> getUpcomingBirthdays(List<LocalDate> birthdays, int withinDays) {
        if (birthdays == null || birthdays.isEmpty() || withinDays <= 0) {
            return new ArrayList<>();
        }
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(withinDays);
        List<LocalDate> upcomingBirthdays = new ArrayList<>();
        
        for (LocalDate birthday : birthdays) {
            if (birthday == null) {
                continue;
            }
            
            // 计算今年的生日
            LocalDate thisYearBirthday = birthday.withYear(today.getYear());
            
            // 如果今年生日已过，检查明年的生日
            if (thisYearBirthday.isBefore(today)) {
                thisYearBirthday = thisYearBirthday.plusYears(1);
            }
            
            // 检查是否在指定天数内
            if (isBetween(thisYearBirthday, today, endDate)) {
                upcomingBirthdays.add(thisYearBirthday);
            }
        }
        
        // 按日期排序
        upcomingBirthdays.sort(LocalDate::compareTo);
        return upcomingBirthdays;
    }
    
    /**
     * 计算最佳拜访时间段（避开午休时间等）
     */
    public static List<String> getOptimalVisitTimeSlots() {
        List<String> timeSlots = new ArrayList<>();
        
        // 上午时间段：9:00-11:30
        for (int hour = 9; hour <= 11; hour++) {
            if (hour == 11) {
                timeSlots.add(String.format("%02d:00-11:30", hour));
                break;
            } else {
                timeSlots.add(String.format("%02d:00-%02d:00", hour, hour + 1));
            }
        }
        
        // 下午时间段：14:00-17:00
        for (int hour = 14; hour <= 16; hour++) {
            timeSlots.add(String.format("%02d:00-%02d:00", hour, hour + 1));
        }
        
        return timeSlots;
    }
    
    /**
     * 判断是否是推荐的拜访时间
     */
    public static boolean isRecommendedVisitTime(LocalTime visitTime) {
        if (visitTime == null) {
            return false;
        }
        
        // 上午推荐时间：9:00-11:30
        LocalTime morningStart = LocalTime.of(9, 0);
        LocalTime morningEnd = LocalTime.of(11, 30);
        
        // 下午推荐时间：14:00-17:00
        LocalTime afternoonStart = LocalTime.of(14, 0);
        LocalTime afternoonEnd = LocalTime.of(17, 0);
        
        return (visitTime.compareTo(morningStart) >= 0 && visitTime.compareTo(morningEnd) <= 0) ||
               (visitTime.compareTo(afternoonStart) >= 0 && visitTime.compareTo(afternoonEnd) <= 0);
    }
    
    /**
     * 计算工作日时长（排除周末）
     */
    public static int calculateWorkdayDuration(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return 0;
        }
        
        return getWorkdayRange(startDate, endDate).size();
    }
    
    /**
     * 生成月度拜访统计的日期标签
     */
    public static List<String> generateMonthlyLabels(int year, int month) {
        List<String> labels = new ArrayList<>();
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = getLastDayOfMonth(monthStart);
        
        LocalDate current = monthStart;
        while (!current.isAfter(monthEnd)) {
            labels.add(format(current, "MM-dd"));
            current = current.plusDays(1);
        }
        
        return labels;
    }
    
    /**
     * 生成季度拜访统计的月份标签
     */
    public static List<String> generateQuarterlyLabels(int year, int quarter) {
        List<String> labels = new ArrayList<>();
        int startMonth = (quarter - 1) * 3 + 1;
        
        for (int i = 0; i < 3; i++) {
            int month = startMonth + i;
            labels.add(String.format("%d年%d月", year, month));
        }
        
        return labels;
    }
    
    /**
     * 获取节假日名称（简单实现，可扩展）
     */
    public static String getHolidayName(LocalDate date) {
        if (date == null) {
            return null;
        }
        
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        
        // 固定节假日
        if (month == 1 && day == 1) {
            return "元旦";
        } else if (month == 5 && day == 1) {
            return "劳动节";
        } else if (month == 10 && day == 1) {
            return "国庆节";
        }
        
        // 可以扩展农历节假日等
        return null;
    }
    
    /**
     * 判断是否是节假日
     */
    public static boolean isHoliday(LocalDate date) {
        return getHolidayName(date) != null;
    }
    
    /**
     * 格式化拜访时长显示
     */
    public static String formatVisitDuration(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes <= 0) {
            return "未知";
        }
        
        if (durationMinutes < 60) {
            return durationMinutes + "分钟";
        } else {
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;
            if (minutes == 0) {
                return hours + "小时";
            } else {
                return hours + "小时" + minutes + "分钟";
            }
        }
    }
    
    /**
     * 获取时间段描述
     */
    public static String getTimeSlotDescription(LocalTime time) {
        if (time == null) {
            return "未知时间";
        }
        
        int hour = time.getHour();
        
        if (hour >= 6 && hour < 9) {
            return "早上";
        } else if (hour >= 9 && hour < 12) {
            return "上午";
        } else if (hour >= 12 && hour < 14) {
            return "中午";
        } else if (hour >= 14 && hour < 18) {
            return "下午";
        } else if (hour >= 18 && hour < 22) {
            return "晚上";
        } else {
            return "深夜";
        }
    }
}