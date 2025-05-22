package com.hungtd.chatapp.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String id;
    String phone;
    String email;
    String password;
    String firstName;
    String lastName;
    Boolean isActive;
    Boolean isBlocked;
    String preferences;
}
