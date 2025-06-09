package com.hungtd.chatapp.exception;

import com.hungtd.chatapp.enums.ErrorCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class AppException extends RuntimeException {
    private int statusCode; //200
    private ErrorCode errorCode;

    public AppException( int statusCode, ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
}
