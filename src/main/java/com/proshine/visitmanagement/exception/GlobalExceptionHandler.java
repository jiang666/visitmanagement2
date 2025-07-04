package com.proshine.visitmanagement.exception;

import com.proshine.visitmanagement.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author System
 * @since 2024-01-01
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());
        return createErrorResponse(400, e.getMessage(), request.getRequestURI());
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        log.warn("资源未找到异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());
        return createErrorResponse(404, e.getMessage(), request.getRequestURI());
    }

    /**
     * 处理验证异常
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(ValidationException e, HttpServletRequest request) {
        log.warn("验证异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());
        return createErrorResponse(400, e.getMessage(), request.getRequestURI());
    }

    /**
     * 处理方法参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("参数验证异常 - 路径: {}, 错误: {}", request.getRequestURI(), errorMessage);
        return createErrorResponse(400, "参数验证失败: " + errorMessage, request.getRequestURI());
    }

    /**
     * 处理Bean绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBindException(BindException e, HttpServletRequest request) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("绑定异常 - 路径: {}, 错误: {}", request.getRequestURI(), errorMessage);
        return createErrorResponse(400, "参数绑定失败: " + errorMessage, request.getRequestURI());
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        log.warn("约束违反异常 - 路径: {}, 错误: {}", request.getRequestURI(), errorMessage);
        return createErrorResponse(400, "参数约束违反: " + errorMessage, request.getRequestURI());
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());
        return createErrorResponse(401, "认证失败，请重新登录", request.getRequestURI());
    }

    /**
     * 处理凭据错误异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        log.warn("凭据错误异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());
        return createErrorResponse(401, "用户名或密码错误", request.getRequestURI());
    }

    /**
     * 处理Spring Security访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("访问拒绝异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());
        return createErrorResponse(403, "访问被拒绝，权限不足", request.getRequestURI());
    }

    /**
     * 处理文件访问拒绝异常
     */
    @ExceptionHandler(java.nio.file.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleFileAccessDeniedException(java.nio.file.AccessDeniedException e, HttpServletRequest request) {
        log.warn("文件访问拒绝异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());
        return createErrorResponse(403, "文件访问被拒绝", request.getRequestURI());
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持异常 - 路径: {}, 方法: {}, 支持的方法: {}",
                request.getRequestURI(), e.getMethod(), e.getSupportedHttpMethods());
        return createErrorResponse(405, "请求方法不支持: " + e.getMethod(), request.getRequestURI());
    }

    /**
     * 处理媒体类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ApiResponse<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        log.warn("媒体类型不支持异常 - 路径: {}, 类型: {}", request.getRequestURI(), e.getContentType());
        return createErrorResponse(415, "媒体类型不支持: " + e.getContentType(), request.getRequestURI());
    }

    /**
     * 处理请求体不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体不可读异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());
        return createErrorResponse(400, "请求体格式错误，无法解析", request.getRequestURI());
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数异常 - 路径: {}, 参数: {}", request.getRequestURI(), e.getParameterName());
        return createErrorResponse(400, "缺少必需参数: " + e.getParameterName(), request.getRequestURI());
    }

    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("方法参数类型不匹配异常 - 路径: {}, 参数: {}, 值: {}, 期望类型: {}",
                request.getRequestURI(), e.getName(), e.getValue(), e.getRequiredType());
        return createErrorResponse(400, "参数类型错误: " + e.getName(), request.getRequestURI());
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("404异常 - 路径: {}, 方法: {}", e.getRequestURL(), e.getHttpMethod());
        return createErrorResponse(404, "请求的资源不存在", request.getRequestURI());
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("文件上传大小超限异常 - 路径: {}, 最大大小: {}", request.getRequestURI(), e.getMaxUploadSize());
        return createErrorResponse(400, "文件大小超出限制", request.getRequestURI());
    }

    /**
     * 处理数据完整性违反异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn("数据完整性违反异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());

        String message = "数据操作失败";
        if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
            SQLIntegrityConstraintViolationException sqlException = (SQLIntegrityConstraintViolationException) e.getCause();
            if (sqlException.getMessage().contains("Duplicate entry")) {
                message = "数据已存在，不能重复添加";
            } else if (sqlException.getMessage().contains("foreign key constraint")) {
                message = "存在关联数据，无法删除";
            }
        }

        return createErrorResponse(400, message, request.getRequestURI());
    }

    /**
     * 处理SQL完整性约束违反异常
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e, HttpServletRequest request) {
        log.warn("SQL完整性约束违反异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());

        String message = "数据库约束违反";
        if (e.getMessage().contains("Duplicate entry")) {
            message = "数据已存在，不能重复添加";
        } else if (e.getMessage().contains("foreign key constraint")) {
            message = "存在关联数据，无法删除";
        }

        return createErrorResponse(400, message, request.getRequestURI());
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常 - 路径: {}", request.getRequestURI(), e);
        return createErrorResponse(500, "系统内部错误", request.getRequestURI());
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage());
        return createErrorResponse(400, "参数错误: " + e.getMessage(), request.getRequestURI());
    }

    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        log.error("非法状态异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage(), e);
        return createErrorResponse(500, "系统状态异常", request.getRequestURI());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常 - 路径: {}, 消息: {}", request.getRequestURI(), e.getMessage(), e);
        return createErrorResponse(500, "系统运行异常", request.getRequestURI());
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("未知异常 - 路径: {}, 异常类型: {}, 消息: {}",
                request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage(), e);
        return createErrorResponse(500, "系统内部错误", request.getRequestURI());
    }

    /**
     * 创建错误响应
     *
     * @param code 错误码
     * @param message 错误消息
     * @param path 请求路径
     * @return 错误响应
     */
    private ApiResponse<Void> createErrorResponse(Integer code, String message, String path) {
        return ApiResponse.<Void>builder()
                .code(code)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
}