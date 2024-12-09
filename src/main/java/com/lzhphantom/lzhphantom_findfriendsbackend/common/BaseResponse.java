package com.lzhphantom.lzhphantom_findfriendsbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 返回结果通用
 *
 * @param <T> 类型
 * @author lzhphantom
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
