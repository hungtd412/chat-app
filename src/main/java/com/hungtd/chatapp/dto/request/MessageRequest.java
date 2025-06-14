package com.hungtd.chatapp.dto.request;

import com.hungtd.chatapp.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest {
    @NotNull(message = "ConversationService ID is required")
    private Long conversationId;
    
    @NotBlank(message = "Message content cannot be empty")
    private String content; // Changed from message to content
    
    @NotNull(message = "Message type is required")
    private Message.Type type;
}
