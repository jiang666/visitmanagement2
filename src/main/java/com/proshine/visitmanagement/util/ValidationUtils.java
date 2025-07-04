package com.proshine.visitmanagement.util;

import com.proshine.visitmanagement.dto.response.ApiResponse;
import com.proshine.visitmanagement.dto.response.UserInfoResponse;
import com.proshine.visitmanagement.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 验证工具类
 * 提供各种数据验证功能
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
public class ValidationUtils {
    
    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    /**
     * 手机号正则表达式（中国大陆）
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^1[3-9]\\d{9}$"
    );
    
    /**
     * 用户名正则表达式（字母、数字、下划线，3-20位）
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$"
    );
    
    /**
     * 密码正则表达式（至少包含字母和数字，6-20位）
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,20}$"
    );
    
    /**
     * 中文姓名正则表达式
     */
    private static final Pattern CHINESE_NAME_PATTERN = Pattern.compile(
            "^[\\u4e00-\\u9fa5]{2,20}$"
    );
    
    /**
     * IP地址正则表达式
     */
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"
    );
    
    /**
     * URL正则表达式
     */
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$"
    );
    
    /**
     * 身份证号正则表达式
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
            "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$"
    );
    
    private ValidationUtils() {
        // 工具类不允许实例化
    }
    
    // ==================== 基础验证方法 ====================
    
    /**
     * 验证字符串非空
     * 
     * @param value 值
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void notBlank(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw ValidationException.Common.required(fieldName);
        }
    }
    
    /**
     * 验证对象非空
     * 
     * @param value 值
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void notNull(Object value, String fieldName) {
        if (value == null) {
            throw ValidationException.Common.required(fieldName);
        }
    }

    public static void notEmpty(Collection<?> collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            throw ValidationException.Common.required(fieldName);
        }
    }
    
    /**
     * 验证字符串长度
     * 
     * @param value 值
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void length(String value, int minLength, int maxLength, String fieldName) {
        if (value == null) {
            return;
        }
        
        if (value.length() < minLength) {
            throw ValidationException.Common.minLength(fieldName, minLength);
        }
        
        if (value.length() > maxLength) {
            throw ValidationException.Common.maxLength(fieldName, maxLength);
        }
    }
    
    /**
     * 验证最大长度
     * 
     * @param value 值
     * @param maxLength 最大长度
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void maxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw ValidationException.Common.maxLength(fieldName, maxLength);
        }
    }
    
    /**
     * 验证最小长度
     * 
     * @param value 值
     * @param minLength 最小长度
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void minLength(String value, int minLength, String fieldName) {
        if (value != null && value.length() < minLength) {
            throw ValidationException.Common.minLength(fieldName, minLength);
        }
    }
    
    /**
     * 验证数值范围
     * 
     * @param value 值
     * @param min 最小值
     * @param max 最大值
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void range(Number value, Number min, Number max, String fieldName) {
        if (value == null) {
            return;
        }
        
        double doubleValue = value.doubleValue();
        double minValue = min.doubleValue();
        double maxValue = max.doubleValue();
        
        if (doubleValue < minValue || doubleValue > maxValue) {
            throw ValidationException.Common.outOfRange(fieldName, min, max);
        }
    }
    
    /**
     * 验证最小值
     * 
     * @param value 值
     * @param min 最小值
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void min(Number value, Number min, String fieldName) {
        if (value != null && value.doubleValue() < min.doubleValue()) {
            throw ValidationException.of(fieldName, "不能小于" + min);
        }
    }
    
    /**
     * 验证最大值
     * 
     * @param value 值
     * @param max 最大值
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void max(Number value, Number max, String fieldName) {
        if (value != null && value.doubleValue() > max.doubleValue()) {
            throw ValidationException.of(fieldName, "不能大于" + max);
        }
    }
    
    // ==================== 格式验证方法 ====================
    
    /**
     * 验证邮箱格式
     * 
     * @param email 邮箱
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void email(String email, String fieldName) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw ValidationException.Common.invalidEmail(fieldName);
        }
    }
    
    /**
     * 验证手机号格式
     * 
     * @param phone 手机号
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void phone(String phone, String fieldName) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw ValidationException.Common.invalidPhone(fieldName);
        }
    }
    
    /**
     * 验证用户名格式
     * 
     * @param username 用户名
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void username(String username, String fieldName) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw ValidationException.User.invalidUsername();
        }
    }
    
    /**
     * 验证密码强度
     * 
     * @param password 密码
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void password(String password, String fieldName) {
        if (!StringUtils.hasText(password)) {
            return;
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw ValidationException.User.weakPassword();
        }
    }
    
    /**
     * 验证中文姓名
     * 
     * @param name 姓名
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void chineseName(String name, String fieldName) {
        if (!StringUtils.hasText(name)) {
            return;
        }
        
        if (!CHINESE_NAME_PATTERN.matcher(name).matches()) {
            throw ValidationException.of(fieldName, "请输入正确的中文姓名");
        }
    }
    
    /**
     * 验证IP地址格式
     * 
     * @param ip IP地址
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void ipAddress(String ip, String fieldName) {
        if (!StringUtils.hasText(ip)) {
            return;
        }
        
        if (!IP_PATTERN.matcher(ip).matches()) {
            throw ValidationException.of(fieldName, "IP地址格式不正确");
        }
    }
    
    /**
     * 验证URL格式
     * 
     * @param url URL地址
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void url(String url, String fieldName) {
        if (!StringUtils.hasText(url)) {
            return;
        }
        
        if (!URL_PATTERN.matcher(url).matches()) {
            throw ValidationException.of(fieldName, "URL格式不正确");
        }
    }
    
    /**
     * 验证身份证号格式
     * 
     * @param idCard 身份证号
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void idCard(String idCard, String fieldName) {
        if (!StringUtils.hasText(idCard)) {
            return;
        }
        
        if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
            throw ValidationException.of(fieldName, "身份证号格式不正确");
        }
    }
    
    /**
     * 验证正则表达式
     * 
     * @param value 值
     * @param pattern 正则表达式
     * @param fieldName 字段名
     * @param errorMessage 错误消息
     * @throws ValidationException 验证失败时抛出
     */
    public static void pattern(String value, Pattern pattern, String fieldName, String errorMessage) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        
        if (!pattern.matcher(value).matches()) {
            throw ValidationException.of(fieldName, errorMessage);
        }
    }
    
    // ==================== 日期时间验证 ====================
    
    /**
     * 验证日期不能是过去
     * 
     * @param date 日期
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void notPastDate(LocalDate date, String fieldName) {
        if (date == null) {
            return;
        }
        
        if (date.isBefore(LocalDate.now())) {
            throw ValidationException.of(fieldName, "不能是过去的日期");
        }
    }
    
    /**
     * 验证日期不能是未来
     * 
     * @param date 日期
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void notFutureDate(LocalDate date, String fieldName) {
        if (date == null) {
            return;
        }
        
        if (date.isAfter(LocalDate.now())) {
            throw ValidationException.of(fieldName, "不能是未来的日期");
        }
    }
    
    /**
     * 验证日期范围
     * 
     * @param date 日期
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void dateRange(LocalDate date, LocalDate startDate, LocalDate endDate, String fieldName) {
        if (date == null || startDate == null || endDate == null) {
            return;
        }
        
        if (date.isBefore(startDate) || date.isAfter(endDate)) {
            throw ValidationException.of(fieldName, 
                    String.format("日期必须在%s到%s之间", 
                            DateUtils.format(startDate), DateUtils.format(endDate)));
        }
    }
    
    /**
     * 验证时间范围
     * 
     * @param time 时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void timeRange(LocalTime time, LocalTime startTime, LocalTime endTime, String fieldName) {
        if (time == null || startTime == null || endTime == null) {
            return;
        }
        
        if (time.isBefore(startTime) || time.isAfter(endTime)) {
            throw ValidationException.of(fieldName, 
                    String.format("时间必须在%s到%s之间", startTime, endTime));
        }
    }
    
    // ==================== 业务验证方法 ====================
    
    /**
     * 验证拜访日期
     * 
     * @param visitDate 拜访日期
     * @throws ValidationException 验证失败时抛出
     */
    public static void visitDate(LocalDate visitDate) {
        notNull(visitDate, "visitDate");
        
        if (!DateUtils.isValidVisitDate(visitDate)) {
            throw ValidationException.Visit.pastVisitDate();
        }
        
        // 不能是周末
        if (DateUtils.isWeekend(visitDate)) {
            throw ValidationException.of("visitDate", "拜访日期不能是周末");
        }
    }
    
    /**
     * 验证拜访时间
     * 
     * @param visitTime 拜访时间
     * @throws ValidationException 验证失败时抛出
     */
    public static void visitTime(LocalTime visitTime) {
        if (visitTime == null) {
            return;
        }
        
        if (!DateUtils.isRecommendedVisitTime(visitTime)) {
            throw ValidationException.of("visitTime", "建议在工作时间内安排拜访（9:00-11:30, 14:00-17:00）");
        }
    }
    
    /**
     * 验证拜访时长
     * 
     * @param durationMinutes 拜访时长（分钟）
     * @throws ValidationException 验证失败时抛出
     */
    public static void visitDuration(Integer durationMinutes) {
        if (durationMinutes == null) {
            return;
        }
        
        if (durationMinutes < 1 || durationMinutes > 1440) {
            throw ValidationException.Visit.invalidDuration();
        }
    }
    
    /**
     * 验证拜访评分
     * 
     * @param rating 评分
     * @throws ValidationException 验证失败时抛出
     */
    public static void visitRating(Integer rating) {
        if (rating == null) {
            return;
        }
        
        if (rating < 1 || rating > 5) {
            throw ValidationException.Visit.invalidRating();
        }
    }
    
    /**
     * 验证跟进日期
     * 
     * @param visitDate 拜访日期
     * @param followUpDate 跟进日期
     * @throws ValidationException 验证失败时抛出
     */
    public static void followUpDate(LocalDate visitDate, LocalDate followUpDate) {
        if (visitDate == null || followUpDate == null) {
            return;
        }
        
        if (!DateUtils.isValidFollowUpDate(visitDate, followUpDate)) {
            throw ValidationException.Visit.invalidFollowUpDate();
        }
    }
    
    /**
     * 验证客户姓名
     * 
     * @param customerName 客户姓名
     * @throws ValidationException 验证失败时抛出
     */
    public static void customerName(String customerName) {
        notBlank(customerName, "customerName");
        maxLength(customerName, 100, "customerName");
        
        // 检查是否包含中文字符或英文字符
        if (!customerName.matches(".*[\\u4e00-\\u9fa5a-zA-Z]+.*")) {
            throw ValidationException.Customer.invalidName();
        }
    }
    
    /**
     * 验证客户生日
     * 
     * @param birthday 生日
     * @throws ValidationException 验证失败时抛出
     */
    public static void customerBirthday(LocalDate birthday) {
        if (birthday == null) {
            return;
        }
        
        if (birthday.isAfter(LocalDate.now())) {
            throw ValidationException.Customer.futureBirthday();
        }
        
        // 不能超过120岁
        if (birthday.isBefore(LocalDate.now().minusYears(120))) {
            throw ValidationException.of("birthday", "生日不能超过120年前");
        }
    }
    
    /**
     * 验证学校名称
     * 
     * @param schoolName 学校名称
     * @throws ValidationException 验证失败时抛出
     */
    public static void schoolName(String schoolName) {
        notBlank(schoolName, "schoolName");
        length(schoolName, 2, 200, "schoolName");
    }
    
    /**
     * 验证网站URL
     * 
     * @param website 网站地址
     * @throws ValidationException 验证失败时抛出
     */
    public static void website(String website) {
        if (!StringUtils.hasText(website)) {
            return;
        }
        
        maxLength(website, 200, "website");
        url(website, "website");
    }
    
    // ==================== 枚举验证方法 ====================
    
    /**
     * 验证枚举值
     * 
     * @param value 值
     * @param enumClass 枚举类
     * @param fieldName 字段名
     * @param <E> 枚举类型
     * @throws ValidationException 验证失败时抛出
     */
    public static <E extends Enum<E>> void enumValue(String value, Class<E> enumClass, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        
        try {
            Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            E[] constants = enumClass.getEnumConstants();
            String[] validValues = new String[constants.length];
            for (int i = 0; i < constants.length; i++) {
                validValues[i] = constants[i].name();
            }
            throw ValidationException.Common.invalidEnum(fieldName, validValues);
        }
    }
    
    /**
     * 验证拜访类型
     * 
     * @param visitType 拜访类型
     * @throws ValidationException 验证失败时抛出
     */
    public static void visitType(String visitType) {
        if (!StringUtils.hasText(visitType)) {
            return;
        }
        
        String[] validTypes = {"FIRST_VISIT", "FOLLOW_UP", "TECHNICAL", "BUSINESS", "AFTER_SALES"};
        if (!java.util.Arrays.asList(validTypes).contains(visitType)) {
            throw ValidationException.Common.invalidEnum("visitType", validTypes);
        }
    }
    
    /**
     * 验证拜访状态
     * 
     * @param status 拜访状态
     * @throws ValidationException 验证失败时抛出
     */
    public static void visitStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return;
        }
        
        String[] validStatuses = {"SCHEDULED", "COMPLETED", "CANCELLED", "POSTPONED"};
        if (!java.util.Arrays.asList(validStatuses).contains(status)) {
            throw ValidationException.Common.invalidEnum("status", validStatuses);
        }
    }
    
    /**
     * 验证意向等级
     * 
     * @param intentLevel 意向等级
     * @throws ValidationException 验证失败时抛出
     */
    public static void intentLevel(String intentLevel) {
        if (!StringUtils.hasText(intentLevel)) {
            return;
        }
        
        String[] validLevels = {"A", "B", "C", "D"};
        if (!java.util.Arrays.asList(validLevels).contains(intentLevel)) {
            throw ValidationException.Common.invalidEnum("intentLevel", validLevels);
        }
    }
    
    /**
     * 验证用户角色
     * 
     * @param role 用户角色
     * @throws ValidationException 验证失败时抛出
     */
    public static void userRole(String role) {
        if (!StringUtils.hasText(role)) {
            return;
        }
        
        String[] validRoles = {"ADMIN", "MANAGER", "SALES"};
        if (!java.util.Arrays.asList(validRoles).contains(role)) {
            throw ValidationException.Common.invalidEnum("role", validRoles);
        }
    }
    
    // ==================== 集合验证方法 ====================
    
    /**
     * 验证集合大小
     * 
     * @param collection 集合
     * @param minSize 最小大小
     * @param maxSize 最大大小
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void collectionSize(Collection<?> collection, int minSize, int maxSize, String fieldName) {
        if (collection == null) {
            return;
        }
        
        int size = collection.size();
        if (size < minSize) {
            throw ValidationException.of(fieldName, "至少需要" + minSize + "个元素");
        }
        
        if (size > maxSize) {
            throw ValidationException.of(fieldName, "最多允许" + maxSize + "个元素");
        }
    }
    
    /**
     * 验证列表中无重复元素
     * 
     * @param list 列表
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void noDuplicates(List<?> list, String fieldName) {
        if (list == null || list.isEmpty()) {
            return;
        }
        
        long uniqueCount = list.stream().distinct().count();
        if (uniqueCount != list.size()) {
            throw ValidationException.of(fieldName, "不能包含重复元素");
        }
    }
    
    // ==================== 复合验证方法 ====================
    
    /**
     * 验证用户注册信息
     * 
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @param realName 真实姓名
     * @throws ValidationException 验证失败时抛出
     */
    public static void userRegistration(String username, String password, String email, String realName) {
        ValidationException.ValidationExceptionBuilder builder = ValidationException.builder();
        
        try {
            notBlank(username, "username");
            username(username, "username");
        } catch (ValidationException e) {
            builder.addError("username", e.getMessage());
        }
        
        try {
            notBlank(password, "password");
            password(password, "password");
        } catch (ValidationException e) {
            builder.addError("password", e.getMessage());
        }
        
        try {
            if (StringUtils.hasText(email)) {
                email(email, "email");
            }
        } catch (ValidationException e) {
            builder.addError("email", e.getMessage());
        }
        
        try {
            notBlank(realName, "realName");
            maxLength(realName, 100, "realName");
        } catch (ValidationException e) {
            builder.addError("realName", e.getMessage());
        }
        
        builder.throwIfHasErrors();
    }
    
    /**
     * 验证拜访记录信息
     * 
     * @param customerId 客户ID
     * @param visitDate 拜访日期
     * @param visitTime 拜访时间
     * @param durationMinutes 拜访时长
     * @param followUpDate 跟进日期
     * @throws ValidationException 验证失败时抛出
     */
    public static void visitRecord(Long customerId, LocalDate visitDate, LocalTime visitTime, 
                                 Integer durationMinutes, LocalDate followUpDate) {
        ValidationException.ValidationExceptionBuilder builder = ValidationException.builder();
        
        try {
            notNull(customerId, "customerId");
        } catch (ValidationException e) {
            builder.addError("customerId", e.getMessage());
        }
        
        try {
            visitDate(visitDate);
        } catch (ValidationException e) {
            builder.addError("visitDate", e.getMessage());
        }
        
        try {
            visitTime(visitTime);
        } catch (ValidationException e) {
            builder.addError("visitTime", e.getMessage());
        }
        
        try {
            visitDuration(durationMinutes);
        } catch (ValidationException e) {
            builder.addError("durationMinutes", e.getMessage());
        }
        
        try {
            followUpDate(visitDate, followUpDate);
        } catch (ValidationException e) {
            builder.addError("followUpDate", e.getMessage());
        }
        
        builder.throwIfHasErrors();
    }
    
    /**
     * 验证客户信息
     * 
     * @param name 客户姓名
     * @param email 邮箱
     * @param phone 手机号
     * @param birthday 生日
     * @throws ValidationException 验证失败时抛出
     */
    public static void customerInfo(String name, String email, String phone, LocalDate birthday) {
        ValidationException.ValidationExceptionBuilder builder = ValidationException.builder();
        
        try {
            customerName(name);
        } catch (ValidationException e) {
            builder.addError("name", e.getMessage());
        }
        
        try {
            if (StringUtils.hasText(email)) {
                email(email, "email");
            }
        } catch (ValidationException e) {
            builder.addError("email", e.getMessage());
        }
        
        try {
            if (StringUtils.hasText(phone)) {
                phone(phone, "phone");
            }
        } catch (ValidationException e) {
            builder.addError("phone", e.getMessage());
        }
        
        try {
            customerBirthday(birthday);
        } catch (ValidationException e) {
            builder.addError("birthday", e.getMessage());
        }
        
        builder.throwIfHasErrors();
    }
    
    // ==================== 判断方法（返回boolean） ====================
    
    /**
     * 检查邮箱格式是否有效
     * 
     * @param email 邮箱
     * @return 是否有效
     */
    public static boolean isValidEmail(String email) {
        return StringUtils.hasText(email) && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * 检查手机号格式是否有效
     * 
     * @param phone 手机号
     * @return 是否有效
     */
    public static boolean isValidPhone(String phone) {
        return StringUtils.hasText(phone) && PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * 检查用户名格式是否有效
     * 
     * @param username 用户名
     * @return 是否有效
     */
    public static boolean isValidUsername(String username) {
        return StringUtils.hasText(username) && USERNAME_PATTERN.matcher(username).matches();
    }
    
    /**
     * 检查密码强度是否符合要求
     * 
     * @param password 密码
     * @return 是否有效
     */
    public static boolean isValidPassword(String password) {
        return StringUtils.hasText(password) && PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * 检查身份证号格式是否有效
     * 
     * @param idCard 身份证号
     * @return 是否有效
     */
    public static boolean isValidIdCard(String idCard) {
        return StringUtils.hasText(idCard) && ID_CARD_PATTERN.matcher(idCard).matches();
    }
    
    /**
     * 检查URL格式是否有效
     * 
     * @param url URL地址
     * @return 是否有效
     */
    public static boolean isValidUrl(String url) {
        return StringUtils.hasText(url) && URL_PATTERN.matcher(url).matches();
    }
    
    /**
     * 检查IP地址格式是否有效
     * 
     * @param ip IP地址
     * @return 是否有效
     */
    public static boolean isValidIp(String ip) {
        return StringUtils.hasText(ip) && IP_PATTERN.matcher(ip).matches();
    }
    
    /**
     * 检查字符串是否为空或仅包含空白字符
     * 
     * @param value 字符串值
     * @return 是否为空
     */
    public static boolean isBlank(String value) {
        return !StringUtils.hasText(value);
    }
    
    /**
     * 检查字符串长度是否在指定范围内
     * 
     * @param value 字符串值
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @return 是否在范围内
     */
    public static boolean isLengthInRange(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        int length = value.length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * 检查数值是否在指定范围内
     * 
     * @param value 数值
     * @param min 最小值
     * @param max 最大值
     * @return 是否在范围内
     */
    public static boolean isInRange(Number value, Number min, Number max) {
        if (value == null) {
            return false;
        }
        double doubleValue = value.doubleValue();
        return doubleValue >= min.doubleValue() && doubleValue <= max.doubleValue();
    }
    
    /**
     * 检查日期是否在指定范围内
     * 
     * @param date 日期
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 是否在范围内
     */
    public static boolean isDateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    // ==================== 批量验证方法 ====================
    
    /**
     * 批量验证用户信息
     * 
     * @param users 用户信息列表
     * @throws ValidationException 验证失败时抛出
     */
    public static void validateUserBatch(List<Map<String, Object>> users) {
        if (users == null || users.isEmpty()) {
            throw ValidationException.of("users", "用户列表不能为空");
        }
        
        ValidationException.ValidationExceptionBuilder builder = ValidationException.builder();
        
        for (int i = 0; i < users.size(); i++) {
            Map<String, Object> user = users.get(i);
            String prefix = "用户[" + (i + 1) + "]";
            
            try {
                String username = (String) user.get("username");
                String email = (String) user.get("email");
                String phone = (String) user.get("phone");
                
                if (isBlank(username)) {
                    builder.addError(prefix, "用户名不能为空");
                } else if (!isValidUsername(username)) {
                    builder.addError(prefix, "用户名格式不正确");
                }
                
                if (StringUtils.hasText(email) && !isValidEmail(email)) {
                    builder.addError(prefix, "邮箱格式不正确");
                }
                
                if (StringUtils.hasText(phone) && !isValidPhone(phone)) {
                    builder.addError(prefix, "手机号格式不正确");
                }
                
            } catch (Exception e) {
                builder.addError(prefix, "数据格式错误");
            }
        }
        
        builder.throwIfHasErrors();
    }
    
    /**
     * 批量验证拜访记录
     * 
     * @param visits 拜访记录列表
     * @throws ValidationException 验证失败时抛出
     */
    public static void validateVisitBatch(List<Map<String, Object>> visits) {
        if (visits == null || visits.isEmpty()) {
            throw ValidationException.of("visits", "拜访记录列表不能为空");
        }
        
        ValidationException.ValidationExceptionBuilder builder = ValidationException.builder();
        
        for (int i = 0; i < visits.size(); i++) {
            Map<String, Object> visit = visits.get(i);
            String prefix = "拜访记录[" + (i + 1) + "]";
            
            try {
                Object customerIdObj = visit.get("customerId");
                String visitDateStr = (String) visit.get("visitDate");
                
                if (customerIdObj == null) {
                    builder.addError(prefix, "客户ID不能为空");
                }
                
                if (isBlank(visitDateStr)) {
                    builder.addError(prefix, "拜访日期不能为空");
                } else {
                    LocalDate visitDate = DateUtils.parseDate(visitDateStr);
                    if (visitDate == null) {
                        builder.addError(prefix, "拜访日期格式不正确");
                    } else if (visitDate.isBefore(LocalDate.now())) {
                        builder.addError(prefix, "拜访日期不能是过去的日期");
                    }
                }
                
            } catch (Exception e) {
                builder.addError(prefix, "数据格式错误");
            }
        }
        
        builder.throwIfHasErrors();
    }
    
    // ==================== 自定义验证方法 ====================
    
    /**
     * 自定义验证方法
     * 
     * @param condition 验证条件
     * @param fieldName 字段名
     * @param errorMessage 错误消息
     * @throws ValidationException 验证失败时抛出
     */
    public static void custom(boolean condition, String fieldName, String errorMessage) {
        if (!condition) {
            throw ValidationException.of(fieldName, errorMessage);
        }
    }
    
    /**
     * 条件验证（只有当条件为真时才进行验证）
     * 
     * @param condition 前置条件
     * @param validator 验证器
     */
    public static void when(boolean condition, Runnable validator) {
        if (condition) {
            validator.run();
        }
    }
    
    /**
     * 验证两个值相等
     * 
     * @param value1 值1
     * @param value2 值2
     * @param fieldName 字段名
     * @throws ValidationException 验证失败时抛出
     */
    public static void equals(Object value1, Object value2, String fieldName) {
        if (!java.util.Objects.equals(value1, value2)) {
            throw ValidationException.of(fieldName, "两个值必须相等");
        }
    }
    
    /**
     * 验证密码确认
     * 
     * @param password 密码
     * @param confirmPassword 确认密码
     * @throws ValidationException 验证失败时抛出
     */
    public static void passwordConfirm(String password, String confirmPassword) {
        if (!java.util.Objects.equals(password, confirmPassword)) {
            throw ValidationException.User.passwordMismatch();
        }
    }
    
    // ==================== 数据类型验证 ====================
    
    /**
     * 验证是否为有效的整数
     * 
     * @param value 字符串值
     * @param fieldName 字段名
     * @return 解析后的整数
     * @throws ValidationException 验证失败时抛出
     */
    public static Integer parseInteger(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw ValidationException.of(fieldName, "必须是有效的整数");
        }
    }
    
    /**
     * 验证是否为有效的长整数
     * 
     * @param value 字符串值
     * @param fieldName 字段名
     * @return 解析后的长整数
     * @throws ValidationException 验证失败时抛出
     */
    public static Long parseLong(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw ValidationException.of(fieldName, "必须是有效的长整数");
        }
    }
    
    /**
     * 验证是否为有效的小数
     * 
     * @param value 字符串值
     * @param fieldName 字段名
     * @return 解析后的小数
     * @throws ValidationException 验证失败时抛出
     */
    public static BigDecimal parseBigDecimal(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw ValidationException.of(fieldName, "必须是有效的数字");
        }
    }
    
    /**
     * 验证是否为有效的日期
     * 
     * @param value 字符串值
     * @param fieldName 字段名
     * @return 解析后的日期
     * @throws ValidationException 验证失败时抛出
     */
    public static LocalDate parseDate(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        
        LocalDate date = DateUtils.parseDate(value);
        if (date == null) {
            throw ValidationException.Common.invalidDate(fieldName);
        }
        
        return date;
    }
    
    /**
     * 验证是否为有效的日期时间
     * 
     * @param value 字符串值
     * @param fieldName 字段名
     * @return 解析后的日期时间
     * @throws ValidationException 验证失败时抛出
     */
    public static LocalDateTime parseDateTime(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        
        LocalDateTime dateTime = DateUtils.parseDateTime(value);
        if (dateTime == null) {
            throw ValidationException.Common.invalidDate(fieldName);
        }
        
        return dateTime;
    }
    
    // ==================== 文件验证方法 ====================
    
    /**
     * 验证文件名
     * 
     * @param fileName 文件名
     * @throws ValidationException 验证失败时抛出
     */
    public static void fileName(String fileName) {
        notBlank(fileName, "fileName");
        maxLength(fileName, 255, "fileName");
        
        // 检查文件名是否包含非法字符
        Pattern invalidChars = Pattern.compile("[\\\\/:*?\"<>|]");
        if (invalidChars.matcher(fileName).find()) {
            throw ValidationException.of("fileName", "文件名包含非法字符");
        }
    }
    
    /**
     * 验证文件扩展名
     * 
     * @param fileName 文件名
     * @param allowedExtensions 允许的扩展名
     * @throws ValidationException 验证失败时抛出
     */
    public static void fileExtension(String fileName, String[] allowedExtensions) {
        if (!StringUtils.hasText(fileName)) {
            return;
        }
        
        String extension = FileUtils.getFileExtension(fileName).toLowerCase();
        boolean isAllowed = false;
        
        for (String allowed : allowedExtensions) {
            if (allowed.toLowerCase().equals(extension)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw ValidationException.of("fileName", 
                    "文件类型不支持，允许的类型: " + String.join(", ", allowedExtensions));
        }
    }
    
    // ==================== 业务特定验证 ====================
    
    /**
     * 验证导入数据的必填字段
     * 
     * @param dataMap 数据映射
     * @param requiredFields 必填字段列表
     * @throws ValidationException 验证失败时抛出
     */
    public static void importDataRequired(Map<String, Object> dataMap, List<String> requiredFields) {
        ValidationException.ValidationExceptionBuilder builder = ValidationException.builder();
        
        for (String field : requiredFields) {
            Object value = dataMap.get(field);
            if (value == null || (value instanceof String && isBlank((String) value))) {
                builder.addError(field, "不能为空");
            }
        }
        
        builder.throwIfHasErrors();
    }
    
    /**
     * 验证分页参数
     * 
     * @param page 页码
     * @param size 页大小
     * @throws ValidationException 验证失败时抛出
     */
    public static void pagination(Integer page, Integer size) {
        if (page != null && page < 0) {
            throw ValidationException.of("page", "页码不能小于0");
        }
        
        if (size != null) {
            if (size <= 0) {
                throw ValidationException.of("size", "页大小必须大于0");
            }
            if (size > 1000) {
                throw ValidationException.of("size", "页大小不能超过1000");
            }
        }
    }
    
    /**
     * 验证排序参数
     * 
     * @param sort 排序字段
     * @param allowedFields 允许的排序字段
     * @throws ValidationException 验证失败时抛出
     */
    public static void sortFields(String sort, List<String> allowedFields) {
        if (!StringUtils.hasText(sort)) {
            return;
        }
        
        String[] sortFields = sort.split(",");
        for (String field : sortFields) {
            String fieldName = field.trim();
            // 移除可能的排序方向后缀
            if (fieldName.endsWith(" ASC") || fieldName.endsWith(" DESC")) {
                fieldName = fieldName.substring(0, fieldName.lastIndexOf(" ")).trim();
            }
            
            if (!allowedFields.contains(fieldName)) {
                throw ValidationException.of("sort", 
                        "不支持按字段[" + fieldName + "]排序，允许的字段: " + String.join(", ", allowedFields));
            }
        }
    }

    /**
     * 验证导出格式
     *
     * @param format 导出格式
     * @param allowedFormats 允许的格式列表
     * @throws ValidationException 验证失败时抛出
     */
    public static void exportFormat(String format, List<String> allowedFormats) {
        if (!StringUtils.hasText(format)) {
            throw ValidationException.of("format", "导出格式不能为空");
        }

        if (allowedFormats == null || allowedFormats.isEmpty()) {
            throw ValidationException.of("allowedFormats", "允许的格式列表不能为空");
        }

        boolean isValid = false;
        for (String allowedFormat : allowedFormats) {
            if (allowedFormat.equalsIgnoreCase(format)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw ValidationException.of("format",
                    "不支持的导出格式: " + format + "，支持的格式: " + String.join(", ", allowedFormats));
        }
    }
}