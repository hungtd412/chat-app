package com.hungtd.chatapp.projection;

import com.hungtd.chatapp.entity.Conversation;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationProjection {
    Long id;
    String title;
    String image_url;
    String type;
    String friend_name;
    String avt_url;
    String latest_message;
    Timestamp created_at;
}
