package com.maigen.api.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 * 拦截所有请求，除了排除的路径外，都需要登录鉴权
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，使用 SaRouter 进行更细粒度的路由匹配
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/**")
                    .notMatch("/auth/**")
                    .notMatch("/doc.html")
                    .notMatch("/webjars/**")
                    .notMatch("/v3/api-docs/**")
                    .notMatch("/swagger-resources/**")
                    .notMatch("/favicon.ico")
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
