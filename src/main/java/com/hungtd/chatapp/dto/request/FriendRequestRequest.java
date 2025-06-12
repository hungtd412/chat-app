package com.hungtd.chatapp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendRequestRequest {
    @NotNull(message = "RECEIVER_ID_REQUIRED")
    Long receiverId;
}
