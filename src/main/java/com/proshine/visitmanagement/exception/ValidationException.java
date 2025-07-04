package com.proshine.visitmanagement.exception;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 验证异常类
 * 用于处理数据验证失败的情况，通常对应HTTP 400状态码
 * 
 * @author System
 * @since 2024-01-01
 */
public class ValidationException extends RuntimeException {
    
    /**
     * 验证错误列表
     */
    private List<ValidationError> errors;
    
    /**
     * 默认构造函数
     */
    public ValidationException() {
        super("数据验证失败");
        this.errors = new ArrayList<>();
    }
    
    /**
     * 构造函数 - 只包含错误消息
     *
     * @param message 错误消息
     */
    public ValidationException(String message) {
        super(message);
        this.errors = new ArrayList<>();
    }
    
    /**
     * 构造函数 - 包含单个验证错误
     *
     * @param field 字段名
     * @param message 错误消息
     */
    public ValidationException(String field, String message) {
        super(message);
        this.errors = new ArrayList<>();
        this.errors.add(new ValidationError(field, message));
    }
    
    /**
     * 构造函数 - 包含验证错误列表
     *
     * @param errors 验证错误列表
     */
    public ValidationException(List<ValidationError> errors) {
        super(buildMessage(errors));
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
    }
    
    /**
     * 构造函数 - 包含错误消息和原因
     *
     * @param message 错误消息
     * @param cause 异常原因
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errors = new ArrayList<>();
    }
    
    /**
     * 构造函数 - 只包含原因
     *
     * @param cause 异常原因
     */
    public ValidationException(Throwable cause) {
        super("数据验证失败", cause);
        this.errors = new ArrayList<>();
    }
    
    /**
     * 获取验证错误列表
     *
     * @return 验证错误列表
     */
    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * 设置验证错误列表
     *
     * @param errors 验证错误列表
     */
    public void setErrors(List<ValidationError> errors) {
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
    }
    
    /**
     * 添加验证错误
     *
     * @param field 字段名
     * @param message 错误消息
     */
    public void addError(String field, String message) {
        this.errors.add(new ValidationError(field, message));
    }
    
    /**
     * 添加验证错误
     *
     * @param error 验证错误
     */
    public void addError(ValidationError error) {
        this.errors.add(error);
    }
    
    /**
     * 是否有验证错误
     *
     * @return 是否有错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * 获取错误数量
     *
     * @return 错误数量
     */
    public int getErrorCount() {
        return errors.size();
    }
    
    /**
     * 获取指定字段的错误
     *
     * @param field 字段名
     * @return 错误列表
     */
    public List<String> getFieldErrors(String field) {
        return errors.stream()
                .filter(error -> Objects.equals(error.getField(), field))
                .map(ValidationError::getMessage)
                .collect(Collectors.toList());
    }
    
    /**
     * 构建错误消息
     *
     * @param errors 错误列表
     * @return 错误消息
     */
    private static String buildMessage(List<ValidationError> errors) {
        if (errors == null || errors.isEmpty()) {
            return "数据验证失败";
        }
        
        return errors.stream()
                .map(error -> error.getField() + ": " + error.getMessage())
                .collect(Collectors.joining("; "));
    }
    
    // 静态工厂方法
    
    /**
     * 创建验证异常
     *
     * @param field 字段名
     * @param message 错误消息
     * @return 验证异常
     */
    public static ValidationException of(String field, String message) {
        return new ValidationException(field, message);
    }
    
    /**
     * 创建验证异常 - 带自定义消息
     *
     * @param message 自定义消息
     * @return 验证异常
     */
    public static ValidationException withMessage(String message) {
        return new ValidationException(message);
    }
    
    /**
     * 创建验证异常构建器
     *
     * @return 验证异常构建器
     */
    public static ValidationExceptionBuilder builder() {
        return new ValidationExceptionBuilder();
    }
    
    // 常见验证场景的静态工厂方法
    
    /**
     * 通用验证异常
     */
    public static class Common {
        
        /**
         * 必填字段为空
         */
        public static ValidationException required(String field) {
            return ValidationException.of(field, "不能为空");
        }
        
        /**
         * 字段长度超限
         */
        public static ValidationException maxLength(String field, int maxLength) {
            return ValidationException.of(field, "长度不能超过" + maxLength + "个字符");
        }
        
        /**
         * 字段长度不足
         */
        public static ValidationException minLength(String field, int minLength) {
            return ValidationException.of(field, "长度不能少于" + minLength + "个字符");
        }
        
        /**
         * 数值超出范围
         */
        public static ValidationException outOfRange(String field, Object min, Object max) {
            return ValidationException.of(field, "值必须在" + min + "到" + max + "之间");
        }
        
        /**
         * 格式错误
         */
        public static ValidationException invalidFormat(String field) {
            return ValidationException.of(field, "格式不正确");
        }
        
        /**
         * 邮箱格式错误
         */
        public static ValidationException invalidEmail(String field) {
            return ValidationException.of(field, "邮箱格式不正确");
        }
        
        /**
         * 手机号格式错误
         */
        public static ValidationException invalidPhone(String field) {
            return ValidationException.of(field, "手机号格式不正确");
        }
        
        /**
         * 日期格式错误
         */
        public static ValidationException invalidDate(String field) {
            return ValidationException.of(field, "日期格式不正确");
        }
        
