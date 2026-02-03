package com.maigen.common.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    /**
     * 限流键的前缀
     */
    String key() default "rate_limit:";

    /**
     * 限流时间窗口 (秒)
     */
    int time() default 60;

    /**
     * 时间窗口内的最大请求次数
     */
    int count() default 10;

    /**
     * 提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}
