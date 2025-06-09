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
public enum ResultMessage {
    SUCCESS("Succeeded"),
    FAILED("Failed")
    ;

    String message;
}
