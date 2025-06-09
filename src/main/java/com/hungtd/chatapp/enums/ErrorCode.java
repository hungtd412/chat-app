package com.hungtd.chatapp.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception!"),
    INVALID_KEY(1001, "Invalid key!"),
    USER_EXISTED( 1002, "User existed!"),
    INVALID_USERNAME(1003, "Username must be at least 3 characters!"),
    INVALID_PASSWORD(1004, "Password must be at least 3 characters!"),
    USER_NOT_EXISTED(1005, "User not existed!"),
    UNAUTHENTICATED(1006, "Unauthenticated!"),
    FORBIDDEN(1007, "Permission required!")
    ;

    int code;
    String message;
}
