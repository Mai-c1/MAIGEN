package com.maigen.api.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.util.SaResult;
import com.maigen.common.core.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(1)
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(CustomException.class)
    public SaResult handleCustomException(CustomException e) {
        log.warn("业务异常: {}", e.getMessage());
        return SaResult.error(e.getMessage()).setCode(e.getCode() != null ? e.getCode() : 400);
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public SaResult handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验异常: {}", message);
        return SaResult.error(message).setCode(400);
    }

    /**
     * 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public SaResult handleNotLoginException(NotLoginException e) {
        log.warn("未登录或 Token 失效: {}", e.getMessage());
        return SaResult.error(e.getMessage()).setCode(401);
    }

    /**
     * 无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public SaResult handleNotPermissionException(NotPermissionException e) {
        log.warn("无权限访问: {}", e.getMessage());
        return SaResult.error("无权限访问").setCode(403);
    }

    /**
     * 无角色权限异常
     */
    @ExceptionHandler(NotRoleException.class)
    public SaResult handleNotRoleException(NotRoleException e) {
        log.warn("无角色权限: {}", e.getMessage());
        return SaResult.error("无权限访问").setCode(403);
    }

    /**
     * 系统异常兜底
     */
    @ExceptionHandler(Exception.class)
    public SaResult handleException(Exception e) {
        log.error("系统异常", e);
        return SaResult.error("系统繁忙，请稍后重试").setCode(500);
    }
}
