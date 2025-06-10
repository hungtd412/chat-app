package com.hungtd.chatapp.dto.request;

import com.hungtd.chatapp.validator.dob.DobConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "EMPTY_USERNAME")
    @Size(min = 3, message = "MIN_USERNAME_LENGTH")
    @Size(max = 20, message = "MAX_USERNAME_LENGTH")
    String username;

    @NotBlank(message = "EMPTY_EMAIL")
    @Size(max = 50, message = "EMAIL_EXISTED")
    String email;

    @Size(min = 3, message = "MIN_PASSWORD_LENGTH")
    @NotBlank(message = "EMPTY_PASSWORD")
    String password;

    @Size(min = 3, message = "MIN_FIRSTNAME_LENGTH")
    @Size(max = 20, message = "MAX_FIRSTNAME_LENGTH")
    @NotBlank(message = "EMPTY_FIRSTNAME")
    String firstName;

    @Size(min = 3, message = "MIN_LASTNAME_LENGTH")
    @Size(max = 20, message = "MAX_LASTNAME_LENGTH")
    @NotBlank(message = "EMPTY_LASTNAME")
    String lastName;

    @NotNull(message = "EMPTY_DOB")
    @DobConstraint(min = 18, message = "MIN_AGE")
    LocalDate dob;
}
