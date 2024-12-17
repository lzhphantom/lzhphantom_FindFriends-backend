package com.lzhphantom.lzhphantom_findfriendsbackend.exception;

import com.lzhphantom.lzhphantom_findfriendsbackend.common.BaseResponse;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.ErrorCode;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.ResultUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessException(BusinessException e){
        log.error("BusinessException:{}", e.getMessage(),e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<?> runtimeException(RuntimeException e){
        log.error("runtimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
