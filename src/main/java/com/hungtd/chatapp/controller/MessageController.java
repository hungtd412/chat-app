package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.request.MessageRequest;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.dto.response.MessageResponse;
import com.hungtd.chatapp.entity.Message;
import com.hungtd.chatapp.mapper.MessageMapper;
import com.hungtd.chatapp.service.chat.MessageService;
import com.hungtd.chatapp.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final MessageMapper messageMapper;
    private final UserService userService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Message>> sendMessage(@Valid @RequestBody MessageRequest messageRequest) {
        Message sentMessage = messageService.sendMessage(messageRequest);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<Message>builder()
                        .data(sentMessage)
                        .build()
        );
    }
    
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessagesByConversationId(
            @PathVariable Long conversationId) {
        List<Message> messages = messageService.getMessagesByConversationId(conversationId);
        Long currentUserId = userService.currentUser().getId();
        
        // Use mapper to convert entities to DTOs and set isBelongCurrentUser field
        List<MessageResponse> messageResponses = messages.stream()
                .map(message -> {
                    MessageResponse response = messageMapper.toMessageResponse(message);
                    response.setBelongCurrentUser(message.getSenderId().equals(currentUserId));
                    return response;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(
                ApiResponse.<List<MessageResponse>>builder()
                        .data(messageResponses)
                        .build()
        );
    }
}
