package com.hungtd.chatapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hungtd.chatapp.entity.Conversation;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse {
    Long id;
    String title;
    String type;
    String imageUrl;
    String friendName;
    String avtUrl;
    String latestMessage;
    Timestamp createdAt;
}
