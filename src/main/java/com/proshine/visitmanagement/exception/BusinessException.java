package com.proshine.visitmanagement.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况
 * 
 * @author System
 * @since 2024-01-01
 */
@Setter
@Getter
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     * -- GETTER --
     *  获取错误码
     *
     *
     * -- SETTER --
     *  设置错误码
     *
     @return 错误码
      * @param code 错误码

     */
    private Integer code;
    
    /**
     * 默认构造函数
     */
    public BusinessException() {
        super();
        this.code = 400;
    }
    
    /**
     * 构造函数 - 只包含错误消息
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }
    
    /**
     * 构造函数 - 包含错误码和错误消息
     *
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 构造函数 - 包含错误消息和原因
     *
     * @param message 错误消息
     * @param cause 异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
    }
    
    /**
     * 构造函数 - 包含错误码、错误消息和原因
     *
     * @param code 错误码
     * @param message 错误消息
     * @param cause 异常原因
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    /**
     * 构造函数 - 只包含原因
     *
     * @param cause 异常原因
     */
    public BusinessException(Throwable cause) {
        super(cause);
        this.code = 400;
    }

    // 静态工厂方法，提供更便捷的异常创建方式
    
    /**
     * 创建业务异常 - 参数错误
     *
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }
    
    /**
     * 创建业务异常 - 未授权
     *
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }
    
    /**
     * 创建业务异常 - 禁止访问
     *
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }
    
    /**
     * 创建业务异常 - 资源未找到
     *
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }
    
    /**
     * 创建业务异常 - 方法不允许
     *
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException methodNotAllowed(String message) {
        return new BusinessException(405, message);
    }
    
    /**
     * 创建业务异常 - 冲突
     *
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException conflict(String message) {
        return new BusinessException(409, message);
    }
    
    /**
     * 创建业务异常 - 内部服务器错误
     *
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException internalServerError(String message) {
        return new BusinessException(500, message);
    }
    
    // 业务场景相关的静态工厂方法
    
    /**
     * 用户相关异常
     */
    public static class User {
        
        /**
         * 用户不存在
         */
        public static BusinessException notFound() {
            return BusinessException.notFound("用户不存在");
        }
        
        /**
         * 用户名已存在
         */
        public static BusinessException usernameExists() {
            return BusinessException.conflict("用户名已存在");
        }
        
        /**
         * 邮箱已存在
         */
        public static BusinessException emailExists() {
            return BusinessException.conflict("邮箱已存在");
        }
        
        /**
         * 用户已被禁用
         */
        public static BusinessException disabled() {
            return BusinessException.forbidden("用户已被禁用");
        }
        
        /**
         * 密码错误
         */
        public static BusinessException wrongPassword() {
            return BusinessException.unauthorized("密码错误");
        }
        
        /**
         * 无权限操作
         */
        public static BusinessException noPermission() {
            return BusinessException.forbidden("无权限进行此操作");
        }
    }
    
    /**
     * 拜访记录相关异常
     */
    public static class Visit {
        
        /**
         * 拜访记录不存在
         */
        public static BusinessException notFound() {
            return BusinessException.notFound("拜访记录不存在");
        }
        
        /**
         * 拜访日期不能为过去
         */
        public static BusinessException pastDate() {
            return BusinessException.badRequest("拜访日期不能是过去的日期");
        }
        
        /**
         * 拜访状态不允许修改
         */
        public static BusinessException statusNotAllowUpdate() {
            return BusinessException.badRequest("当前状态下不允许修改拜访记录");
        }
        
        /**
         * 同一天不能重复拜访同一客户
         */
        public static BusinessException duplicateVisit() {
            return BusinessException.conflict("同一天不能重复拜访同一客户");
        }
    }
    
    /**
     * 客户相关异常
     */
    public static class Customer {
        
        /**
         * 客户不存在
         */
        public static BusinessException notFound() {
            return BusinessException.notFound("客户不存在");
        }
        
        /**
         * 客户已存在
         */
        public static BusinessException exists() {
            return BusinessException.conflict("客户已存在");
        }
        
        /**
         * 客户有关联数据，不能删除
         */
        public static BusinessException hasRelatedData() {
            return BusinessException.conflict("客户存在关联的拜访记录，无法删除");
        }
    }
    
    /**
     * 学校相关异常
     */
    public static class School {
        
        /**
         * 学校不存在
         */
        public static BusinessException notFound() {
            return BusinessException.notFound("学校不存在");
        }
        
        /**
         * 学校已存在
         */
        public static BusinessException exists() {
            return BusinessException.conflict("学校已存在");
        }
        
        /**
         * 学校有关联数据，不能删除
         */
        public static BusinessException hasRelatedData() {
            return BusinessException.conflict("学校存在关联的院系，无法删除");
        }
    }
    
    /**
     * 院系相关异常
     */
    public static class Department {
        
        /**
         * 院系不存在
         */
        public static BusinessException notFound() {
            return BusinessException.notFound("院系不存在");
        }
        
        /**
         * 院系已存在
         */
        public static BusinessException exists() {
            return BusinessException.conflict("院系已存在");
        }
        
        /**
         * 院系有关联数据，不能删除
         */
        public static BusinessException hasRelatedData() {
            return BusinessException.conflict("院系存在关联的客户，无法删除");
        }
    }
    
    /**
     * 文件相关异常
     */
    public static class File {
        
        /**
         * 文件不存在
         */
        public static BusinessException notFound() {
            return BusinessException.notFound("文件不存在");
        }
        
        /**
         * 文件类型不支持
         */
        public static BusinessException typeNotSupported() {
            return BusinessException.badRequest("文件类型不支持");
        }
        
        /**
         * 文件大小超限
         */
        public static BusinessException sizeTooLarge() {
            return BusinessException.badRequest("文件大小超出限制");
        }
        
        /**
         * 文件上传失败
         */
        public static BusinessException uploadFailed() {
            return BusinessException.internalServerError("文件上传失败");
        }
        
        /**
         * 文件导出失败
         */
        public static BusinessException exportFailed() {
            return BusinessException.internalServerError("文件导出失败");
        }
    }
    
    @Override
    public String toString() {
        return "BusinessException{" +
                "code=" + code +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}