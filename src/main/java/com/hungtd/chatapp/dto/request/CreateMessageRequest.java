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
public class CreateMessageRequest {
    @NotNull(message = "EMPTY_CONVERSATION_ID")
    private Long conversationId;
    
    @NotBlank(message = "MISSING_CONTENT_FIELD")
    private String content;
    
    @NotNull(message = "EMPTY_MESSAGE_TYPE")
    private Message.Type type;
}
