package com.maigen.common.core.exception;

import lombok.Data;

@Data
public class CustomException extends RuntimeException {
    Integer code;

    public CustomException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public CustomException(CustomExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMessage());
        this.code = exceptionEnum.getCode();
    }
}
