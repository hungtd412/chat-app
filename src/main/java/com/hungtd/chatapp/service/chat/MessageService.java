package com.hungtd.chatapp.service.chat;

import com.hungtd.chatapp.dto.request.MessageRequest;
import com.hungtd.chatapp.dto.response.MessageResponse;
import com.hungtd.chatapp.entity.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.List;

public interface MessageService {
    /**
     * Retrieves all messages for a specific conversation
     * 
     * @param conversationId the ID of the conversation
     * @return list of messages ordered by ID (newest first)
     */
    List<Message> getMessagesByConversationId(Long conversationId);
    
    /**
     * Processes and sends a chat message
     * 
     * @param messageRequest the message request containing the message details
     * @param headerAccessor the STOMP header accessor containing authentication information
     */
    void processChatMessage(MessageRequest messageRequest, StompHeaderAccessor headerAccessor);
}
