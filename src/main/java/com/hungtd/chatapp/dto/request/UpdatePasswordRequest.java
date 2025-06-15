package com.hungtd.chatapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePasswordRequest {
    @NotBlank(message = "EMPTY_PASSWORD")
    String currentPassword;

    @Size(min = 3, message = "MIN_PASSWORD_LENGTH")
    @NotBlank(message = "EMPTY_PASSWORD")
    String password;
}
