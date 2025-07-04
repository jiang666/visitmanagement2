package com.proshine.visitmanagement.exception;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 资源未找到异常类
 * 用于处理资源不存在的情况，通常对应HTTP 404状态码
 * 
 * @author System
 * @since 2024-01-01
 */

@Setter
@Getter
public class ResourceNotFoundException extends RuntimeException {
    
    /**
     * 资源类型
     * -- GETTER --
     *  获取资源类型
     *
     *
     * -- SETTER --
     *  设置资源类型
     *
     @return 资源类型
      * @param resourceType 资源类型

     */
    private String resourceType;
    
    /**
     * 资源标识符
     * -- GETTER --
     *  获取资源标识符
     *
     *
     * -- SETTER --
     *  设置资源标识符
     *
     @return 资源标识符
      * @param resourceId 资源标识符

     */
    private Object resourceId;
    
    /**
     * 默认构造函数
     */
    public ResourceNotFoundException() {
        super("资源未找到");
    }
    
    /**
     * 构造函数 - 只包含错误消息
     *
     * @param message 错误消息
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    /**
     * 构造函数 - 包含资源类型和资源ID
     *
     * @param resourceType 资源类型
     * @param resourceId 资源标识符
     */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s[%s]未找到", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * 构造函数 - 包含错误消息和原因
     *
     * @param message 错误消息
     * @param cause 异常原因
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造函数 - 包含资源类型、资源ID和原因
     *
     * @param resourceType 资源类型
     * @param resourceId 资源标识符
     * @param cause 异常原因
     */
    public ResourceNotFoundException(String resourceType, Object resourceId, Throwable cause) {
        super(String.format("%s[%s]未找到", resourceType, resourceId), cause);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * 构造函数 - 只包含原因
     *
     * @param cause 异常原因
     */
    public ResourceNotFoundException(Throwable cause) {
        super("资源未找到", cause);
    }

    // 静态工厂方法，提供更便捷的异常创建方式
    
    /**
     * 创建资源未找到异常
     *
     * @param resourceType 资源类型
     * @param resourceId 资源标识符
     * @return 资源未找到异常
     */
    public static ResourceNotFoundException of(String resourceType, Object resourceId) {
        return new ResourceNotFoundException(resourceType, resourceId);
    }
    
    /**
     * 创建资源未找到异常 - 带自定义消息
     *
     * @param message 自定义消息
     * @return 资源未找到异常
     */
    public static ResourceNotFoundException withMessage(String message) {
        return new ResourceNotFoundException(message);
    }
    
    // 业务实体相关的静态工厂方法
    
    /**
     * 用户相关异常
     */
    public static class User {
        
        /**
         * 用户ID未找到
         */
        public static ResourceNotFoundException byId(Long userId) {
            return ResourceNotFoundException.of("用户", userId);
        }
        
        /**
         * 用户名未找到
         */
        public static ResourceNotFoundException byUsername(String username) {
            return ResourceNotFoundException.of("用户", username);
        }
        
        /**
         * 邮箱未找到
         */
        public static ResourceNotFoundException byEmail(String email) {
            return ResourceNotFoundException.of("用户", email);
        }
        
        /**
         * 通用用户未找到
         */
        public static ResourceNotFoundException notFound() {
            return ResourceNotFoundException.withMessage("用户不存在");
        }
    }
    
    /**
     * 拜访记录相关异常
     */
    public static class Visit {
        
        /**
         * 拜访记录ID未找到
         */
        public static ResourceNotFoundException byId(Long visitId) {
            return ResourceNotFoundException.of("拜访记录", visitId);
        }
        
        /**
         * 通用拜访记录未找到
         */
        public static ResourceNotFoundException notFound() {
            return ResourceNotFoundException.withMessage("拜访记录不存在");
        }
        
        /**
         * 指定客户的拜访记录未找到
         */
        public static ResourceNotFoundException byCustomer(Long customerId) {
            return ResourceNotFoundException.withMessage("客户[" + customerId + "]的拜访记录不存在");
        }
        
        /**
         * 指定销售的拜访记录未找到
         */
        public static ResourceNotFoundException bySales(Long salesId) {
            return ResourceNotFoundException.withMessage("销售[" + salesId + "]的拜访记录不存在");
        }
    }
    
    /**
     * 客户相关异常
     */
    public static class Customer {
        
        /**
         * 客户ID未找到
         */
        public static ResourceNotFoundException byId(Long customerId) {
            return ResourceNotFoundException.of("客户", customerId);
        }
        
        /**
         * 客户姓名未找到
         */
        public static ResourceNotFoundException byName(String customerName) {
            return ResourceNotFoundException.of("客户", customerName);
        }
        
        /**
         * 通用客户未找到
         */
        public static ResourceNotFoundException notFound() {
            return ResourceNotFoundException.withMessage("客户不存在");
        }
        
        /**
         * 指定院系的客户未找到
         */
        public static ResourceNotFoundException byDepartment(Long departmentId) {
            return ResourceNotFoundException.withMessage("院系[" + departmentId + "]下没有客户");
        }
    }
    
    /**
     * 学校相关异常
     */
    public static class School {
        
        /**
         * 学校ID未找到
         */
        public static ResourceNotFoundException byId(Long schoolId) {
            return ResourceNotFoundException.of("学校", schoolId);
        }
        
        /**
         * 学校名称未找到
         */
        public static ResourceNotFoundException byName(String schoolName) {
            return ResourceNotFoundException.of("学校", schoolName);
        }
        
        /**
         * 通用学校未找到
         */
        public static ResourceNotFoundException notFound() {
            return ResourceNotFoundException.withMessage("学校不存在");
        }
        
        /**
         * 指定城市的学校未找到
         */
        public static ResourceNotFoundException byCity(String city) {
            return ResourceNotFoundException.withMessage("城市[" + city + "]下没有学校");
        }
    }
    
    /**
     * 院系相关异常
     */
    public static class Department {
        
        /**
         * 院系ID未找到
         */
        public static ResourceNotFoundException byId(Long departmentId) {
            return ResourceNotFoundException.of("院系", departmentId);
        }
        
        /**
         * 院系名称未找到
         */
        public static ResourceNotFoundException byName(String departmentName) {
            return ResourceNotFoundException.of("院系", departmentName);
        }
        
        /**
         * 通用院系未找到
         */
        public static ResourceNotFoundException notFound() {
            return ResourceNotFoundException.withMessage("院系不存在");
        }
        
        /**
         * 指定学校的院系未找到
         */
        public static ResourceNotFoundException bySchool(Long schoolId) {
            return ResourceNotFoundException.withMessage("学校[" + schoolId + "]下没有院系");
        }
    }
    
    /**
     * 文件相关异常
     */
    public static class File {
        
        /**
         * 文件ID未找到
         */
        public static ResourceNotFoundException byId(String fileId) {
            return ResourceNotFoundException.of("文件", fileId);
        }
        
        /**
         * 文件路径未找到
         */
        public static ResourceNotFoundException byPath(String filePath) {
            return ResourceNotFoundException.of("文件", filePath);
        }
        
        /**
         * 文件名未找到
         */
        public static ResourceNotFoundException byName(String fileName) {
            return ResourceNotFoundException.of("文件", fileName);
        }
        
        /**
         * 通用文件未找到
         */
        public static ResourceNotFoundException notFound() {
            return ResourceNotFoundException.withMessage("文件不存在");
        }
        
        /**
         * 导出文件未找到
         */
        public static ResourceNotFoundException exportNotFound() {
            return ResourceNotFoundException.withMessage("导出文件不存在或已过期");
        }
    }
    
    /**
     * 配置相关异常
     */
    public static class Config {
        
        /**
         * 配置项未找到
         */
        public static ResourceNotFoundException byKey(String configKey) {
            return ResourceNotFoundException.of("配置项", configKey);
        }
        
        /**
         * 通用配置未找到
         */
        public static ResourceNotFoundException notFound() {
            return ResourceNotFoundException.withMessage("配置不存在");
        }
    }
    
    /**
     * 权限相关异常
     */
    public static class Permission {
        
        /**
         * 权限代码未找到
         */
        public static ResourceNotFoundException byCode(String permissionCode) {
            return ResourceNotFoundException.of("权限", permissionCode);
        }
        
        /**
         * 角色权限未找到
         */
        public static ResourceNotFoundException byRole(String roleName) {
            return ResourceNotFoundException.withMessage("角色[" + roleName + "]的权限配置不存在");
        }
        
        /**
         * 通用权限未找到
         */
        public static ResourceNotFoundException notFound() {
            return ResourceNotFoundException.withMessage("权限不存在");
        }
    }
    
    @Override
    public String toString() {
        return "ResourceNotFoundException{" +
                "resourceType='" + resourceType + '\'' +
                ", resourceId=" + resourceId +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}