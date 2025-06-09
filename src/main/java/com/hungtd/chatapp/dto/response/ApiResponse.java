package com.hungtd.chatapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.enums.ResultMessage;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    int status; //200
    String result;
    T data;
    ErrorCode error;

    public ApiResponse<T> buildSuccessfulApiResponse(int status, T data) {
        this.status = status;
        this.result = ResultMessage.SUCCESS.name();
        this.data = data;
        return this;
    }

    public ApiResponse<T> buildFailedApiResponse(int status, ErrorCode errorCode) {
        this.status = status;
        this.result = ResultMessage.FAILED.name();
        this.error = errorCode;
        return this;
    }
}