        /**
         * 枚举值无效
         */
        public static ValidationException invalidEnum(String field, String[] validValues) {
            return ValidationException.of(field, "值必须是以下之一: " + String.join(", ", validValues));
        }
    }
    
    /**
     * 用户验证异常
     */
    public static class User {
        
        /**
         * 用户名格式错误
         */
        public static ValidationException invalidUsername() {
            return ValidationException.of("username", "用户名只能包含字母、数字和下划线，长度为3-20个字符");
        }
        
        /**
         * 密码强度不够
         */
        public static ValidationException weakPassword() {
            return ValidationException.of("password", "密码必须包含字母和数字，长度为6-20个字符");
        }
        
        /**
         * 密码确认不匹配
         */
        public static ValidationException passwordMismatch() {
            return ValidationException.of("confirmPassword", "密码确认不匹配");
        }
        
        /**
         * 真实姓名格式错误
         */
        public static ValidationException invalidRealName() {
            return ValidationException.of("realName", "真实姓名只能包含中文、字母，长度为2-20个字符");
        }
    }
    
    /**
     * 拜访记录验证异常
     */
    public static class Visit {
        
        /**
         * 拜访日期不能是过去
         */
        public static ValidationException pastVisitDate() {
            return ValidationException.of("visitDate", "拜访日期不能是过去的日期");
        }
        
        /**
         * 拜访时长无效
         */
        public static ValidationException invalidDuration() {
            return ValidationException.of("durationMinutes", "拜访时长必须在1-1440分钟之间");
        }
        
        /**
         * 评分无效
         */
        public static ValidationException invalidRating() {
            return ValidationException.of("rating", "评分必须在1-5分之间");
        }
        
        /**
         * 跟进日期不能早于拜访日期
         */
        public static ValidationException invalidFollowUpDate() {
            return ValidationException.of("followUpDate", "跟进日期不能早于拜访日期");
        }
    }
    
    /**
     * 客户验证异常
     */
    public static class Customer {
        
        /**
         * 客户姓名格式错误
         */
        public static ValidationException invalidName() {
            return ValidationException.of("name", "客户姓名只能包含中文、字母，长度为2-50个字符");
        }
        
        /**
         * 职位格式错误
         */
        public static ValidationException invalidPosition() {
            return ValidationException.of("position", "职位长度不能超过100个字符");
        }
        
        /**
         * 生日不能是未来日期
         */
        public static ValidationException futureBirthday() {
            return ValidationException.of("birthday", "生日不能是未来日期");
        }
    }
    
    /**
     * 学校验证异常
     */
    public static class School {
        
        /**
         * 学校名称格式错误
         */
        public static ValidationException invalidName() {
            return ValidationException.of("name", "学校名称长度为2-200个字符");
        }
        
        /**
         * 网站URL格式错误
         */
        public static ValidationException invalidWebsite() {
            return ValidationException.of("website", "网站URL格式不正确");
        }
    }
    
    /**
     * 文件验证异常
     */
    public static class File {
        
        /**
         * 文件类型不支持
         */
        public static ValidationException unsupportedType(String fileName) {
            return ValidationException.of("file", "文件[" + fileName + "]类型不支持");
        }
        
        /**
         * 文件大小超限
         */
        public static ValidationException sizeTooLarge(String fileName, long maxSize) {
            return ValidationException.of("file", "文件[" + fileName + "]大小超出限制，最大允许" + (maxSize / 1024 / 1024) + "MB");
        }
        
        /**
         * 文件为空
         */
        public static ValidationException emptyFile() {
            return ValidationException.of("file", "文件不能为空");
        }
    }
    
    /**
     * 验证异常构建器
     */
    public static class ValidationExceptionBuilder {
        private final List<ValidationError> errors = new ArrayList<>();
        
        /**
         * 添加字段错误
         */
        public ValidationExceptionBuilder addError(String field, String message) {
            errors.add(new ValidationError(field, message));
            return this;
        }
        
        /**
         * 添加必填验证错误
         */
        public ValidationExceptionBuilder required(String field) {
            return addError(field, "不能为空");
        }
        
        /**
         * 添加长度验证错误
         */
        public ValidationExceptionBuilder maxLength(String field, int maxLength) {
            return addError(field, "长度不能超过" + maxLength + "个字符");
        }
        
        /**
         * 添加格式验证错误
         */
        public ValidationExceptionBuilder invalidFormat(String field) {
            return addError(field, "格式不正确");
        }
        
        /**
         * 构建异常
         */
        public ValidationException build() {
            return new ValidationException(errors);
        }
        
        /**
         * 如果有错误则抛出异常
         */
        public void throwIfHasErrors() {
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }
        }
    }
    
    /**
     * 验证错误信息
     */
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
        
        public ValidationError() {}
        
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        
        public ValidationError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }
        
        public String getField() {
            return field;
        }
        
        public void setField(String field) {
            this.field = field;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public Object getRejectedValue() {
            return rejectedValue;
        }
        
        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }
        
        @Override
        public String toString() {
            return "ValidationError{" +
                    "field='" + field + '\'' +
                    ", message='" + message + '\'' +
                    ", rejectedValue=" + rejectedValue +
                    '}';
        }
    }
    
    @Override
    public String toString() {
        return "ValidationException{" +
                "errors=" + errors +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}