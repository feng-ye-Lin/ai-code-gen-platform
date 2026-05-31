package com.yuri.yeaicodegenplatform.common;

import com.yuri.yeaicodegenplatform.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yuri
 * @create 2026-05-31 12:27
 */
@Data
public class BaseResponse<T> implements Serializable {
    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
