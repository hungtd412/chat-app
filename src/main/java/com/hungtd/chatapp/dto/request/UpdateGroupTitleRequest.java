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
public class UpdateGroupTitleRequest {
    @NotBlank(message = "EMPTY_TITLE")
    @Size(min = 1, message = "MIN_CONVERSATION_TITLE_LENGTH")
    @Size(max = 50, message = "MAX_CONVERSATION_TITLE_LENGTH")
    String title;
}
