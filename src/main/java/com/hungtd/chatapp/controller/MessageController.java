package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.request.MessageRequest;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.dto.response.MessageResponse;
import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.entity.Message;
import com.hungtd.chatapp.entity.ParticipantId;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.mapper.MessageMapper;
import com.hungtd.chatapp.repository.ConversationRepository;
import com.hungtd.chatapp.repository.MessageRepository;
import com.hungtd.chatapp.repository.ParticipantRepository;
import com.hungtd.chatapp.repository.UserRepository;
import com.hungtd.chatapp.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageMapper messageMapper;
    private final UserService userService;
    
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessagesByConversationId(
            @PathVariable Long conversationId) {
        // Validate conversation exists
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        
        // Get current user
        User currentUser = userService.currentUser();
        
        // Check if current user is participant in this conversation
        ParticipantId participantId = new ParticipantId(conversationId, currentUser.getId());
        if (!participantRepository.existsById(participantId)) {
            throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
        }
        
        // Get all messages for this conversation ordered by ID (newest first)
        List<Message> messages = messageRepository.findAllByConversationIdOrderByIdDesc(conversationId);
        
        // Use mapper to convert entities to DTOs and set isBelongCurrentUser field
        List<MessageResponse> messageResponses = messages.stream()
                .map(message -> {
                    MessageResponse response = messageMapper.toMessageResponse(message);
                    response.setConversationId(conversationId);
                    response.setBelongCurrentUser(message.getSenderId().equals(currentUser.getId()));
                    return response;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(
                ApiResponse.<List<MessageResponse>>builder()
                        .data(messageResponses)
                        .build()
        );
    }

    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload MessageRequest messageRequest, 
                                  StompHeaderAccessor headerAccessor) {
        // Extract the JWT token from headers
        String token = headerAccessor.getFirstNativeHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            // If no token found, try to get it from sessionAttributes
            token = (String) headerAccessor.getSessionAttributes().get("token");
        }
        
        // This endpoint handles messages sent via WebSocket
        // Using userService instead of Principal since Principal isn't working
        User currentUser = new User();
        
        // If the above didn't work, try to manually extract user from token
        if (token != null) {
            String username = extractUsernameFromToken(token);
            if (username != null) {
                currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            }
        }
        
        // If we still couldn't get the user, throw an exception
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Store the current user ID in a final variable to use in lambda expressions
        final Long currentUserId = currentUser.getId();
        final String currentUsername = currentUser.getUsername();

        // Validate conversation exists
        Long conversationId = messageRequest.getConversationId();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        
        // Check if current user is participant in this conversation
        ParticipantId participantId = new ParticipantId(conversationId, currentUserId);
        if (!participantRepository.existsById(participantId)) {
            throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
        }
        
        // Create and save message
        Message message = Message.builder()
                .conversation(conversation)
                .senderId(currentUserId)
                .type(Message.Type.valueOf(messageRequest.getType().name()))
                .message(messageRequest.getMessage())
                .build();
        
        Message savedMessage = messageRepository.save(message);
        
        // Create response with needed fields
        MessageResponse messageResponse = messageMapper.toMessageResponse(savedMessage);
        messageResponse.setConversationId(conversationId);
        messageResponse.setBelongCurrentUser(true);
        
        // Send message via WebSocket based on conversation type
        if (conversation.getType() == Conversation.Type.PRIVATE) {
            // For private conversations, find the other participant and send to their queue
            Long otherUserId = participantRepository.findUserIdsByConversationId(conversationId).stream()
                    .filter(id -> !id.equals(currentUserId)) // Using effectively final currentUserId
                    .findFirst()
                    .orElse(null);
            
            if (otherUserId != null) {
                // Get the other user
                User otherUser = userRepository.findById(otherUserId).orElse(null);
                if (otherUser != null) {
                    // Create a message response for the recipient
                    MessageResponse otherUserMessage = messageMapper.toMessageResponse(savedMessage);
                    otherUserMessage.setConversationId(conversationId);
                    otherUserMessage.setBelongCurrentUser(false);
                    
                    messagingTemplate.convertAndSendToUser(
                        otherUser.getUsername(),
                        "/queue/messages",
                        otherUserMessage
                    );
                    
                    messagingTemplate.convertAndSendToUser(
                        currentUsername,
                        "/queue/messages",
                        messageResponse
                    );
                }
            }
        } else {
            // For group conversations, send to the topic for this conversation
            messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId, 
                messageResponse
            );
        }
    }
    
    // Helper method to extract username from JWT token
    private String extractUsernameFromToken(String token) {
        try {
            // Simple JWT parsing - you should adapt this to your token structure
            String[] splitToken = token.split("\\.");
            if (splitToken.length != 3) {
                return null;
            }
            
            // Decode the payload (second part of the token)
            String payload = new String(java.util.Base64.getDecoder().decode(splitToken[1]));
            
            // Extract username from payload - customize this based on your JWT structure
            // This is a very simplified example, you should use a proper JWT library
            if (payload.contains("\"sub\":\"")) {
                int startIndex = payload.indexOf("\"sub\":\"") + 7;
                int endIndex = payload.indexOf("\"", startIndex);
                return payload.substring(startIndex, endIndex);
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}