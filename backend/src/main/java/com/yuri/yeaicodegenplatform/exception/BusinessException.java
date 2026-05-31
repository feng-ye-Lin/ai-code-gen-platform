package com.yuri.yeaicodegenplatform.exception;

import lombok.Getter;

/**
 * @author yuri
 * @create 2026-05-31 12:24
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
