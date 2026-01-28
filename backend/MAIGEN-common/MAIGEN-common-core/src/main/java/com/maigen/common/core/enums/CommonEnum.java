package com.maigen.common.core.enums;

/**
 * 公共枚举类
 */
public enum CommonEnum {

    /**
     * 成功状态
     */
    SUCCESS(200, "成功"),

    /**
     * 失败状态
     */
    FAIL(500, "失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在");

    private final int code;
    private final String message;

    CommonEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}