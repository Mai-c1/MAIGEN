package com.maigen.common.core.exception;

import com.maigen.common.core.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(CustomException.class)
    public Result<?> handleCustomException(CustomException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage(), e.getCode());
    }

    /**
     * 其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统繁忙，请稍后重试");
    }

    // <2> 处理 json 请求体调用接口校验失败抛出的异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String error = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return Result.error(error);
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public Result<?> handlerFileSizeLimitExceededException(FileSizeLimitExceededException e) {
        return Result.error("文件大小超过限制,单次上传最多100MB");
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    @ResponseBody
    public void sseTimeoutException(AsyncRequestTimeoutException e) {
        log.error("SSE连接已被关闭");
    }
}
