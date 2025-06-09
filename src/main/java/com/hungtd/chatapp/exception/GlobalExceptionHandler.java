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
    ResponseEntity<ApiResponse<Object>> handlingGeneralException(RuntimeException exception) {
        ApiResponse<Object> apiResponse = new ApiResponse<>().buildFailedApiResponse(400, ErrorCode.UNCATEGORIZED_EXCEPTION);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Object>> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException ignored) {

        }

        ApiResponse<Object> apiResponse = new ApiResponse<>().buildFailedApiResponse(400,
                errorCode);

        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Object>> handlingAppException(AppException appException) {

        ApiResponse<Object> apiResponse = createApiResponseFromAppException(appException);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
//        ApiResponse<Object> apiResponse = new ApiResponse<>().buildFailedApiResponse(400, ErrorCode.FORBIDDEN);
//        return ResponseEntity.badRequest().body(apiResponse);
//    }

    private ApiResponse<Object> createApiResponseFromAppException(AppException appException) {
        return new ApiResponse<>().buildFailedApiResponse(appException.getStatusCode(),
                appException.getErrorCode());
    }
}
