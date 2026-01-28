package com.maigen.common.core.exception;

import com.maigen.common.core.enums.CommonEnum;

/**
 * 公共异常类
 */
public class CommonException extends RuntimeException {

    private final int code;

    /**
     * 构造方法
     */
    public CommonException(String message) {
        super(message);
        this.code = CommonEnum.FAIL.getCode();
    }

    /**
     * 构造方法
     */
    public CommonException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造方法
     */
    public CommonException(CommonEnum commonEnum) {
        super(commonEnum.getMessage());
        this.code = commonEnum.getCode();
    }

    public int getCode() {
        return code;
    }
}