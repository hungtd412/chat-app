package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.request.MessageRequest;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.dto.response.MessageResponse;
import com.hungtd.chatapp.entity.Message;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.mapper.MessageMapper;
import com.hungtd.chatapp.service.chat.MessageService;
import com.hungtd.chatapp.service.user.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("messages")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageController {

    MessageService messageService;
    MessageMapper messageMapper;
    UserService userService;

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessagesByConversationId(
            @PathVariable Long conversationId) {
        log.debug("Retrieving messages for conversation: {}", conversationId);
        
        // Get messages from service
        List<Message> messages = messageService.getMessagesByConversationId(conversationId);
        
        // Get current user ID for mapping message ownership
        User currentUser = userService.currentUser();
        
        // Use mapper to convert to response DTOs with ownership information
        List<MessageResponse> messageResponses = messageMapper.toMessageResponseList(messages, currentUser.getId());
        
        return ResponseEntity.ok(
                ApiResponse.<List<MessageResponse>>builder()
                        .data(messageResponses)
                        .build()
        );
    }

    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload MessageRequest messageRequest, StompHeaderAccessor headerAccessor) {
        log.debug("Handling chat message for conversation: {}", messageRequest.getConversationId());
        
        messageService.processChatMessage(messageRequest, headerAccessor);
    }
}
