package com.hungtd.chatapp.service.websocket;

import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.entity.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.List;

public interface WebSocketService {
    
    /**
     * Extracts username from JWT token in StompHeaderAccessor
     * 
     * @param headerAccessor STOMP header accessor containing authentication information
     * @return the extracted username
     */
    String extractUsernameFromHeader(StompHeaderAccessor headerAccessor);
    
    /**
     * Sends WebSocket messages based on conversation type
     * 
     * @param message the message entity
     * @param conversation the conversation entity
     * @param currentUserId the ID of the current user (sender)
     * @param currentUsername the username of the current user (sender)
     */
    void sendWebSocketMessages(Message message, Conversation conversation, Long currentUserId, String currentUsername);
    
    /**
     * Finds usernames of other participants in a conversation
     * 
     * @param conversationId the ID of the conversation
     * @param currentUserId the ID of the current user
     * @return list of usernames of other users in the conversation
     */
    List<String> findOtherUsernamesInConversation(Long conversationId, Long currentUserId);
}
