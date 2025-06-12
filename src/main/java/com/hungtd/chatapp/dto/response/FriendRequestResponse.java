package com.hungtd.chatapp.dto.response;

import com.hungtd.chatapp.enums.FriendRequestStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendRequestResponse {
    Long id;
    UserResponse sender;
    UserResponse receiver;
    FriendRequestStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
