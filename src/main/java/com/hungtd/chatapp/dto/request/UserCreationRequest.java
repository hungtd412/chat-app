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
public class UserCreationRequest {
    @Size(max = 16)
    String phone;

    @Size(max = 255)
    String email;

    @NotBlank(message = "Password cannot be empty!")
    @Size(min = 3, message = "INVALID_PASSWORD")
    String password;

    @Size(max = 20)
    String firstName;

    @Size(max = 20)
    String lastName;

    String preferences;
}
