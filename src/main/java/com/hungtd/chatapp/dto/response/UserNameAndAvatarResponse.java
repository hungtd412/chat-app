package com.hungtd.chatapp.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserNameAndAvatarResponse {
    Long id;
    String friendName;
    String avtUrl;
    Long conversationId;
    
    // Constructor without conversationId for backward compatibility
    public UserNameAndAvatarResponse(Long id, String friendName, String avtUrl) {
        this.id = id;
        this.friendName = friendName;
        this.avtUrl = avtUrl;
    }
}
