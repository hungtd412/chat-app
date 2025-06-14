package com.hungtd.chatapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hungtd.chatapp.entity.Message;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {
    Long id;
    Long conversationId;
    Long senderId;
    Message.Type type;
    String content;
    LocalDateTime createdAt;
    boolean isBelongCurrentUser;
}
