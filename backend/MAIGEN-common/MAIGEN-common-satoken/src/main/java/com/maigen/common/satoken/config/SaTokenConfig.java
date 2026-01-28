package com.maigen.common.satoken.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token配置类
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加Sa-Token拦截器
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 检查是否登录
            StpUtil.checkLogin();
        }))
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/login", "/api/register");
    }
}