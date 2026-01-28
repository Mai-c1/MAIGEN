package com.maigen.common.satoken.util;

import cn.dev33.satoken.stp.StpUtil;

/**
 * Sa-Token工具类
 */
public class SaTokenUtil {

    /**
     * 登录
     */
    public static void login(Object id) {
        StpUtil.login(id);
    }

    /**
     * 退出登录
     */
    public static void logout() {
        StpUtil.logout();
    }

    /**
     * 获取当前用户ID
     */
    public static Object getCurrentUserId() {
        return StpUtil.getLoginId();
    }

    /**
     * 检查是否登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }

    /**
     * 获取Token
     */
    public static String getToken() {
        return StpUtil.getTokenValue();
    }
}