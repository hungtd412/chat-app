package com.hungtd.chatapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequest {
    @NotBlank(message = "EMPTY_ACCESSTOKEN")
    private String accessToken;

    @NotBlank(message = "EMPTY_REFRESHTOKEN")
    private String refreshToken;
}
