package com.hungtd.chatapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {
    @NotBlank(message = "EMPTY_ACCESSTOKEN")
    private String accessToken;

    @NotBlank(message = "EMPTY_REFRESHTOKEN")
    private String refreshToken;
}
