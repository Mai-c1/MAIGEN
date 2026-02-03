package com.maigen.api.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONUtil;
import com.maigen.api.entity.OperationLog;
import com.maigen.api.mapper.OperationLogMapper;
import com.maigen.common.core.annotation.Log;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogMapper operationLogMapper;

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            saveLog(joinPoint, logAnnotation, duration, exception);
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, Log logAnnotation, long duration, Exception e) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;
            HttpServletRequest request = attributes.getRequest();

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            
            OperationLog opLog = OperationLog.builder()
                    .userId(StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null)
                    .module(logAnnotation.module())
                    .operation(logAnnotation.operation())
                    .method(signature.getDeclaringTypeName() + "." + signature.getName())
                    .params(JSONUtil.toJsonStr(joinPoint.getArgs()))
                    .ip(JakartaServletUtil.getClientIP(request))
                    .status(e == null ? 1 : 0)
                    .errorMsg(e != null ? e.getMessage() : null)
                    .duration(duration)
                    .createdAt(LocalDateTime.now())
                    .build();

            operationLogMapper.insert(opLog);
        } catch (Exception ex) {
            log.error("保存操作日志失败", ex);
        }
    }
}
