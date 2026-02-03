package com.maigen.api.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.maigen.common.core.annotation.RateLimit;
import com.maigen.common.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;

    @Before("@annotation(rateLimit)")
    public void before(JoinPoint joinPoint, RateLimit rateLimit) {
        String key = rateLimit.key();
        int time = rateLimit.time();
        int count = rateLimit.count();

        String combinedKey = key + getCombineKey(rateLimit, joinPoint);
        
        Long currentCount = redisTemplate.opsForValue().increment(combinedKey);
        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(combinedKey, time, TimeUnit.SECONDS);
        }

        if (currentCount != null && currentCount > count) {
            throw new CustomException(rateLimit.message(), 429);
        }
    }

    /**
     * 构建限流 Key (用户ID + 方法名)
     */
    private String getCombineKey(RateLimit rateLimit, JoinPoint joinPoint) {
        StringBuilder sb = new StringBuilder();
        if (StpUtil.isLogin()) {
            sb.append(StpUtil.getLoginIdAsLong()).append(":");
        } else {
            // 未登录则取 IP (简化版)
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                sb.append(attributes.getRequest().getRemoteAddr()).append(":");
            }
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        sb.append(signature.getMethod().getName());
        return sb.toString();
    }
}
