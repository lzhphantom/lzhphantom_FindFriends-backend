package com.lzhphantom.lzhphantom_findfriendsbackend.common;

/**
 * 返回工具类
 * @author lzhphantom
 */
public class ResultUtils {
    /**
     * 成功
     * @param data 返回数据
     * @return 响应
     * @param <T> 数据类型
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     * @param errorCode 错误码
     * @return 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * @param errorCode 错误码
     * @return 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode,String message) {
        return new BaseResponse<>(errorCode.getCode(),message);
    }

    /**
     * 失败
     * @param code 错误码
     * @return 响应
     */
    public static BaseResponse<?> error(int code,String message) {
        return new BaseResponse<>(code,message);
    }
}
