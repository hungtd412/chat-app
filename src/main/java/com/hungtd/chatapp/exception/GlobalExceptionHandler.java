package com.hungtd.chatapp.exception;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<Void>> handlingGeneralException(RuntimeException exception) {
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;

        ApiResponse<Void> apiResponse = buildApiResponseVoidFromErrorCode(errorCode);

        return ResponseEntity.status(500).body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();

        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION; //DEFAULT EXCEPTION

        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException ignored) {

        }

        ApiResponse<Void> apiResponse = buildApiResponseVoidFromErrorCode(errorCode);

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Void>> handlingAppException(AppException appException) {
        ErrorCode errorCode = appException.getErrorCode();

        ApiResponse<Void> apiResponse = buildApiResponseVoidFromErrorCode(errorCode);

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }
    
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        ApiResponse<Void> apiResponse = buildApiResponseVoidFromErrorCode(errorCode);

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    private ApiResponse<Void> buildApiResponseVoidFromErrorCode(ErrorCode errorCode) {
        return ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }
}
