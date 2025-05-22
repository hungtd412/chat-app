package com.hungtd.chatapp.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String phone;
    String email;
    String firstName;
    String lastName;
    Boolean isActive;
    Boolean isBlocked;
    String preferences;
}
